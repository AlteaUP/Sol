package com.solgroup.b2bcommercefacades.company.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2bcommercefacades.company.B2BUserFacade;
import de.hybris.platform.b2bcommercefacades.company.impl.DefaultB2BUserFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.servicelayer.util.ServicesUtil;

public class DefaultSolgroupB2BUserFacade extends DefaultB2BUserFacade implements B2BUserFacade {
	public void updateCustomer(CustomerData customerData) {
		ServicesUtil.validateParameterNotNullStandardMessage("customerData", customerData);
		Assert.hasText(customerData.getFirstName(), "The field [FirstName] cannot be empty");
		Assert.hasText(customerData.getLastName(), "The field [LastName] cannot be empty");
		B2BCustomerModel customerModel;
		if (StringUtils.isEmpty(customerData.getUid())) {
			customerModel = (B2BCustomerModel) this.getModelService().create(B2BCustomerModel.class);
		} else {
			customerModel = (B2BCustomerModel) this.getUserService().getUserForUID(customerData.getUid(),
					B2BCustomerModel.class);
		}

		this.getModelService().save(this.getB2BCustomerReverseConverter().convert(customerData, customerModel));
	}

}
