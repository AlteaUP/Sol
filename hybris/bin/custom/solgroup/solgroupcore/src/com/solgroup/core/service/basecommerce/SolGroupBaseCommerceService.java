package com.solgroup.core.service.basecommerce;

import de.hybris.platform.ordersplitting.model.WarehouseModel;

public interface SolGroupBaseCommerceService {

	
	WarehouseModel getDefaultWarehouseForLegacySystemAndCountry(String legacySystemCode, String countryIsoCode);
}
