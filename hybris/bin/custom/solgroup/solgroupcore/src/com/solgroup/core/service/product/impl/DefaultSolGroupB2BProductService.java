package com.solgroup.core.service.product.impl;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.product.SolGroupB2BProductService;

import de.hybris.platform.b2bacceleratorservices.dao.PagedB2BProductDao;
import de.hybris.platform.b2bacceleratorservices.product.impl.DefaultB2BProductService;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.product.ProductModel;

public class DefaultSolGroupB2BProductService extends DefaultB2BProductService implements SolGroupB2BProductService{

	
	private PagedB2BProductDao pagedB2BProductDao;
	
	@Override
	public SearchPageData<ProductModel> getProductsForSkus(final Collection<String> skus, final PageableData pageableData)
	{
		return getPagedB2BProductDao().findPagedProductsForSkus(skus, pageableData);
	}
	
	@Required
	public void setPagedB2BProductDao(final PagedB2BProductDao<ProductModel> pagedB2BProductDao)
	{
		this.pagedB2BProductDao = pagedB2BProductDao;
	}

	protected PagedB2BProductDao<ProductModel> getPagedB2BProductDao()
	{
		return pagedB2BProductDao;
	}
}
