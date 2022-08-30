package com.solgroup.acceleratorstorefrontcommons.util;

import javax.annotation.Resource;


//import com.solgroup.acceleratorstorefrontcommons.forms.SolGroupAddressForm;
//import de.hybris.platform.acceleratorstorefrontcommons.forms.SolGroupAddressForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;
import de.hybris.platform.acceleratorstorefrontcommons.util.AddressDataUtil;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;

public class SolgroupAddressDataUtil extends AddressDataUtil {

	@Resource(name = "i18NFacade")
	private I18NFacade i18NFacade;
	
	public I18NFacade getI18NFacade() {
		return i18NFacade;
	}

	public void setI18NFacade(I18NFacade i18nFacade) {
		i18NFacade = i18nFacade;
	}
	
	
	
	

	public void convertBasic(final AddressData source, final AddressForm target)
	{
		super.convertBasic(source, target);
		target.setBillingAddress(source.isBillingAddress());
		target.setShippingAddress(source.isShippingAddress());
		target.setFax(source.getFax());
		target.setEmail(source.getEmail());
		// BUGFIX SOL653
		target.setPhone(source.getPhone());
	}
	
	public AddressData convertToAddressData(final AddressForm addressForm)
	{
	
		AddressData addressData = super.convertToAddressData(addressForm);
		
		addressData.setBillingAddress(addressForm.getBillingAddress()==null?false:addressForm.getBillingAddress());
		addressData.setShippingAddress(addressForm.getShippingAddress()==null?true:addressForm.getShippingAddress());
		addressData.setFax(addressForm.getFax());
		addressData.setEmail(addressForm.getEmail());
		
		return addressData;
	}
	
}
