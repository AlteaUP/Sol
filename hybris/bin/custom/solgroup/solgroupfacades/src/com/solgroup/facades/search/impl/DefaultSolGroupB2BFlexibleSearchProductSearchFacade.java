package com.solgroup.facades.search.impl;

import java.math.BigDecimal;
import java.util.List;

import de.hybris.platform.b2bacceleratorfacades.search.data.ProductSearchStateData;
import de.hybris.platform.b2bacceleratorfacades.search.impl.DefaultB2BFlexibleSearchProductSearchFacade;
import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.product.data.VariantMatrixElementData;
import de.hybris.platform.commercefacades.search.data.SearchStateData;
import de.hybris.platform.commerceservices.search.facetdata.ProductSearchPageData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;

/**
 * 
 * @author fmilazzo
 *
 * @param <ITEM>
 */
public class DefaultSolGroupB2BFlexibleSearchProductSearchFacade<ITEM extends ProductData>
        extends DefaultB2BFlexibleSearchProductSearchFacade<ProductData> {

    @Override
    public ProductSearchPageData<SearchStateData, ProductData> search(final ProductSearchStateData searchState,
            final PageableData pageableData) {
        ProductSearchPageData<SearchStateData, ProductData> productSearchPagaData = super.search(searchState,
                pageableData);
        for (ProductData productData : productSearchPagaData.getResults()) {
            productData.setShowSearchedBaseProduct(Boolean.FALSE);
            if (productData.getMultidimensional().booleanValue()) {
                for (VariantMatrixElementData variantMatrixElementData : productData.getVariantMatrix()) {
                    variantMatrixElementData.setShowSearchedVariant(Boolean.FALSE);
                    List<VariantMatrixElementData> elements = variantMatrixElementData.getElements();
                    if (elements.size() > 0) {
                        VariantMatrixElementData element = elements.get(0);
                        while (!element.getIsLeaf()) {
                            element = element.getElements().get(0);
                        }
                        if (element.getVariantOption().getPriceData() != null
                                && !BigDecimal.ZERO.equals(element.getVariantOption().getPriceData().getValue())) {
                            if (StockLevelStatus.INSTOCK
                                    .equals(element.getVariantOption().getStock().getStockLevelStatus())) {
                                variantMatrixElementData.setShowSearchedVariant(Boolean.TRUE);
                            }
                        }
                    }
                }
            } else {
                if (productData.getPrice() != null && !BigDecimal.ZERO.equals(productData.getPrice().getValue())) {
                    if (StockLevelStatus.INSTOCK.equals(productData.getStock().getStockLevelStatus())) {
                        productData.setShowSearchedBaseProduct(Boolean.TRUE);
                    }
                }
            }
        }
        return productSearchPagaData;
    }
}
