package com.solgroup.core.service.b2bcustomer.impl;

import org.apache.commons.lang.StringUtils;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commerceservices.customer.CustomerEmailResolutionService;
import de.hybris.platform.commerceservices.customer.impl.DefaultCustomerEmailResolutionService;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.util.mail.MailUtils;

public class DefaultSolGroupB2BCustomerEmailResolutionService extends DefaultCustomerEmailResolutionService {

	@Override
	public String getEmailForCustomer(CustomerModel customer) {

		if(customer instanceof B2BCustomerModel) {
			B2BCustomerModel b2bCustomer = (B2BCustomerModel)customer;
			
			if(StringUtils.isNotEmpty(b2bCustomer.getEmail())) {
				return b2bCustomer.getEmail();
			}
		}
		
		return super.getEmailForCustomer(customer);
		
	}

}
