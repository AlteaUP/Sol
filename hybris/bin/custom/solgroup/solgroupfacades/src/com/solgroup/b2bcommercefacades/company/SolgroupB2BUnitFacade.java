package com.solgroup.b2bcommercefacades.company;

import java.util.Map;

import de.hybris.platform.b2bcommercefacades.company.B2BUnitFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;

public interface SolgroupB2BUnitFacade extends B2BUnitFacade {

	Map<String, String> getAllActiveUnitsOfOrganizationWithErpCodeAndName();
	
	AddressData getBillingAddress(String b2bCustomerCode);
}
