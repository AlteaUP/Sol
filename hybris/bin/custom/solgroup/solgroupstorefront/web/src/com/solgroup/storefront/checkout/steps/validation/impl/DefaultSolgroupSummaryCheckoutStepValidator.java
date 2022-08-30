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
package com.solgroup.storefront.checkout.steps.validation.impl;

import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.validation.ValidationResults;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType;
import de.hybris.platform.commercefacades.order.data.CartData;

import javax.annotation.Resource;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.solgroup.facades.order.SolGroupCheckoutFacade;


public class DefaultSolgroupSummaryCheckoutStepValidator extends DefaultSummaryCheckoutStepValidator
{
	@Resource(name = "b2bCheckoutFacade")
	private SolGroupCheckoutFacade b2bCheckoutFacade;
	
	@Override
	protected ValidationResults doValidateOnEnter(final RedirectAttributes redirectAttributes)
	{
		final CartData checkoutCart = b2bCheckoutFacade.getCheckoutCart();
		final B2BPaymentTypeData checkoutPaymentType = checkoutCart.getPaymentType();

		if (checkoutPaymentType == null)
		{
			GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.INFO_MESSAGES_HOLDER,
					"checkout.multi.paymentType.notprovided");
			return ValidationResults.REDIRECT_TO_PAYMENT_TYPE;
		}

		if (CheckoutPaymentType.ACCOUNT.getCode().equals(checkoutPaymentType.getCode()))
		{
			GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.INFO_MESSAGES_HOLDER,
					"checkout.multi.costCenter.notprovided");
			return ValidationResults.REDIRECT_TO_PAYMENT_TYPE;
		}

		if (b2bCheckoutFacade.hasNoDeliveryAddress())
		{
			GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.INFO_MESSAGES_HOLDER,
					"checkout.multi.deliveryAddress.notprovided");
			return ValidationResults.REDIRECT_TO_DELIVERY_ADDRESS;
		}

		//		if (getCheckoutFlowFacade().hasNoDeliveryMode())
		//		{
		//			GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.INFO_MESSAGES_HOLDER,
		//					"checkout.multi.deliveryMethod.notprovided");
		//			return ValidationResults.REDIRECT_TO_DELIVERY_METHOD;
		//		}

//		if (b2bCheckoutFacade.hasNoPaymentInfo())
//		{
//			GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.INFO_MESSAGES_HOLDER,
//					"checkout.multi.paymentDetails.notprovided");
//			return ValidationResults.REDIRECT_TO_PAYMENT_METHOD;
//		}

		if (!b2bCheckoutFacade.hasShippingItems())
		{
			checkoutCart.setDeliveryAddress(null);
		}

		return ValidationResults.SUCCESS;
	}
}