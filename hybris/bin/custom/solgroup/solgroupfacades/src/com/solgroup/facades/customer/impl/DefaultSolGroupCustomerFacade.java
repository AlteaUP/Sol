package com.solgroup.facades.customer.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import com.solgroup.core.service.b2bcustomer.SolGroupCustomerAccountService;
import com.solgroup.core.service.b2bcustomer.impl.SolGroupB2BCustomerService;
import com.solgroup.facades.customer.SolGroupCustomerFacade;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2bacceleratorfacades.customer.exception.InvalidPasswordException;
import de.hybris.platform.b2bacceleratorfacades.customer.impl.DefaultB2BCustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.TokenInvalidatedException;

public class DefaultSolGroupCustomerFacade extends DefaultB2BCustomerFacade implements SolGroupCustomerFacade {

	private SolGroupCustomerAccountService solGroupCustomerAccountService;
	private SolGroupB2BCustomerService solGroupB2BCustomerService;
	
	private static final Logger LOG = Logger.getLogger(DefaultSolGroupCustomerFacade.class);
	
	@Override
	protected void validateDataBeforeUpdate(final CustomerData customerData)
	{
		validateParameterNotNullStandardMessage("customerData", customerData);
		//		Assert.hasText(customerData.getTitleCode(), "The field [TitleCode] cannot be empty");
		Assert.hasText(customerData.getFirstName(), "The field [FirstName] cannot be empty");
		Assert.hasText(customerData.getLastName(), "The field [LastName] cannot be empty");
		Assert.hasText(customerData.getUid(), "The field [Uid] cannot be empty");
	}

	
	@Override
	public void identityConfirmation_createPassword(String token, String newPassword) throws TokenInvalidatedException, InvalidPasswordException {
		validatePassword(newPassword);
		getSolGroupCustomerAccountService().identityConfirmation_createPassword(token, newPassword);
	}

	public boolean validateToken(String token) throws TokenInvalidatedException,IllegalArgumentException
	{
		return getSolGroupCustomerAccountService().validateToken(token);
	}
	
	public boolean validatePassword(final String password)
	{

		boolean isValid=true;
		
		if (StringUtils.isNotBlank(getPasswordPattern()))
		{
			Pattern p = Pattern.compile(getPasswordPattern());
			Matcher m = p.matcher(password);
			isValid = m.find();
		}

		if (!isValid)
		{
			throw new InvalidPasswordException("Password does not match pattern.");
		}

		return isValid;
	}

	@Override
	public void forgottenPassword_searchByEmail(String email) {
		Assert.hasText(email, "The field [email] cannot be empty");
		final B2BCustomerModel b2bCustomerModel = (B2BCustomerModel) getSolGroupB2BCustomerService().findB2BCustomerByEmail(email);
		if(b2bCustomerModel != null) {
			getCustomerAccountService().forgottenPassword(b2bCustomerModel);
		}
		else {
			LOG.warn(String.format("No B2BCustomer found for email [%s]. Impossible to perform forgottenPassword procedure", email));
		}
		
	}

	
	protected SolGroupCustomerAccountService getSolGroupCustomerAccountService() {
		return solGroupCustomerAccountService;
	}

	@Required
	public void setSolGroupCustomerAccountService(SolGroupCustomerAccountService solGroupCustomerAccountService) {
		this.solGroupCustomerAccountService = solGroupCustomerAccountService;
	}


	protected SolGroupB2BCustomerService getSolGroupB2BCustomerService() {
		return solGroupB2BCustomerService;
	}

	@Required
	public void setSolGroupB2BCustomerService(SolGroupB2BCustomerService solGroupB2BCustomerService) {
		this.solGroupB2BCustomerService = solGroupB2BCustomerService;
	}




	
	

}
