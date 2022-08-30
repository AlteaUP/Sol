package com.solgroup.facades.services.checkout.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import de.hybris.platform.b2b.model.B2BCostCenterModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType;
import de.hybris.platform.b2bacceleratorservices.strategies.impl.DefaultB2BDeliveryAddressesLookupStrategy;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;

public class DefaultSolGroupB2BDeliveryAddressesLookupStrategy extends DefaultB2BDeliveryAddressesLookupStrategy{

	@Resource(name="checkoutCustomerStrategy")
	private CheckoutCustomerStrategy checkoutCustomerStrategy;
	
	@Override
	public List<AddressModel> getDeliveryAddressesForOrder(final AbstractOrderModel abstractOrder, final boolean visibleAddressesOnly)
	{
		// Use fallback
		final List<AddressModel> addressesForOrder = new ArrayList<AddressModel>();
		if (abstractOrder != null)
		{
			final B2BCustomerModel user = (B2BCustomerModel)abstractOrder.getUser();
			
			final CompanyModel company = user.getDefaultB2BUnit();
			if (user instanceof CustomerModel)
			{
				if (visibleAddressesOnly)
				{
					addressesForOrder.addAll(company.getShippingAddresses());
				}
				else
				{
					addressesForOrder.addAll(company.getAddresses());
				}

				// If the user had no addresses, check the order for an address in case it's a guest checkout.
				if (checkoutCustomerStrategy.isAnonymousCheckout() &&
						addressesForOrder.isEmpty() && abstractOrder.getDeliveryAddress() != null)
				{
					addressesForOrder.add(abstractOrder.getDeliveryAddress());
				}
			}
			
		}
		return addressesForOrder;
	}
}
