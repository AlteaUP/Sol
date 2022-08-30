package com.solgroup.commercefacades.product.converters.populator;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.commercefacades.product.converters.populator.ProductVariantMatrixPopulator;
import de.hybris.platform.commercefacades.product.data.StockData;
import de.hybris.platform.commercefacades.product.data.VariantMatrixElementData;
import de.hybris.platform.commerceservices.stock.CommerceStockService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.variants.model.VariantProductModel;
import de.hybris.platform.variants.model.VariantValueCategoryModel;

public class DefaultSolGroupProductVariantMatrixPopulator extends ProductVariantMatrixPopulator {


	private CommerceStockService commerceStockService;
	private BaseStoreService baseStoreService;

	private static final long MAX_STOCK_AVAILABLE_QTY = 9999;

	@Override
	protected void createNodesForVariant(final VariantProductModel variant,
			VariantMatrixElementData currentParentNode) {
		final List<VariantValueCategoryModel> valuesCategories = getVariantValuesCategories(variant);
		for (final VariantValueCategoryModel valueCategory : valuesCategories) {
			final VariantMatrixElementData existingNode = getExistingNode(currentParentNode, valueCategory);

			if (existingNode == null) {
				final VariantMatrixElementData createdNode = createNode(currentParentNode, valueCategory);
				createdNode.getVariantOption().setCode(variant.getCode());
				createdNode.getVariantOption().setErpCode(variant.getErpCode());
				createdNode.getVariantOption().setRefill(BooleanUtils.isTrue(variant.getRefill()));
				currentParentNode = createdNode;
				
				StockData stockData = new StockData();
				final BaseStoreModel baseStore = getBaseStoreService().getCurrentBaseStore();
				if (!isStockSystemEnabled(baseStore)) {
					stockData.setStockLevelStatus(StockLevelStatus.INSTOCK);
					stockData.setStockLevel(Long.valueOf(0));
				} else {
					Long stockLevel = getCommerceStockService().getStockLevelForProductAndBaseStore(variant, baseStore);
					if (stockLevel == null) {
						stockLevel = new Long(MAX_STOCK_AVAILABLE_QTY);
					}
					stockData.setStockLevel(stockLevel);
					stockData.setStockLevelStatus(
							getCommerceStockService().getStockLevelStatusForProductAndBaseStore(variant, baseStore));
				}
				createdNode.getVariantOption().setStock(stockData);
			} else {
				currentParentNode = existingNode;
			}
		}
	}
	
	
	
	
	
	protected boolean isStockSystemEnabled(final BaseStoreModel baseStore) {
		return getCommerceStockService().isStockSystemEnabled(baseStore);
	}

	protected CommerceStockService getCommerceStockService() {
		return commerceStockService;
	}

	@Required
	public void setCommerceStockService(CommerceStockService commerceStockService) {
		this.commerceStockService = commerceStockService;
	}

	protected BaseStoreService getBaseStoreService() {
		return baseStoreService;
	}

	@Required
	public void setBaseStoreService(BaseStoreService baseStoreService) {
		this.baseStoreService = baseStoreService;
	}

}
