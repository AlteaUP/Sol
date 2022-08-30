package com.solgroup.service;

import java.util.Collection;

import com.solgroup.core.model.LegacySystemModel;
import com.solgroup.core.ws.services.stock.StocksResponse;

import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.product.ProductModel;

public interface SolGroupWSStockService {
	
	void sendStockRequest(LegacySystemModel legacySystem, CountryModel country);

	void sendStockRequest(LegacySystemModel legacySystem, CountryModel country, Collection<ProductModel> products);
	
}
