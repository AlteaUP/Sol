package com.solgroup.core.service.b2bcustomer.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
/*
 * mdc
 */
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import com.solgroup.core.dao.b2bcustomer.impl.DefaultSolGroupB2BCustomerAccountDao;
import com.solgroup.core.service.b2bcustomer.SolGroupCustomerAccountService;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2bacceleratorservices.customer.impl.DefaultB2BCustomerAccountService;
import de.hybris.platform.commerceservices.customer.TokenInvalidatedException;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.commerceservices.security.SecureToken;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.store.BaseStoreModel;

public class DefaultSolGroupCustomerAccountService extends DefaultB2BCustomerAccountService implements SolGroupCustomerAccountService {

    private SolGroupB2BCustomerService solGroupB2BCustomerService;
    private DefaultSolGroupB2BCustomerAccountDao solGroupB2BCustomerAccountDao;

    /**
     * Generate token.
     * Set token on new B2BCustomer
     */
    @Override
    public void identityConfirmation_createToken(CustomerModel customerModel) {
        validateParameterNotNullStandardMessage("customerModel", customerModel);
        final long timeStamp = getTokenValiditySeconds() > 0L ? new Date().getTime() : 0L;
        final SecureToken data = new SecureToken(customerModel.getUid(), timeStamp);
        final String token = getSecureTokenService().encryptData(data);
        customerModel.setToken(token);
        getModelService().save(customerModel);

    }


    /**
     * Complete identity confirmation process
     */
    @Override
    public void identityConfirmation_createPassword(final String token, final String newPassword) throws TokenInvalidatedException
    {
        Assert.hasText(token, "The field [token] cannot be empty");
        Assert.hasText(newPassword, "The field [newPassword] cannot be empty");

        final SecureToken data = getSecureTokenService().decryptData(token);
        //		if (getTokenValiditySeconds() > 0L)
        //		{
        //			final long delta = new Date().getTime() - data.getTimeStamp();
        //			if (delta / 1000 > getTokenValiditySeconds())
        //			{
        //				throw new IllegalArgumentException("token expired");
        //			}
        //		}

        final CustomerModel customer = getUserService().getUserForUID(data.getData(), CustomerModel.class);
        if (customer == null)
        {
            throw new IllegalArgumentException("user for token not found");
        }
        if (!token.equals(customer.getToken()))
        {
            throw new TokenInvalidatedException();
        }
        customer.setToken(null);
        customer.setLoginDisabled(false);
        if(customer instanceof B2BCustomerModel) {
            final B2BCustomerModel b2bCustomer = (B2BCustomerModel)customer;
            if(b2bCustomer.getDefaultB2BUnit()!=null && BooleanUtils.isTrue(b2bCustomer.getDefaultB2BUnit().getActive())) {
                getSolGroupB2BCustomerService().toggleB2BCustomer(b2bCustomer, Boolean.TRUE, Boolean.FALSE);
                b2bCustomer.setIdentityConfirmationCompleted(Boolean.TRUE);
            }
        }
        getModelService().save(customer);

        getUserService().setPassword(data.getData(), newPassword, getPasswordEncoding());
    }


    @Override
    public boolean validateToken(String token) throws TokenInvalidatedException,IllegalArgumentException
    {
        final SecureToken data = getSecureTokenService().decryptData(token);

        final CustomerModel customer = getUserService().getUserForUID(data.getData(), CustomerModel.class);
        if (customer == null)
        {
            throw new IllegalArgumentException("user for token not found");
        }
        if (!token.equals(customer.getToken()))
        {
            throw new TokenInvalidatedException();
        }
        return true;
    }

    @Override
    public SearchPageData<OrderModel> getOrderList(final String orderCode, final Date orderDate,
            final Set<B2BUnitModel> b2bUnitsSetModel, final BaseStoreModel store,
            final OrderStatus status, final PageableData pageableData) {
        validateParameterNotNull(b2bUnitsSetModel, "B2BUnits set model cannot be null");
        validateParameterNotNull(store, "Store must not be null");
        validateParameterNotNull(pageableData, "PageableData must not be null");
        return getSolGroupB2BCustomerAccountDao().findOrdersByB2BUnitAndStore(orderCode, orderDate, b2bUnitsSetModel,
                store, status, pageableData);
    }

    @Override
    public OrderModel getOrderForCode(final Set<B2BUnitModel> b2bUnitsSetModel, final String code,
            final BaseStoreModel store) {
        validateParameterNotNull(b2bUnitsSetModel, "B2BUnits set model cannot be null");
        validateParameterNotNull(code, "Order code cannot be null");
        validateParameterNotNull(store, "Store must not be null");
        return getSolGroupB2BCustomerAccountDao().findOrderByCustomerAndCodeAndStore(b2bUnitsSetModel, code, store);
    }
    
	protected CurrencyModel getCurrency(final CustomerModel customerModel)
	{
		if (customerModel != null && customerModel.getSessionCurrency() != null)
		{
			return customerModel.getSessionCurrency();
		}
		else if(customerModel instanceof B2BCustomerModel && ((B2BCustomerModel)customerModel).getDefaultB2BUnit().getSessionCurrency()!=null) {
			return ((B2BCustomerModel)customerModel).getDefaultB2BUnit().getSessionCurrency();
		}
		else {
			return getCommonI18NService().getCurrentCurrency();
		}
		
	}
 

    protected SolGroupB2BCustomerService getSolGroupB2BCustomerService() {
        return solGroupB2BCustomerService;
    }

    @Required
    public void setSolGroupB2BCustomerService(SolGroupB2BCustomerService solGroupB2BCustomerService) {
        this.solGroupB2BCustomerService = solGroupB2BCustomerService;
    }

    protected DefaultSolGroupB2BCustomerAccountDao getSolGroupB2BCustomerAccountDao() {
        return solGroupB2BCustomerAccountDao;
    }

    @Required
    public void setSolGroupB2BCustomerAccountDao(DefaultSolGroupB2BCustomerAccountDao solGroupB2BCustomerAccountDao) {
        this.solGroupB2BCustomerAccountDao = solGroupB2BCustomerAccountDao;
    }


}
