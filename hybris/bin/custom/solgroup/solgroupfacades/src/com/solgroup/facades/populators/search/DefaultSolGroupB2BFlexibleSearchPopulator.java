package com.solgroup.facades.populators.search;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.b2bacceleratorfacades.search.converters.impl.DefaultB2BFlexibleSearchPopulator;
import de.hybris.platform.commercefacades.converter.ConfigurablePopulator;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.product.ProductModel;

public class DefaultSolGroupB2BFlexibleSearchPopulator extends DefaultB2BFlexibleSearchPopulator {

    // private Collection<ProductOption> productBasicSearchOptions;

    private ConfigurablePopulator<ProductModel, ProductData, ProductOption> productConfiguredPopulator;

    @Override
    protected List<ProductData> getProductDataList(final SearchPageData searchPageData) {

        final List<ProductModel> productModelList = searchPageData.getResults();
        final List<ProductData> productDataList = new ArrayList<>(productModelList.size());

        for (final ProductModel productModel : productModelList) {
            final ProductData productData = new ProductData();

            if (productConfiguredPopulator != null && productModel != null) {
                productData.setErpCode(productModel.getErpCode());
                productData.setCode(productModel.getCode());
                productConfiguredPopulator.populate(productModel, productData, getProductBasicSearchOptions());
            }

            productDataList.add(productData);
        }

        return productDataList;
    }

    protected ConfigurablePopulator<ProductModel, ProductData, ProductOption> getProductConfiguredPopulator() {
        return productConfiguredPopulator;
    }

    @Override
    @Required
    public void setProductConfiguredPopulator(
            ConfigurablePopulator<ProductModel, ProductData, ProductOption> productConfiguredPopulator) {
        this.productConfiguredPopulator = productConfiguredPopulator;
    }

}
