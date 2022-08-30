package com.solgroup.core.service.address;

import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.user.AddressService;

public interface SolGroupAddressService extends AddressService {
	
	String getHybrisErpCode(String erpCode, String addressOwnerId);
	
	String retrieveErpCode(AddressModel address);

}
