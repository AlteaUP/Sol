/**
 *
 */
package com.solgroup.job.importData.jobs;


import com.google.common.collect.Lists;
import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.dao.tax.SolGroupTaxRowDao;
import com.solgroup.core.enums.MasterSystemEnum;
import com.solgroup.core.enums.ProductStatusEnum;
import com.solgroup.core.model.LegacySystemModel;
import com.solgroup.core.model.job.BatchImportCronJobModel;
import com.solgroup.core.service.categories.SolGroupCategoryService;
import com.solgroup.core.service.product.SolGroupProductService;
import com.solgroup.core.ws.services.product.xml.ProductsImportHybris;
import com.solgroup.core.ws.services.product.xml.ProductsImportHybris.BaseProducts;
import com.solgroup.core.ws.services.product.xml.ProductsImportHybris.BaseProducts.BaseProduct;
import com.solgroup.core.ws.services.product.xml.ProductsImportHybris.BaseProducts.BaseProduct.CommercialCategories.CommercialCategory;
import com.solgroup.core.ws.services.product.xml.ProductsImportHybris.BaseProducts.BaseProduct.Names.Name;
import com.solgroup.core.ws.services.product.xml.ProductsImportHybris.BaseProducts.BaseProduct.Variants.Variant;
import com.solgroup.core.ws.services.product.xml.ProductsImportHybris.BaseProducts.BaseProduct.Variants.Variant.VariantsCategories.VariantCategory;
import com.solgroup.job.importData.abstractJob.AbstractImportJob;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.europe1.enums.ProductTaxGroup;
import de.hybris.platform.europe1.model.TaxRowModel;
import de.hybris.platform.product.daos.ProductDao;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.variants.model.GenericVariantProductModel;
import de.hybris.platform.variants.model.VariantCategoryModel;
import de.hybris.platform.variants.model.VariantTypeModel;
import de.hybris.platform.variants.model.VariantValueCategoryModel;
import jersey.repackaged.com.google.common.collect.Maps;
import jersey.repackaged.com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author salvo
 *
 */
public class ImportProductJob extends AbstractImportJob {

	private ProductDao productDao;
	private SolGroupCategoryService solGroupCategoryService;
    private SolGroupTaxRowDao solGroupTaxRowDao;
    private SolGroupProductService solGroupProductService;
    private ConfigurationService configurationService;

    private final Logger LOG = Logger.getLogger(ImportProductJob.class);
    private static final String PRODUCT_STATUS_ENABLED_CODE = "1";
    private static final String default_um = "pieces";
    private static final String visibilityCategory_suffix = "_visibility";
    
	private static final String IMPEX_SEPARATOR = ";";
	private static final String STRING_RETURN = "\n";
    
	private static final String impexHeader_baseProduct = "INSERT_UPDATE Product;	code[unique=true];	erpCode;	sourceSystem(code);	legacyName[lang=en];legacyName[lang=it];legacyName[lang=de];legacyName[lang=fr];	material(code);	stockable[default=true];	weight[default=0];	unit(code);	status(code);	europe1PriceFactory_PTG(code);	variantType;	approvalStatus(code)[default=check];	$catalogVersion;";
    private static final String impexHeader_baseProductCategories = "INSERT_UPDATE CategoryProductRelation; source(code,$catalogVersion)[unique=true]; target(code,$catalogVersion)[unique=true];";
    
    private static final String impexHeader_genericVariantProduct = "INSERT_UPDATE GenericVariantProduct;	code[unique=true];	erpCode;	sourceSystem(code);	legacyName[lang=en];legacyName[lang=it];legacyName[lang=de];legacyName[lang=fr];	weight[default=0];	unit(code);	europe1PriceFactory_PTG(code);	material(code);	status(code);	stockable[default=true]; baseProduct(code,$catalogVersion);	supercategories(code,$catalogVersion)[mode=append];	approvalStatus(code)[default=check];	$catalogVersion;";
    
    private static final String impexHeader_categoryRemove = "UPDATE Product; code[unique=true];	supercategories(code,$catalogVersion)[mode=remove];	$catalogVersion;";
    
	@Override
    public PerformResult perform(final CronJobModel cronJob) {
		CronJobStatus cronJobStatus;
		CronJobResult cronJobResult;

        if (cronJob instanceof BatchImportCronJobModel) {
            LOG.info("Start importProduct job. User is " + getUserService().getCurrentUser().getUid());

			final BatchImportCronJobModel batchImportCronJob = (BatchImportCronJobModel) cronJob;
			final MediaModel xmlMedia = batchImportCronJob.getImportMedia();
			final InputStream mediaStream = getMediaService().getStreamFromMedia(xmlMedia);
			final ProductsImportHybris importData = (ProductsImportHybris) getCommonWsService()
					.parseXmlStream(ProductsImportHybris.class, mediaStream);

			final CMSSiteModel site = batchImportCronJob.getSite();
            if (site == null) {
                LOG.error("No cmsSite found for BatchImportCronJob " + batchImportCronJob.getCode());
				return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
			}
	        

			final LegacySystemModel sourceSystem = batchImportCronJob.getLegacySystem();
			final CatalogVersionModel catalogVersion = site.getImportProductCatalog();
			
            StringBuilder impexMacro_stringBuilder = new StringBuilder();
            impexMacro_stringBuilder.append("$solPrefix=" + site.getUid());
            impexMacro_stringBuilder.append(STRING_RETURN);
            impexMacro_stringBuilder.append("$productCatalog=$solPrefixProductCatalog");
            impexMacro_stringBuilder.append(STRING_RETURN);
            impexMacro_stringBuilder.append("$catalogVersion=catalogversion(catalog(id[default=$productCatalog]),version[default='Staged'])[unique=true,default=$productCatalog:Staged]");
            impexMacro_stringBuilder.append(STRING_RETURN);
            
            StringBuilder impexBaseProduct_stringBuilder = new StringBuilder();
            impexBaseProduct_stringBuilder.append(impexHeader_baseProduct);
            impexBaseProduct_stringBuilder.append(STRING_RETURN);
            
            StringBuilder impexGenericVariantProdict_stringBuilder = new StringBuilder();
            impexGenericVariantProdict_stringBuilder.append(impexHeader_genericVariantProduct);
            impexGenericVariantProdict_stringBuilder.append(STRING_RETURN);
            
            StringBuilder categoryProductRelation_stringBuilder = new StringBuilder();
            categoryProductRelation_stringBuilder.append(impexHeader_baseProductCategories);
            categoryProductRelation_stringBuilder.append(STRING_RETURN);
            
            StringBuilder removeCategory_stringBuilder = new StringBuilder();
            removeCategory_stringBuilder.append(impexHeader_categoryRemove);
            removeCategory_stringBuilder.append(STRING_RETURN);
            
            LOG.info("Start importProduct job " + batchImportCronJob.getCode());

			Set<String> existingProductsCode = new HashSet<>();
			final BaseProducts baseProducts = importData.getBaseProducts();
            //Iterate over all baseProduct to retrieve existing products on the system
			for (final BaseProduct baseProduct : baseProducts.getBaseProduct()) {
				getExistingProductsCode(existingProductsCode, getProductDao().findProductsByCode(catalogVersion, getSolGroupProductService().getHybrisProductCode(baseProduct.getErpCode(), site)));

				if (baseProduct.getVariants() != null && CollectionUtils.isNotEmpty(baseProduct.getVariants().getVariant())) {
					for (final Variant variant : baseProduct.getVariants().getVariant()) {
						getExistingProductsCode(existingProductsCode, getProductDao().findProductsByCode(catalogVersion, getSolGroupProductService().getHybrisProductCode(variant.getErpCode(), site)));
					}
				}
			}

			// Iterate over all baseProducts sent in message

            for (final BaseProduct baseProduct : baseProducts.getBaseProduct()) {

            	// Create baseProduct impex row
            	createBaseProductImpexRow(baseProduct, catalogVersion, sourceSystem, site, impexBaseProduct_stringBuilder, categoryProductRelation_stringBuilder, removeCategory_stringBuilder, existingProductsCode);

				// Iterate over all variants for current baseProduct
                if (baseProduct.getVariants() != null && CollectionUtils.isNotEmpty(baseProduct.getVariants().getVariant())) {
                    for (final Variant variant : baseProduct.getVariants().getVariant()) {
                    	createVariantProductImpexRow(variant, baseProduct, catalogVersion, sourceSystem, site, impexGenericVariantProdict_stringBuilder,removeCategory_stringBuilder, existingProductsCode);
					}
				}

			} // end for to iterate over all base products
            
            
           
			StringBuilder importImpex = new StringBuilder();

			importImpex.append(impexMacro_stringBuilder.toString());
			importImpex.append(STRING_RETURN);
			importImpex.append(STRING_RETURN);
			importImpex.append(STRING_RETURN);
			

			importImpex.append(impexBaseProduct_stringBuilder.toString());
			importImpex.append(STRING_RETURN);
			importImpex.append(STRING_RETURN);
			importImpex.append(STRING_RETURN);

			importImpex.append(categoryProductRelation_stringBuilder.toString());
			importImpex.append(STRING_RETURN);
			importImpex.append(STRING_RETURN);
			importImpex.append(STRING_RETURN);

			
			importImpex.append(impexGenericVariantProdict_stringBuilder.toString());
			importImpex.append(STRING_RETURN);
			importImpex.append(STRING_RETURN);
			importImpex.append(STRING_RETURN);

			importImpex.append(removeCategory_stringBuilder.toString());
			importImpex.append(STRING_RETURN);
			importImpex.append(STRING_RETURN);
			
			

//			if(LOG.isDebugEnabled()) {
//				LOG.debug("\n" + importImpex);
//			}

			LOG.info("\n" + importImpex);

			
			try {
				
				performImpexImport(importImpex.toString(),site,importData.getHeader().getSourceSystem(),importData.getHeader().getMessageID(),batchImportCronJob.getJob().getCode());
				
				cronJobStatus = CronJobStatus.FINISHED;
				cronJobResult = CronJobResult.SUCCESS;

				
			} catch (Exception e) {
				LOG.error(e.getMessage(),e);
				cronJobStatus = CronJobStatus.FINISHED;
				cronJobResult = CronJobResult.ERROR;
			}

            
        } else {
            LOG.error("CronJob " + cronJob.getCode() + " is not an instance of BatchImportCronJobModel");
			cronJobStatus = CronJobStatus.FINISHED;
			cronJobResult = CronJobResult.ERROR;

		}

		return new PerformResult(cronJobResult, cronJobStatus);
	}

	private void getExistingProductsCode(Set<String> existingProductsCode, List<ProductModel> foundProducts) {
		if(foundProducts != null && !foundProducts.isEmpty()) {
			foundProducts.stream().map(ProductModel::getCode).collect(Collectors.toCollection(() -> existingProductsCode));
		}
	}

	private void createBaseProductImpexRow(BaseProduct baseProduct, CatalogVersionModel catalogVersion, LegacySystemModel sourceSystem, CMSSiteModel site, StringBuilder stringBuilder, StringBuilder categoryProductRelationBuilder, StringBuilder categoryRemoveBuilder, Set<String> existingProductsCode) {

		boolean baseProductExists = existingProductsCode.contains(getSolGroupProductService().getHybrisProductCode(baseProduct.getErpCode(), site));

		// Code and erpCode
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append( getSolGroupProductService().getHybrisProductCode(baseProduct.getErpCode(), site) );
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append(baseProduct.getErpCode());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Legacy system
		stringBuilder.append(sourceSystem.getCode());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Legacy names
		Map<String,String> localizedNames = Maps.newHashMap();
		for (final Name name : baseProduct.getNames().getName()) {
			localizedNames.put(name.getLang(), name.getValue());
		}
		if(StringUtils.isNotEmpty(localizedNames.get("en"))) {
			stringBuilder.append(localizedNames.get("en"));
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		if(StringUtils.isNotEmpty(localizedNames.get("it"))) {
			stringBuilder.append(localizedNames.get("it"));
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		if(StringUtils.isNotEmpty(localizedNames.get("de"))) {
			stringBuilder.append(localizedNames.get("de"));
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		if(StringUtils.isNotEmpty(localizedNames.get("fr"))) {
			stringBuilder.append(localizedNames.get("fr"));
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Material
		stringBuilder.append(baseProduct.getMaterial());
		stringBuilder.append(IMPEX_SEPARATOR);

		// Stockable
		if(BooleanUtils.isFalse(baseProduct.isStockable())) {
			stringBuilder.append("false");
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Weight
		if(baseProduct.getWeight()!=null) {
			stringBuilder.append(baseProduct.getWeight().doubleValue());
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Unit measure
		if(StringUtils.isNotEmpty(baseProduct.getUm())) {
			stringBuilder.append(baseProduct.getUm());
		}
		else {
			stringBuilder.append(default_um);
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Status
		final String productStatusCode = baseProduct.getStatus();
        if (StringUtils.isNotEmpty(productStatusCode) && productStatusCode.equals(PRODUCT_STATUS_ENABLED_CODE)) {
        	stringBuilder.append(ProductStatusEnum.ENABLED.getCode());
        } else {
        	stringBuilder.append(ProductStatusEnum.DISABLED.getCode());
		}
        stringBuilder.append(IMPEX_SEPARATOR);

		// Tax group
		ProductTaxGroup productTaxGroup = getTaxGroup(baseProduct.getTaxCode(), catalogVersion); 
		if(productTaxGroup!=null) {
			stringBuilder.append(productTaxGroup.getCode());
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Variant type
		if(baseProduct.getVariants() != null && CollectionUtils.isNotEmpty(baseProduct.getVariants().getVariant())) {
			final VariantTypeModel variantType = getCommonDao().findItemByUniqueCode(VariantTypeModel._TYPECODE,
					VariantTypeModel.CODE, GenericVariantProductModel._TYPECODE, VariantTypeModel.class);
			stringBuilder.append(variantType.getPk().toString());
		}
		stringBuilder.append(IMPEX_SEPARATOR);


		//If base product already exists on system, skip this step
		if(!baseProductExists) {

			// Categories
			Set<String> categoriesToAdd = Sets.newHashSet();
			Set<String> categoriesToRemove = Sets.newHashSet();

			Set<String> currentLegacyCategories = getSolGroupProductService().findCategoriesForProductByMaster(getSolGroupProductService().getHybrisProductCode(baseProduct.getErpCode(), site), MasterSystemEnum.LEGACY.getCode(), catalogVersion);
			Set<String> newLegacyCategories = Sets.newHashSet();

			boolean hasVisibility = false;
			if (baseProduct.getCommercialCategories() != null
					&& CollectionUtils.isNotEmpty(baseProduct.getCommercialCategories().getCommercialCategory())) {
				for (final CommercialCategory commercialCategory : baseProduct.getCommercialCategories().getCommercialCategory()) {
					if (StringUtils.isNotEmpty(commercialCategory.getCode())) {
						String hybrisCategoryCode = getSolGroupCategoryService().getHybrisCategoryCode(commercialCategory.getCode(), site);
						newLegacyCategories.add(hybrisCategoryCode);
						if (hybrisCategoryCode.endsWith(visibilityCategory_suffix)) {
							hasVisibility = true;
						}
						if (!currentLegacyCategories.contains(hybrisCategoryCode)) {
							categoriesToAdd.add(hybrisCategoryCode);
						}
					}
				}
			}

			if (hasVisibility == false) {
				String visibilityCategory_all = getSolGroupCategoryService().getHybrisCategoryCode(getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CATEGORY_VISIBILITY_ALL), site);
				newLegacyCategories.add(visibilityCategory_all);
				if (!currentLegacyCategories.contains(visibilityCategory_all)) {
					categoriesToAdd.add(visibilityCategory_all);
				}
			}

			if (baseProduct.getVariants() != null && CollectionUtils.isNotEmpty(baseProduct.getVariants().getVariant())) {
				for (Variant variant : baseProduct.getVariants().getVariant()) {
					if (variant.getVariantsCategories() != null && CollectionUtils.isNotEmpty(variant.getVariantsCategories().getVariantCategory())) {
						for (VariantCategory variantCategory : variant.getVariantsCategories().getVariantCategory()) {
							if (StringUtils.isNotEmpty(variantCategory.getVariantCategory())) {
								String hybrisCategoryCode = getSolGroupCategoryService().getHybrisCategoryCode(variantCategory.getVariantCategory(), site);
								newLegacyCategories.add(hybrisCategoryCode);
								if (!currentLegacyCategories.contains(hybrisCategoryCode)) {
									categoriesToAdd.add(hybrisCategoryCode);
								}
							}

						}
					}
				}
			}


			for (String currentCategoryCode : currentLegacyCategories) {
				if (!newLegacyCategories.contains(currentCategoryCode)) {
					categoriesToRemove.add(currentCategoryCode);
				}
			}


			if (CollectionUtils.isNotEmpty(categoriesToAdd)) {
				for (String categoryCode : categoriesToAdd) {
					categoryProductRelationBuilder.append(IMPEX_SEPARATOR);
					categoryProductRelationBuilder.append(categoryCode);
					categoryProductRelationBuilder.append(IMPEX_SEPARATOR);
					categoryProductRelationBuilder.append(getSolGroupProductService().getHybrisProductCode(baseProduct.getErpCode(), site));
					categoryProductRelationBuilder.append(IMPEX_SEPARATOR);
					categoryProductRelationBuilder.append(STRING_RETURN);
				}


//        	String categoryStr = "";
//        	for(String categoryCode : categoriesToAdd) {
//        		categoryStr = categoryStr + categoryCode + ",";
//        	}
//        	if(categoryStr.endsWith(",")) {
//        		categoryStr = categoryStr.substring(0, categoryStr.length()-1);
//        	}
//        	stringBuilder.append(categoryStr);
			}
//		stringBuilder.append(IMPEX_SEPARATOR);

			//Probably never used because existing product will not follow this flow
			if (CollectionUtils.isNotEmpty(categoriesToRemove)) {
				String categoryStr = "";
				for (String categoryCode : categoriesToRemove) {
					categoryStr = categoryStr + categoryCode + ",";
				}
				if (categoryStr.endsWith(",")) {
					categoryStr = categoryStr.substring(0, categoryStr.length() - 1);
				}
				if (!categoryStr.isEmpty()) {
					categoryRemoveBuilder.append(IMPEX_SEPARATOR);
					categoryRemoveBuilder.append(getSolGroupProductService().getHybrisProductCode(baseProduct.getErpCode(), site));
					categoryRemoveBuilder.append(IMPEX_SEPARATOR);
					categoryRemoveBuilder.append(categoryStr);
					categoryRemoveBuilder.append(IMPEX_SEPARATOR);
					categoryRemoveBuilder.append(IMPEX_SEPARATOR);
					categoryRemoveBuilder.append(STRING_RETURN);
				}
			}
		}
		
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append(STRING_RETURN);

	}
	
	private void createVariantProductImpexRow(Variant variant, BaseProduct baseProduct, CatalogVersionModel catalogVersion, LegacySystemModel sourceSystem, CMSSiteModel site, StringBuilder stringBuilder, StringBuilder categoryRemoveBuilder, Set<String> existingProductsCode) {

		boolean baseProductExists = existingProductsCode.contains(getSolGroupProductService().getHybrisProductCode(baseProduct.getErpCode(), site));

		boolean variantExists = existingProductsCode.contains(getSolGroupProductService().getHybrisProductCode(variant.getErpCode(), site));

		// Code and erpCode
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append(getSolGroupProductService().getHybrisProductCode(variant.getErpCode(), site));
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append(variant.getErpCode());
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Legacy system
		stringBuilder.append(sourceSystem.getCode());
		stringBuilder.append(IMPEX_SEPARATOR);

		// Localized legacy names
		Map<String,String> localizedNames = Maps.newHashMap();
		for (final com.solgroup.core.ws.services.product.xml.ProductsImportHybris.BaseProducts.BaseProduct.Variants.Variant.Names.Name name : variant.getNames().getName()) {
			localizedNames.put(name.getLang(), name.getValue());
		}
		if(StringUtils.isNotEmpty(localizedNames.get("en"))) {
			stringBuilder.append(localizedNames.get("en"));
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		if(StringUtils.isNotEmpty(localizedNames.get("it"))) {
			stringBuilder.append(localizedNames.get("it"));
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		if(StringUtils.isNotEmpty(localizedNames.get("de"))) {
			stringBuilder.append(localizedNames.get("de"));
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		if(StringUtils.isNotEmpty(localizedNames.get("fr"))) {
			stringBuilder.append(localizedNames.get("fr"));
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Weight
		if(variant.getWeight()!=null) {
			stringBuilder.append(variant.getWeight().doubleValue());
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Unit measure
		if(StringUtils.isNotEmpty(variant.getUm())) {
			stringBuilder.append(variant.getUm());
		}
		else if (StringUtils.isNotEmpty(baseProduct.getUm())) {
			stringBuilder.append(baseProduct.getUm());
		}
		else {
			stringBuilder.append(default_um);
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Tax group
		ProductTaxGroup productTaxGroup = getTaxGroup(variant.getTaxCode(), catalogVersion); 
		if(productTaxGroup!=null) {
			stringBuilder.append(productTaxGroup.getCode());
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Material
		stringBuilder.append(baseProduct.getMaterial());
		stringBuilder.append(IMPEX_SEPARATOR);

		
		// Status
		// Base product is disabled --> all variants all disabled
        if (!baseProduct.getStatus().equals(PRODUCT_STATUS_ENABLED_CODE)) {
        	stringBuilder.append(ProductStatusEnum.DISABLED.getCode());
		}
		// Base product is enabled --> variant status read from legacy
        else {
    		final String productStatusCode = variant.getStatus();
            if (StringUtils.isNotEmpty(productStatusCode) && productStatusCode.equals(PRODUCT_STATUS_ENABLED_CODE)) {
            	stringBuilder.append(ProductStatusEnum.ENABLED.getCode());
            } else {
            	stringBuilder.append(ProductStatusEnum.DISABLED.getCode());
    		}
		}
		stringBuilder.append(IMPEX_SEPARATOR);
		
		// Stockable
		if(BooleanUtils.isFalse(baseProduct.isStockable())) {
			stringBuilder.append("false");
		}
		stringBuilder.append(IMPEX_SEPARATOR);

		// Base product
		stringBuilder.append(getSolGroupProductService().getHybrisProductCode(baseProduct.getErpCode(), site));
		stringBuilder.append(IMPEX_SEPARATOR);

		if(!baseProductExists && !variantExists) {

			// Variant value categories
			List<String> categoriesToAdd = Lists.newArrayList();
			List<String> categoriesToRemove = Lists.newArrayList();

			Set<String> currentLegacyCategories = getSolGroupProductService().findCategoriesForProductByMaster(getSolGroupProductService().getHybrisProductCode(variant.getErpCode(), site), MasterSystemEnum.LEGACY.getCode(), catalogVersion);
			Set<String> newLegacyCategories = Sets.newHashSet();

			for (VariantCategory variantCategory : variant.getVariantsCategories().getVariantCategory()) {
				if (StringUtils.isNotEmpty(variantCategory.getVariantValueCategory())) {
					String hybrisCategoryCode = getSolGroupCategoryService().getHybrisCategoryCode(variantCategory.getVariantValueCategory(), site);
					newLegacyCategories.add(hybrisCategoryCode);
					if (!currentLegacyCategories.contains(hybrisCategoryCode)) {
						categoriesToAdd.add(hybrisCategoryCode);
					}
				}
			}

			for (String currentCategoryCode : currentLegacyCategories) {
				if (!newLegacyCategories.contains(currentCategoryCode)) {
					categoriesToRemove.add(currentCategoryCode);
				}
			}

			if (CollectionUtils.isNotEmpty(categoriesToAdd)) {
				String categoryStr = "";
				for (String categoryCode : categoriesToAdd) {
					categoryStr = categoryStr + categoryCode + ",";
				}
				if (categoryStr.endsWith(",")) {
					categoryStr = categoryStr.substring(0, categoryStr.length() - 1);
				}
				stringBuilder.append(categoryStr);
			}
			stringBuilder.append(IMPEX_SEPARATOR);

			if (CollectionUtils.isNotEmpty(categoriesToRemove)) {
				String categoryStr = "";
				for (String categoryCode : categoriesToRemove) {
					categoryStr = categoryStr + categoryCode + ",";
				}
				if (categoryStr.endsWith(",")) {
					categoryStr = categoryStr.substring(0, categoryStr.length() - 1);
				}

				if (!categoryStr.isEmpty()) {
					categoryRemoveBuilder.append(IMPEX_SEPARATOR);
					categoryRemoveBuilder.append(getSolGroupProductService().getHybrisProductCode(variant.getErpCode(), site));
					categoryRemoveBuilder.append(IMPEX_SEPARATOR);
					categoryRemoveBuilder.append(categoryStr);
					categoryRemoveBuilder.append(IMPEX_SEPARATOR);
					categoryRemoveBuilder.append(IMPEX_SEPARATOR);
					categoryRemoveBuilder.append(STRING_RETURN);
				}

			}
		}else if(baseProductExists && !variantExists){
			//In this case we have to manage variant value category assignment
			// Variant value categories
			List<String> categoriesToAdd = Lists.newArrayList();

			Set<CategoryModel> baseProductCurrentCategories = getSolGroupProductService().findCategoriesObjectForProductByMaster(getSolGroupProductService().getHybrisProductCode(baseProduct.getErpCode(), site), MasterSystemEnum.LEGACY.getCode(), catalogVersion);

			boolean variantValueCategoryFound = false;
			//For each variant category a random variant value category is set
			for (CategoryModel category : baseProductCurrentCategories) {
				if (category instanceof VariantCategoryModel) {
					variantValueCategoryFound = false;
					//Retrieve a default VariantCategoryValue for current VariantCategory
					Collection<CategoryModel> categorySubCategories = category.getAllSubcategories();
					if(categorySubCategories != null && !categorySubCategories.isEmpty()){
						Iterator<CategoryModel> categorySubCategoriesIterator = categorySubCategories.iterator();
						while(categorySubCategoriesIterator.hasNext() && !variantValueCategoryFound){
							CategoryModel categorySubCategory =  categorySubCategoriesIterator.next();
							if (categorySubCategory instanceof VariantValueCategoryModel) {
								//The first VariantValueCategory found is inserted as variant superclass
								categoriesToAdd.add(categorySubCategory.getCode());
								variantValueCategoryFound = true;
							}

						}
					}
				}
			}

			if (CollectionUtils.isNotEmpty(categoriesToAdd)) {
				String categoryStr = "";
				for (String categoryCode : categoriesToAdd) {
					categoryStr = categoryStr + categoryCode + ",";
				}
				if (categoryStr.endsWith(",")) {
					categoryStr = categoryStr.substring(0, categoryStr.length() - 1);
				}
				stringBuilder.append(categoryStr);
			}
			stringBuilder.append(IMPEX_SEPARATOR);
		}

		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append(IMPEX_SEPARATOR);
		stringBuilder.append(STRING_RETURN);
		
	}








    private ProductTaxGroup getTaxGroup(final String taxCode, final CatalogVersionModel catalogVersion) {
       
    	
    	ProductTaxGroup productTaxGroup = null;
    	
        if (StringUtils.isNotEmpty(taxCode)) {
            final List<TaxRowModel> taxRows = getSolGroupTaxRowDao().findTaxRowsByCode(taxCode, catalogVersion);
            
            if (CollectionUtils.isNotEmpty(taxRows) && taxRows.get(0).getPg() != null) {
                productTaxGroup = (ProductTaxGroup) taxRows.get(0).getPg();
            }
        } else {
            final List<TaxRowModel> taxRows = getSolGroupTaxRowDao().findTaxRowsByCode(null, catalogVersion);
            for (TaxRowModel taxRowModel : taxRows) {
                if (BooleanUtils.isTrue(taxRowModel.getDefaultTax())) {
                	productTaxGroup = (ProductTaxGroup)taxRowModel.getPg();
                    break;
                }
            }
        }

        if(productTaxGroup==null) {
        	LOG.warn(String.format("No TAX ROW found for code [%s]",taxCode));
        }

        return productTaxGroup;
    
    }

    
    
    protected ProductDao getProductDao() {
		return productDao;
	}

	@Required
    public void setProductDao(final ProductDao productDao) {
		this.productDao = productDao;
	}

    protected SolGroupTaxRowDao getSolGroupTaxRowDao() {
        return solGroupTaxRowDao;
    }

    @Required
    public void setSolGroupTaxRowDao(SolGroupTaxRowDao solGroupTaxRowDao) {
        this.solGroupTaxRowDao = solGroupTaxRowDao;
    }

	protected SolGroupProductService getSolGroupProductService() {
		return solGroupProductService;
	}

	@Required
	public void setSolGroupProductService(SolGroupProductService solGroupProductService) {
		this.solGroupProductService = solGroupProductService;
	}

	protected SolGroupCategoryService getSolGroupCategoryService() {
		return solGroupCategoryService;
	}

	@Required
	public void setSolGroupCategoryService(SolGroupCategoryService solGroupCategoryService) {
		this.solGroupCategoryService = solGroupCategoryService;
	}

	protected ConfigurationService getConfigurationService() {
		return configurationService;
	}

	@Required
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
	
	
    
    


















}
