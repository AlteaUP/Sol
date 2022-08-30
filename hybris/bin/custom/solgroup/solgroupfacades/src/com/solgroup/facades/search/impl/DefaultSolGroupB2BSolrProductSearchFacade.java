package com.solgroup.facades.search.impl;

import java.math.BigDecimal;
import java.util.List;

import de.hybris.platform.b2bacceleratorfacades.search.data.ProductSearchStateData;
import de.hybris.platform.b2bacceleratorfacades.search.impl.DefaultB2BSolrProductSearchFacade;
import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.product.data.VariantMatrixElementData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;

/**
 * 
 * @author fmilazzo
 *
 */
@SuppressWarnings("rawtypes")
public class DefaultSolGroupB2BSolrProductSearchFacade extends DefaultB2BSolrProductSearchFacade {

    @SuppressWarnings("unchecked")
    @Override
    public SearchPageData<ProductData> search(final ProductSearchStateData searchState,
            final PageableData pageableData) {
        SearchPageData<ProductData> searchPageData = super.search(searchState, pageableData);
        for (ProductData productData : searchPageData.getResults()) {
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
        return searchPageData;
    }

}
