package com.solgroup.facades.product.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.product.data.VariantMatrixElementData;
import de.hybris.platform.commercefacades.product.impl.DefaultProductVariantFacade;
import de.hybris.platform.core.model.product.ProductModel;

public class DefaultSolGroupProductVariantFacade extends DefaultProductVariantFacade {

    @SuppressWarnings("deprecation")
    public ProductData getProductForCodeAndOptions(final String code, final Collection<ProductOption> options,
            CartData cartData) {
        final ProductModel productModel = getProductService().getProductForCode(code);
        final ProductData productData = getProductForOptions(productModel, options);
        List<VariantMatrixElementData> variantMatrixElementDataList = new ArrayList<VariantMatrixElementData>();
        for (OrderEntryData baseProductData : cartData.getEntries()) {
            if (code.equals(baseProductData.getProduct().getCode())) {
                for (OrderEntryData variantData : baseProductData.getEntries()) {
                    for (VariantMatrixElementData variantMatrixElementData : productData.getVariantMatrix()) {
                        if (variantData.getProduct().getCode()
                                .equals(variantMatrixElementData.getVariantOption().getCode())) {
                            variantMatrixElementDataList.add(variantMatrixElementData);
                        }
                    }
                }
            }
        }
        productData.setVariantMatrix(variantMatrixElementDataList);
        return productData;
    }

    @SuppressWarnings("deprecation")
    public ProductData getProductForCodeAndOptionsForOrderForm(final String code,
            final Collection<ProductOption> options) {

        final ProductModel productModel = getProductService().getProductForCode(code);
        final ProductData productData = getProductForOptions(productModel, options);

        if (options.contains(ProductOption.VARIANT_MATRIX_EXTENDED)) {
            if (productData.getMultidimensional().booleanValue()) {
                List<VariantMatrixElementData> variantMatrixElementDataList = new ArrayList<VariantMatrixElementData>();
                for (VariantMatrixElementData variantMatrixElementData : productData.getVariantMatrix()) {
                    List<VariantMatrixElementData> elements = variantMatrixElementData.getElements();
                    if (elements.size() > 0) {
                        VariantMatrixElementData element = elements.get(0);
                        while (!element.getIsLeaf()) {
                            element = element.getElements().get(0);
                        }
                        if (element.getVariantOption().getPriceData() != null
                                && !element.getVariantOption().getPriceData().getValue().equals(BigDecimal.ZERO)) {
                            variantMatrixElementDataList.add(variantMatrixElementData);
                        }
                    }
                }
                productData.setVariantMatrix(variantMatrixElementDataList);
            }
        }
        return productData;
    }

}
