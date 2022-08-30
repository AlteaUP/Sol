package com.solgroup.core.service.address.impl;

import org.apache.commons.lang.StringUtils;

import com.solgroup.core.service.address.SolGroupAddressService;

import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.user.impl.DefaultAddressService;

public class DefaultSolGroupAddressService extends DefaultAddressService implements SolGroupAddressService {

	@Override
	public String getHybrisErpCode(String erpCode, String addressOwnerId) {
		return erpCode + "__" + addressOwnerId;
	}

	@Override
	public String retrieveErpCode(AddressModel address) {
		if(StringUtils.isNotEmpty(address.getErpCode())) {
			return address.getErpCode().split("__")[0];
		}
		return "";
	}

}
