package com.solgroup.core.dao.b2bcustomer.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.dao.b2bcustomer.SolGroupB2BCustomerAccountDao;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commerceservices.search.flexiblesearch.PagedFlexibleSearchService;
import de.hybris.platform.commerceservices.search.flexiblesearch.data.SortQueryData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.store.BaseStoreModel;

/**
 * @author fmilazzo
 *
 */
public class DefaultSolGroupB2BCustomerAccountDao extends AbstractItemDao implements SolGroupB2BCustomerAccountDao {

    private static final String FIND_ORDERS_BY_B2BUNITS_CODE_STORE_QUERY = "SELECT {" + OrderModel.PK + "}, {"
            + OrderModel.CREATIONTIME + "}, {" + OrderModel.CODE + "} FROM {" + OrderModel._TYPECODE + "} WHERE {"
            + OrderModel.CODE + "} = ?code AND {" + OrderModel.VERSIONID + "} IS NULL AND {" + OrderModel.UNIT
            + "} IN (?b2bUnitsSet) AND {" + OrderModel.STORE + "} = ?store";

    private static final String FIND_ORDERS_BY_B2BUNITS_STORE_QUERY = "SELECT {" + OrderModel.PK + "}, {"
            + OrderModel.CREATIONTIME + "}, {" + OrderModel.CODE + "} FROM {" + OrderModel._TYPECODE + "} WHERE {"
            + OrderModel.UNIT + "} IN (?b2bUnitsSet) AND {" + OrderModel.VERSIONID + "} IS NULL AND {"
            + OrderModel.STORE
            + "} = ?store";

    private static final String FILTER_ORDER_STATUS = " AND {" + OrderModel.STATUS + "} = ?filterOrderStatus";

    // private static final String FILTER_ORDER_CODE = " AND {" + OrderModel.CODE + "} LIKE %?filterOrderCode%";

    private static final String FILTER_ORDER_DATE = " AND {" + OrderModel.DATE + "} >= ?filterOrderDate";

    private static final String SORT_ORDERS_BY_DATE = " ORDER BY {" + OrderModel.CREATIONTIME + "} DESC, {"
            + OrderModel.PK + "}";

    private static final String SORT_ORDERS_BY_CODE = " ORDER BY {" + OrderModel.CODE + "},{" + OrderModel.CREATIONTIME
            + "} DESC, {" + OrderModel.PK + "}";

    private PagedFlexibleSearchService pagedFlexibleSearchService;

    @Override
    public SearchPageData<OrderModel> findOrdersByB2BUnitAndStore(final String orderCode, final Date orderDate,
            final Set<B2BUnitModel> b2bUnitsSet,
            final BaseStoreModel store, final OrderStatus status, final PageableData pageableData) {
        validateParameterNotNull(b2bUnitsSet, "B2BUnitsSet must not be null");
        validateParameterNotNull(store, "Store must not be null");

        final Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("b2bUnitsSet", b2bUnitsSet);
        queryParams.put("store", store);

        final StringBuilder filterClause = new StringBuilder();
        filterClause.append(StringUtils.EMPTY);

        if (status != null) {
            filterClause.append(FILTER_ORDER_STATUS);
            queryParams.put("filterOrderStatus", status);
        }

        if (StringUtils.isNotEmpty(orderCode)) {
            filterClause.append(" AND {" + OrderModel.CODE + "} LIKE '%" + orderCode + "%'");
            // queryParams.put("filterOrderCode", orderCode);
        }

        if (orderDate != null) {
            filterClause.append(FILTER_ORDER_DATE);
            queryParams.put("filterOrderDate", orderDate);
        }

        final List<SortQueryData> sortQueries = Arrays.asList(
                createSortQueryData("byDate",
                        createQuery(FIND_ORDERS_BY_B2BUNITS_STORE_QUERY, filterClause.toString(),
                                SORT_ORDERS_BY_DATE)),
                createSortQueryData("byOrderNumber", createQuery(FIND_ORDERS_BY_B2BUNITS_STORE_QUERY,
                        filterClause.toString(), SORT_ORDERS_BY_CODE)));

        return getPagedFlexibleSearchService().search(sortQueries, "byDate", queryParams, pageableData);
    }

    @Override
    public OrderModel findOrderByCustomerAndCodeAndStore(final Set<B2BUnitModel> b2bUnitsSet, final String code,
            final BaseStoreModel store) {
        validateParameterNotNull(b2bUnitsSet, "B2bUnits set must not be null");
        validateParameterNotNull(code, "Code must not be null");
        validateParameterNotNull(store, "Store must not be null");
        final Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("b2bUnitsSet", b2bUnitsSet);
        queryParams.put("code", code);
        queryParams.put("store", store);
        final OrderModel result = getFlexibleSearchService()
                .searchUnique(new FlexibleSearchQuery(FIND_ORDERS_BY_B2BUNITS_CODE_STORE_QUERY, queryParams));
        return result;
    }

    protected SortQueryData createSortQueryData(final String sortCode, final String query) {
        final SortQueryData result = new SortQueryData();
        result.setSortCode(sortCode);
        result.setQuery(query);
        return result;
    }

    protected String createQuery(final String... queryClauses) {
        final StringBuilder queryBuilder = new StringBuilder();

        for (final String queryClause : queryClauses) {
            queryBuilder.append(queryClause);
        }

        return queryBuilder.toString();
    }

    protected PagedFlexibleSearchService getPagedFlexibleSearchService() {
        return pagedFlexibleSearchService;
    }

    @Required
    public void setPagedFlexibleSearchService(PagedFlexibleSearchService pagedFlexibleSearchService) {
        this.pagedFlexibleSearchService = pagedFlexibleSearchService;
    }

}
