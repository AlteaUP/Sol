package com.solgroup.facades.user.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.google.common.base.Preconditions;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.impl.DefaultUserFacade;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.model.ModelService;

public class DefaultSolGroupUserFacade extends DefaultUserFacade{
	
	
	@Resource(name="modelService")
	ModelService modelService;
	
	@Override
	public void addAddress(final AddressData addressData)
	{
		validateParameterNotNullStandardMessage("addressData", addressData);

		final B2BCustomerModel currentCustomer = (B2BCustomerModel) getCurrentUserForCheckout();
		
		CompanyModel company = currentCustomer.getDefaultB2BUnit();

		final boolean makeThisAddressTheDefault = addressData.isDefaultAddress()
				|| (company.getShippingAddress() == null && addressData.isVisibleInAddressBook());

		// Create the new address model
		final AddressModel newAddress = getModelService().create(AddressModel.class);
		getAddressReversePopulator().populate(addressData, newAddress);

		// Store the address against the user
		
		saveAddressEntry(company, newAddress);

		// Update the address ID in the newly created address
		addressData.setId(newAddress.getPk().toString());

//		if (makeThisAddressTheDefault)
//		{
//			getCustomerAccountService().setDefaultAddressEntry(currentCustomer, newAddress);
//		}
	}
	
	
	public void saveAddressEntry(final CompanyModel companyModel, final AddressModel addressModel)
	{
		validateParameterNotNull(companyModel, "Customer model cannot be null");
		validateParameterNotNull(addressModel, "Address model cannot be null");
		final List<AddressModel> customerAddresses = new ArrayList<AddressModel>();
		customerAddresses.addAll(companyModel.getAddresses());
		if (companyModel.getAddresses().contains(addressModel))
		{
			modelService.save(addressModel);
		}
		else
		{
			addressModel.setOwner(companyModel);
			addressModel.setShippingAddress(true);
			modelService.save(addressModel);
			modelService.refresh(addressModel);
			customerAddresses.add(addressModel);
		}
		companyModel.setAddresses(customerAddresses);

		modelService.save(companyModel);
		modelService.refresh(companyModel);
	}
	
	public static void validateParameterNotNull(Object parameter, String nullMessage) {
		Preconditions.checkArgument(parameter != null, nullMessage);
	}

	public static void validateParameterNotNullStandardMessage(String parameter, Object parameterValue) {
		validateParameterNotNull(parameterValue, "Parameter " + parameter + " can not be null");
	}

}
