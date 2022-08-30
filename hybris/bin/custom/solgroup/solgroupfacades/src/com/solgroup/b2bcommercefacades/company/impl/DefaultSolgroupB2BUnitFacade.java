package com.solgroup.b2bcommercefacades.company.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.b2bcommercefacades.company.SolgroupB2BUnitFacade;
import com.solgroup.core.service.b2bunit.SolGroupB2BUnitService;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2bcommercefacades.company.B2BUnitFacade;
import de.hybris.platform.b2bcommercefacades.company.impl.DefaultB2BUnitFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;

public class DefaultSolgroupB2BUnitFacade extends DefaultB2BUnitFacade implements SolgroupB2BUnitFacade {
	
	private SolGroupB2BUnitService solGroupB2BUnitService;
	private UserService userService;
	private Converter<AddressModel, AddressData> addressConverter; 

	@Override
	public Map<String, String> getAllActiveUnitsOfOrganizationWithErpCodeAndName() {
		B2BCustomerModel currentUser = (B2BCustomerModel) this.getUserService().getCurrentUser();
		Set units = this.getB2BUnitService().getAllUnitsOfOrganization(currentUser);
		Map<String, String> b2bUnitMap = new HashMap<String, String>();
		Iterator arg4 = units.iterator();
		while (arg4.hasNext()) {
			B2BUnitModel b2BUnitModel = (B2BUnitModel) arg4.next();
			if (Boolean.TRUE.equals(b2BUnitModel.getActive())) {

				b2bUnitMap.put(b2BUnitModel.getUid(), b2BUnitModel.getErpCode() + " - " + b2BUnitModel.getName());
			}
		}
		
		return b2bUnitMap;
	}
	
	
	@Override
	public AddressData getBillingAddress(String b2bCustomerCode) {
		
		B2BCustomerModel b2bCustomerModel = (B2BCustomerModel)userService.getUserForUID(b2bCustomerCode);
		AddressModel billingAddressModel = getSolGroupB2BUnitService().getBillingAddress(b2bCustomerModel.getDefaultB2BUnit());
		AddressData billingAddressData = addressConverter.convert(billingAddressModel);
		return billingAddressData;
	}


	protected SolGroupB2BUnitService getSolGroupB2BUnitService() {
		return solGroupB2BUnitService;
	}

	@Required
	public void setSolGroupB2BUnitService(SolGroupB2BUnitService solGroupB2BUnitService) {
		this.solGroupB2BUnitService = solGroupB2BUnitService;
	}


	protected UserService getUserService() {
		return userService;
	}

	@Required
	public void setUserService(UserService userService) {
		this.userService = userService;
	}


	protected Converter<AddressModel, AddressData> getAddressConverter() {
		return addressConverter;
	}

	@Required
	public void setAddressConverter(Converter<AddressModel, AddressData> addressConverter) {
		this.addressConverter = addressConverter;
	}
	
	
	
	
	

}
