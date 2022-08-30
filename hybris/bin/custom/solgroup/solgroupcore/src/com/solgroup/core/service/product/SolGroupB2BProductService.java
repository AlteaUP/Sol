package com.solgroup.core.service.product;

import java.util.Collection;

import de.hybris.platform.b2bacceleratorservices.product.B2BProductService;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.product.ProductModel;

public interface SolGroupB2BProductService extends B2BProductService{

	public SearchPageData<ProductModel> getProductsForSkus(final Collection<String> skus, final PageableData pageableData);

}
