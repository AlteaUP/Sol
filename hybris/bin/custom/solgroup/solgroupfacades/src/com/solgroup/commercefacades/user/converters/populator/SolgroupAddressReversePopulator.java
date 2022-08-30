package com.solgroup.commercefacades.user.converters.populator;



import de.hybris.platform.commercefacades.user.converters.populator.AddressReversePopulator;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class SolgroupAddressReversePopulator extends AddressReversePopulator {

	
	@Override
	public void populate(AddressData source, AddressModel target) throws ConversionException {

		super.populate(source, target);
		
		target.setFax(source.getFax());
		target.setEmail(source.getEmail());
		
		// Set hybris code
		target.setHybrisCode(String.valueOf(System.currentTimeMillis()));
		
	}


}
