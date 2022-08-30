package com.solgroup.facades.order.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.service.b2bcustomer.impl.DefaultSolGroupCustomerAccountService;
import com.solgroup.facades.order.SolGroupOrderFacade;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderHistoryData;
import de.hybris.platform.commercefacades.order.impl.DefaultOrderFacade;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.store.BaseStoreModel;

/**
 * @author fmilazzo
 *
 */
public class DefaultSolGroupOrderFacade extends DefaultOrderFacade implements SolGroupOrderFacade {

    private static final String ORDER_NOT_FOUND_FOR_B2BUNIT_AND_BASE_STORE = "Order with guid %s not found for B2BUnit in current BaseStore";

    private SessionService sessionService;
    private DefaultSolGroupCustomerAccountService customerAccountService;

    @Override
    public SearchPageData<OrderHistoryData> getPagedOrderHistoryForStatuses(final String orderCode,
            final Date orderDate, final PageableData pageableData,
            final OrderStatus status) {
        Set<B2BUnitModel> b2bUnitsSet = getSessionService().getAttribute(SolgroupCoreConstants.SESSION_PARAM_B2BUNITS_BRANCH_SET);
        final BaseStoreModel currentBaseStore = getBaseStoreService().getCurrentBaseStore();
        final SearchPageData<OrderModel> orderResults = getCustomerAccountService().getOrderList(orderCode, orderDate,
                b2bUnitsSet, currentBaseStore, status, pageableData);
        return convertPageData(orderResults, getOrderHistoryConverter());
    }

    @Override
    public List<OrderStatus> getOrderStatusList() {
        return Arrays.asList(OrderStatus.CREATED, OrderStatus.IN_PROGRESS, OrderStatus.PARTIALLY_DELIVERED,
                OrderStatus.DELIVERED, OrderStatus.CANCELLED);
    }

    @Override
    public OrderData getOrderDetailsForCode(final String code) {
        final BaseStoreModel baseStoreModel = getBaseStoreService().getCurrentBaseStore();

        OrderModel orderModel = null;
        if (getCheckoutCustomerStrategy().isAnonymousCheckout()) {
            orderModel = getCustomerAccountService().getOrderDetailsForGUID(code, baseStoreModel);
        } else {
            try {
                Set<B2BUnitModel> b2bUnitsSet = getSessionService()
                        .getAttribute(SolgroupCoreConstants.SESSION_PARAM_B2BUNITS_BRANCH_SET);
                orderModel = getCustomerAccountService().getOrderForCode(b2bUnitsSet, code, baseStoreModel);
            } catch (final ModelNotFoundException e) {
                throw new UnknownIdentifierException(String.format(ORDER_NOT_FOUND_FOR_B2BUNIT_AND_BASE_STORE, code));
            }
        }

        if (orderModel == null) {
            throw new UnknownIdentifierException(String.format(ORDER_NOT_FOUND_FOR_B2BUNIT_AND_BASE_STORE, code));
        }
        return getOrderConverter().convert(orderModel);
    }

    protected SessionService getSessionService() {
        return sessionService;
    }

    @Required
    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    protected DefaultSolGroupCustomerAccountService getCustomerAccountService() {
        return customerAccountService;
    }

    @Required
    public void setCustomerAccountService(DefaultSolGroupCustomerAccountService customerAccountService) {
        this.customerAccountService = customerAccountService;
    }

}
