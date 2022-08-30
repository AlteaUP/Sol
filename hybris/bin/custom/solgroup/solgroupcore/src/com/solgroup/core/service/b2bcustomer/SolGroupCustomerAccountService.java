package com.solgroup.core.service.b2bcustomer;

import java.util.Date;
import java.util.Set;

import de.hybris.platform.b2b.model.B2BUnitModel;

/*
 * mdc
 */

import de.hybris.platform.b2bacceleratorservices.customer.B2BCustomerAccountService;
import de.hybris.platform.commerceservices.customer.TokenInvalidatedException;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.store.BaseStoreModel;

public interface SolGroupCustomerAccountService extends B2BCustomerAccountService {

    void identityConfirmation_createToken(CustomerModel customerModel);

    void identityConfirmation_createPassword(final String token, final String newPassword)
            throws TokenInvalidatedException;

    boolean validateToken(String token) throws TokenInvalidatedException, IllegalArgumentException;

    SearchPageData<OrderModel> getOrderList(final String orderCode, final Date orderDate,
            final Set<B2BUnitModel> b2bUnitsSetModel, final BaseStoreModel store, final OrderStatus status,
            final PageableData pageableData);

    OrderModel getOrderForCode(final Set<B2BUnitModel> b2bUnitsSetModel, final String code, final BaseStoreModel store);
}
