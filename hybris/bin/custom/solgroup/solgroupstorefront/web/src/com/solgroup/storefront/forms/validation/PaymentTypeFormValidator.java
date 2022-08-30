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
package com.solgroup.storefront.forms.validation;



import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.solgroup.storefront.forms.PaymentTypeForm;


/**
 * Validator for {@link PaymentTypeForm}.
 */
@Component("solGroupPaymentTypeFormValidator")
public class PaymentTypeFormValidator implements Validator
{

	@Override
	public boolean supports(final Class<?> clazz)
	{
		return PaymentTypeForm.class.equals(clazz);
	}

	@Override
	public void validate(final Object object, final Errors errors)
	{
		if (object instanceof PaymentTypeForm)
		{
			final PaymentTypeForm paymentTypeForm = (PaymentTypeForm) object;

			//			if (CheckoutPaymentType.ACCOUNT.getCode().equals(paymentTypeForm.getPaymentType())
			//					&& StringUtils.isBlank(paymentTypeForm.getCostCenterId()))
			//			{
			//				errors.rejectValue("costCenterId", "general.required");
			//			}
		}
	}

}
