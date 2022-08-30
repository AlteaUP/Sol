package com.solgroup.core.dao.b2bcustomer;

import java.util.Date;
import java.util.Set;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.store.BaseStoreModel;

/**
 * @author fmilazzo
 *
 */
public interface SolGroupB2BCustomerAccountDao {

    SearchPageData<OrderModel> findOrdersByB2BUnitAndStore(final String orderCode, final Date orderDate,
            final Set<B2BUnitModel> b2bUnitsSet, final BaseStoreModel store, final OrderStatus status,
            final PageableData pageableData);

    OrderModel findOrderByCustomerAndCodeAndStore(final Set<B2BUnitModel> b2bUnitsSet, final String code,
            final BaseStoreModel store);

}
