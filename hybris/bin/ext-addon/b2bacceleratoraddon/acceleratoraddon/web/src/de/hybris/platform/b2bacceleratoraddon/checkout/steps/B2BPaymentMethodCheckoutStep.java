/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.b2bacceleratoraddon.checkout.steps;

import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;

import org.springframework.beans.factory.annotation.Required;


public class B2BPaymentMethodCheckoutStep extends CheckoutStep
{
	private CheckoutFacade checkoutFacade;

	@Override
	public boolean isEnabled()
	{
		final CartData checkoutCart = getCheckoutFacade().getCheckoutCart();
		if (checkoutCart == null || checkoutCart.getPaymentType() == null)
		{
			return false;
		}
		//		return CheckoutPaymentType.CARD.getCode().equals(checkoutCart.getPaymentType().getCode());
		return true;
	}

	protected CheckoutFacade getCheckoutFacade()
	{
		return checkoutFacade;
	}

	@Required
	public void setCheckoutFacade(final CheckoutFacade checkoutFacade)
	{
		this.checkoutFacade = checkoutFacade;
	}

}
