package com.solgroup.core.interceptors.product;

import java.util.*;

import com.solgroup.core.model.VisibilityCategoryModel;
import com.solgroup.core.util.SolGroupUtilities;
import de.hybris.platform.category.model.CategoryModel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Sets;
import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.model.MaterialModel;
import com.solgroup.core.service.stock.SolgroupStockService;
import com.solgroup.core.strategies.SolgroupBaseCommerceSelectionProductStrategy;

import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.product.daos.UnitDao;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.variants.model.GenericVariantProductModel;

public class SolgroupProductPrepareInterceptor implements PrepareInterceptor<ProductModel> {

	private final Logger LOG = Logger.getLogger(SolgroupProductPrepareInterceptor.class);

    private ConfigurationService configurationService;
    private UnitDao unitDao;
	private SolgroupStockService stockService;
	private SolgroupBaseCommerceSelectionProductStrategy baseCommerceSelectionProductStrategy;
	private SolGroupUtilities solGroupUtilities;
	


	@Override
	public void onPrepare(ProductModel product, InterceptorContext ctx) throws InterceptorException {

		// Manage only staged product
		if (BooleanUtils.isTrue(product.getCatalogVersion().getActive())) {
			return;
		}
		
		assessUnit(product);

		assessRefill(product);
		
		assetStock(product, ctx);

		assetApprovalStatus(product, ctx);

	}
	
	private void assetStock(ProductModel product, InterceptorContext ctx) {
		if (ctx.isNew(product))
			createStockLevel(product, ctx);
		else if (ctx.isModified(product, product.STOCKABLE)
				&& product.getItemModelContext().getOriginalValue(product.STOCKABLE) != product.getStockable()) {
			updateStockLevel(product, ctx);
		}
	}
	
	private boolean checkVisibilityCategory(ProductModel product) {
		
		boolean hasVisibilityCategory = false;
		int visibilityCategories = 0;
		Collection<CategoryModel> superCategories = product.getSupercategories();
		if(CollectionUtils.isNotEmpty(superCategories)) {
			for(CategoryModel category : superCategories) {
				if(category instanceof VisibilityCategoryModel) {
					hasVisibilityCategory = true;
					visibilityCategories++;
				}
			}
		}
		
		if(!hasVisibilityCategory || visibilityCategories==0) {
			return false;
		}
		else {
			return true;
		}
		
	}

	private StockLevelModel createStockLevel(ProductModel product, InterceptorContext ctx) {

		StockLevelModel stockLevel = getStockService().createSelfEstimateStockLevel(
				getBaseCommerceSelectionProductStrategy().getDefaultBaseStoreForProduct(product), product);
		// getStockService().saveStockLevel(stockLevel);
		Set<StockLevelModel> stocks = Sets.newHashSet();
		stocks.add(stockLevel);
		product.setStockLevels(stocks);
		return stockLevel;

	}

	private StockLevelModel updateStockLevel(ProductModel product, InterceptorContext ctxif) {

		if(product.getStockLevels().size()!=1)
			LOG.warn("Product " + product.getCode() + " have more stock Levels or don't have one! No update will be performed");
		else
		{
			return getStockService().updateSelfEstimateStockLevel(product);
		}

		return null;

	}

    private void assessUnit(ProductModel product) {
        if(product.getUnit()==null) {

            if(LOG.isInfoEnabled())
                LOG.info(product.getCode() + " don't have unit");


            UnitModel unitModel = null;

            // Variant product --> take unit from base product
            if(product instanceof GenericVariantProductModel) {
                final GenericVariantProductModel genericVariantProductModel = (GenericVariantProductModel)product;
                final ProductModel baseProduct = genericVariantProductModel.getBaseProduct();
                if(baseProduct.getUnit()!=null) {
                    unitModel = baseProduct.getUnit();
                }				
            }

            // Default case --> set sales unit as 'pieces'
            if(unitModel == null) {
                final Collection<UnitModel> units = getUnitDao().findUnitsByCode(SolgroupCoreConstants.DEFAULT_UNIT_MEASURE);
                if(CollectionUtils.isNotEmpty(units)) {
                    product.setUnit(units.iterator().next());
                }
            }
        }
    }

    private void assessRefill(ProductModel product) {

        MaterialModel material = product.getMaterial();
        if(material==null && product instanceof GenericVariantProductModel && ((GenericVariantProductModel)product).getBaseProduct()!=null) {
            final ProductModel baseProduct = ((GenericVariantProductModel)product).getBaseProduct();
            material = baseProduct.getMaterial();
        }

        if(LOG.isInfoEnabled() && (material == null || product.getUnit() == null))
            LOG.warn("Material attribute or Unit attribute is null. Refill assessment will be skipped!");

        // if (material != null && product.getUnit() != null && condition.contains(material.getCode(),
        // product.getUnit().getCode())) {
        // product.setRefill(condition.get(material.getCode(), product.getUnit().getCode()));
        // }

        final List<String> materialsList = Arrays.asList(configurationService.getConfiguration().getString("refill.code.materials").split(","));
        product.setRefill(Boolean.FALSE);
        if(material != null && StringUtils.isNotEmpty(material.getCode()) && product.getUnit() != null && StringUtils.isNotEmpty(product.getUnit().getCode())) {
            for (final String property : materialsList) {
                if (isRefill(property.trim(), material.getCode(), product.getUnit().getCode())) {
                    product.setRefill(Boolean.TRUE);
                }
            }
        }
    }
    /**
     * Product is refill if material is in "refill.code.materials" project property and unit is not "pieces"
     * 
     * @param property
     * @param material
     * @param unit
     * @return
     */
    private boolean isRefill(String property, String material, String unit) {
        if (property.equals(material) && !SolgroupCoreConstants.DEFAULT_UNIT_MEASURE.equals(unit)) {
            return true;
        }
        return false;
    }

 private void assetApprovalStatus(ProductModel product, InterceptorContext ctx) {
    	Boolean hasModifedAttributes = Boolean.FALSE;
    	
    	// New product --> is always in approvalStatus = CHECK
    	if(ctx.isNew(product)) {
    		hasModifedAttributes = Boolean.TRUE;
    	}
    	// Existing product --> check if registry attributes were modified
    	else {
    		
    		// Check base attributes
    		hasModifedAttributes = checkModifiedAttribute(product, ProductModel.NAME, ctx, hasModifedAttributes);
    		hasModifedAttributes = checkModifiedAttribute(product, ProductModel.DESCRIPTION, ctx, hasModifedAttributes);
    		hasModifedAttributes = checkModifiedAttribute(product, ProductModel.UNIT, ctx, hasModifedAttributes);
    		hasModifedAttributes = checkModifiedAttribute(product, ProductModel.SUMMARY, ctx, hasModifedAttributes);
        	
        	
        	// Check classification attributes
//        	List<ProductFeatureModel> featureList = product.getFeatures();
//        	for(ProductFeatureModel feature : featureList) {
//        		if(BooleanUtils.isTrue(hasModifedAttributes)) {
//        			break;
//        		}
//        		hasModifedAttributes = checkModifiedAttribute(feature, ProductFeatureModel.VALUE, ctx, hasModifedAttributes);
//        	}
        	
        	// Check categories
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.SUPERCATEGORIES, ctx, hasModifedAttributes);
//        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.CLASSIFICATIONCLASSES, ctx, hasModifedAttributes);
        	
        	// Multimedia
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.PICTURE, ctx, hasModifedAttributes);
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.THUMBNAIL, ctx, hasModifedAttributes);
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.DATA_SHEET, ctx, hasModifedAttributes);
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.DETAIL, ctx, hasModifedAttributes);
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.LOGO, ctx, hasModifedAttributes);
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.NORMAL, ctx, hasModifedAttributes);
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.OTHERS, ctx, hasModifedAttributes);
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.THUMBNAILS, ctx, hasModifedAttributes);
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.GALLERYIMAGES, ctx, hasModifedAttributes);
        	

        	// Less important attributes
        	//hasModifedAttributes = checkModifiedAttribute(product, ProductModel.CODE, ctx, hasModifedAttributes);
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.ERPCODE, ctx, hasModifedAttributes);
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.ONLINEDATE, ctx, hasModifedAttributes);
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.OFFLINEDATE, ctx, hasModifedAttributes);
        	//hasModifedAttributes = checkModifiedAttribute(product, ProductModel.REFILL, ctx, hasModifedAttributes);
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.STOCKABLE, ctx, hasModifedAttributes);
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.MATERIAL, ctx, hasModifedAttributes);
        	hasModifedAttributes = checkModifiedAttribute(product, ProductModel.SOURCESYSTEM, ctx, hasModifedAttributes);
        	
        	// Check variantCategories and variant values categories
        	if(!hasModifedAttributes) {
	        	Collection<CategoryModel> categories = product.getSupercategories();
	        	if(CollectionUtils.isNotEmpty(categories)) {
	        		String defaultVariantCategory = getConfigurationService().getConfiguration().getString("variantCategory.default");
	        		String defaultVariantValueCategory = getConfigurationService().getConfiguration().getString("variantValueCategory.default");
    				for(CategoryModel category : categories) {
	        			if(category.getCode().endsWith(defaultVariantCategory) || category.getCode().endsWith(defaultVariantValueCategory)) {
	        				hasModifedAttributes = true;
	        				break;
	        			}
	        		}
	        	}
        	}
        	
    	}

    	// set article approval status
    	if(hasModifedAttributes) {
    		product.setApprovalStatus(ArticleApprovalStatus.CHECK);
    	}
    	else if( !(product instanceof GenericVariantProductModel) && !checkVisibilityCategory(product)  ) {
    		product.setApprovalStatus(ArticleApprovalStatus.CHECK);
    	}
    	
    }
    
    
     private Boolean checkModifiedAttribute(ItemModel item, String attributeName, InterceptorContext ctx, Boolean currentModifiedStatus) {
    	if(currentModifiedStatus!=null && currentModifiedStatus.booleanValue()) {
    		return currentModifiedStatus;
    	}
    	
    	if(ctx.isModified(item, attributeName)) {
    		//LOG.info(attributeName + " was modified");
    		return Boolean.TRUE;
    	}
    	else {
    		return currentModifiedStatus;
    	}
    	
    }
     

    private void removeCategory(CategoryModel categoryToRemove, Collection<CategoryModel> currentCategories, Set<CategoryModel> newCategories) {
    	if(newCategories.isEmpty()) {
    		newCategories.addAll(currentCategories);
    	}
    	newCategories.remove(categoryToRemove);
    }
     
    private void addCategory(CategoryModel categoryToAdd, Collection<CategoryModel> currentCategories, Set<CategoryModel> newCategories) {
    	if(newCategories.isEmpty()) {
    		newCategories.addAll(currentCategories);
    	}
    	newCategories.add(categoryToAdd);
    }

    
	protected UnitDao getUnitDao() {
		return unitDao;
	}

	@Required
	public void setUnitDao(UnitDao unitDao) {
		this.unitDao = unitDao;
	}

	protected SolgroupStockService getStockService() {
		return stockService;
	}

	@Required
	public void setStockService(SolgroupStockService stockService) {
		this.stockService = stockService;
	}

	protected SolgroupBaseCommerceSelectionProductStrategy getBaseCommerceSelectionProductStrategy() {
		return baseCommerceSelectionProductStrategy;
	}

	@Required
	public void setBaseCommerceSelectionProductStrategy(
			SolgroupBaseCommerceSelectionProductStrategy baseCommerceSelectionProductStrategy) {
		this.baseCommerceSelectionProductStrategy = baseCommerceSelectionProductStrategy;
	}
    protected ConfigurationService getConfigurationService() {
        return configurationService;
    }

    @Required
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

	public SolGroupUtilities getSolGroupUtilities() {
		return solGroupUtilities;
	}

	@Required
	public void setSolGroupUtilities(SolGroupUtilities solGroupUtilities) {
		this.solGroupUtilities = solGroupUtilities;
	}



	
	
	

	
	
}
