package com.solgroup.facades.order;

import java.util.Date;
import java.util.List;

import de.hybris.platform.commercefacades.order.OrderFacade;
import de.hybris.platform.commercefacades.order.data.OrderHistoryData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;

/**
 * @author fmilazzo
 *
 */
public interface SolGroupOrderFacade extends OrderFacade {

    SearchPageData<OrderHistoryData> getPagedOrderHistoryForStatuses(final String orderCode, final Date orderDate,
            final PageableData pageableData, final OrderStatus statuses);

    List<OrderStatus> getOrderStatusList();

}