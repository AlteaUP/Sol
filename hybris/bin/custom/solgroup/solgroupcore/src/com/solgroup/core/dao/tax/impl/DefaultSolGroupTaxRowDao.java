package com.solgroup.core.dao.tax.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.solgroup.core.dao.tax.SolGroupTaxRowDao;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.order.price.TaxModel;
import de.hybris.platform.europe1.model.TaxRowModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

/**
 * @author fmilazzo
 *
 */
public class DefaultSolGroupTaxRowDao extends AbstractItemDao implements SolGroupTaxRowDao {

    @Override
    public List<TaxRowModel> findTaxRowsByCode(final String code, final CatalogVersionModel catalogVersion) {
        final SearchResult<TaxRowModel> searchResult = getFlexibleSearchService()
                .search(getTaxRowsSearchQuery(code, catalogVersion));
        return searchResult.getResult();
    }

    private FlexibleSearchQuery getTaxRowsSearchQuery(final String code, final CatalogVersionModel catalogVersion) {
        final StringBuilder builder = new StringBuilder();
        final Map<String, Object> params = new HashMap<String, Object>();
        builder.append("SELECT ");
        builder.append("{tr:pk} ");
        builder.append("FROM {TaxRow AS tr JOIN Tax AS t ON {tr:tax} = {t:pk}} ");
        builder.append("WHERE {tr:catalogVersion}=?catalogVersion ");
        if (StringUtils.isNotEmpty(code)) {
            builder.append("AND {t:code}=?code ");
            params.put(TaxModel.CODE, code);
        }
        params.put(TaxRowModel.CATALOGVERSION, catalogVersion);
        final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(builder.toString());
        searchQuery.addQueryParameters(params);
        return searchQuery;
    }
}
