package com.solgroup.core.product.dao.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.solgroup.core.product.dao.SolGroupPagedB2BProductDao;

import de.hybris.platform.b2bacceleratorservices.dao.PagedB2BProductDao;
import de.hybris.platform.b2bacceleratorservices.dao.impl.DefaultPagedB2BProductDao;
import de.hybris.platform.commerceservices.search.dao.impl.DefaultPagedGenericDao;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.product.ProductModel;

public class DefaultSolGroupPagedB2BProductDao extends DefaultPagedB2BProductDao implements SolGroupPagedB2BProductDao<ProductModel>
{
	private static final String FIND_PRODUCTS_BY_SKUS = "SELECT DISTINCT query.PK FROM " + "(" + "   {{"
			+ "       SELECT {PK} AS PK FROM {Product!} WHERE {erpCode} IN (?skulist) " + "   }}" + "   UNION ALL " + "   {{"
			+ "       SELECT {p:PK} AS PK FROM {GenericVariantProduct AS v JOIN Product AS p ON {v:baseproduct} = {p:PK} } WHERE {v:erpCode} IN (?skulist) "
			+ "   }}" + ") query";

	public DefaultSolGroupPagedB2BProductDao(final String typeCode)
	{
		super(typeCode);
	}

	@Override
	public SearchPageData<ProductModel> findPagedProductsForSkus(final Collection<String> skus, final PageableData pageableData)
	{
		final Map<String, Object> params = new HashMap<String, Object>(1);
		params.put("skulist", skus);

		return getPagedFlexibleSearchService().search(FIND_PRODUCTS_BY_SKUS, params, pageableData);
	}
}