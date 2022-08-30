package com.solgroup.commercefacades.product.converters.populator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.commercefacades.product.converters.populator.ProductVariantMatrixPopulator;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.product.data.StockData;
import de.hybris.platform.commercefacades.product.data.VariantMatrixElementData;
import de.hybris.platform.commerceservices.stock.CommerceStockService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.variants.model.VariantProductModel;
import de.hybris.platform.variants.model.VariantValueCategoryModel;

public class DefaultSolGroupProductVariantMatrix4CartPopulator extends ProductVariantMatrixPopulator {

    private CommerceStockService commerceStockService;
    private BaseStoreService baseStoreService;

    private static final long MAX_STOCK_AVAILABLE_QTY = 9999;

    @Override
    public void populate(final ProductModel productModel, final ProductData productData) throws ConversionException {
        final Collection<VariantProductModel> variants = getVariants(productModel);
        productData.setMultidimensional(Boolean.valueOf(CollectionUtils.isNotEmpty(variants)));
        if (productData.getMultidimensional().booleanValue()) {
            final VariantMatrixElementData node = createNode();
            for (final VariantProductModel variant : variants) {
                createNodesForVariant(variant, node);
            }
            productData.setVariantMatrix(node.getElements());
        }
    }

    @Override
    protected void createNodesForVariant(final VariantProductModel variant,
            VariantMatrixElementData currentParentNode) {
        final List<VariantValueCategoryModel> valuesCategories = getVariantValuesCategories(variant);
        for (final VariantValueCategoryModel valueCategory : valuesCategories) {
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
        }
    }

    protected VariantMatrixElementData createNode() {
        final VariantMatrixElementData elementData = new VariantMatrixElementData();
        elementData.setElements(new ArrayList<VariantMatrixElementData>());
        return elementData;
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
