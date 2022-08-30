package com.solgroup.core.service.basecommerce.impl;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.model.LegacySystemModel;
import com.solgroup.core.service.basecommerce.SolGroupBaseCommerceService;
import com.solgroup.core.strategies.SolgroupBaseCommerceSelectionProductStrategy;

import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

public class DefaultSolGroupBaseCommerceService implements SolGroupBaseCommerceService {

	private SolgroupBaseCommerceSelectionProductStrategy baseCommerceSelectionProductStrategy;
	
	@Override
	public WarehouseModel getDefaultWarehouseForLegacySystemAndCountry(String legacySystemCode, String countryIsoCode) {
		
		return getBaseCommerceSelectionProductStrategy().getDefaultWarehouseForLegacySystemAndCountry(legacySystemCode, countryIsoCode);
	}

	protected SolgroupBaseCommerceSelectionProductStrategy getBaseCommerceSelectionProductStrategy() {
		return baseCommerceSelectionProductStrategy;
	}

	@Required
	public void setBaseCommerceSelectionProductStrategy(SolgroupBaseCommerceSelectionProductStrategy baseCommerceSelectionProductStrategy) {
		this.baseCommerceSelectionProductStrategy = baseCommerceSelectionProductStrategy;
	}

}
