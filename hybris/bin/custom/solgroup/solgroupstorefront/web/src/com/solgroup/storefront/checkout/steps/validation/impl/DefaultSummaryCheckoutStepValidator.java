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
import de.hybris.platform.commercefacades.order.data.CartData;
import rx.exceptions.OnErrorThrowable.OnNextValue;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.validation.AbstractCheckoutStepValidator;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.solgroup.facades.order.SolGroupCheckoutFacade;


public abstract class DefaultSummaryCheckoutStepValidator extends AbstractCheckoutStepValidator
{
	private static final Logger LOGGER = Logger.getLogger(DefaultSummaryCheckoutStepValidator.class);

	@Resource(name = "b2bCheckoutFacade")
	private SolGroupCheckoutFacade b2bCheckoutFacade;
	
	@Override
	public ValidationResults validateOnEnter(final RedirectAttributes redirectAttributes)
	{
		final ValidationResults cartResult = checkCartAndDelivery(redirectAttributes);
		if (cartResult != null) {
			return cartResult;
		}

		final ValidationResults paymentResult = checkPaymentMethodAndPickup(redirectAttributes);
		if (paymentResult != null) {
			return paymentResult;
		}
		
		doValidateOnEnter(redirectAttributes);

		return ValidationResults.SUCCESS;
	}

	protected ValidationResults checkPaymentMethodAndPickup(final RedirectAttributes redirectAttributes) {
//		if (b2bCheckoutFacade.hasNoPaymentInfo())
//		{
//			GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.INFO_MESSAGES_HOLDER,
//					"checkout.multi.paymentDetails.notprovided");
//			return ValidationResults.REDIRECT_TO_PAYMENT_METHOD;
//		}

		final CartData cartData = b2bCheckoutFacade.getCheckoutCart();

		if (!b2bCheckoutFacade.hasShippingItems())
		{
			cartData.setDeliveryAddress(null);
		}
		
		return null;
	}

	protected ValidationResults checkCartAndDelivery(final RedirectAttributes redirectAttributes) {
		if (!b2bCheckoutFacade.hasValidCart())
		{
			LOGGER.info("Missing, empty or unsupported cart");
			return ValidationResults.REDIRECT_TO_CART;
		}

		if (b2bCheckoutFacade.hasNoDeliveryAddress())
		{
			GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.INFO_MESSAGES_HOLDER,
					"checkout.multi.deliveryAddress.notprovided");
			return ValidationResults.REDIRECT_TO_DELIVERY_ADDRESS;
		}

//		if (b2bCheckoutFacade.hasNoDeliveryMode())
//		{
//			GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.INFO_MESSAGES_HOLDER,
//					"checkout.multi.deliveryMethod.notprovided");
//			return ValidationResults.REDIRECT_TO_DELIVERY_METHOD;
//		}
		return null;
	}
	
	/**
	 * Performs implementation specific validation on entering a checkout step after the common validation has been
	 * performed in the abstract implementation.
	 *
	 * @param redirectAttributes
	 * @return {@link ValidationResults}
	 */
	protected abstract ValidationResults doValidateOnEnter(final RedirectAttributes redirectAttributes);
}