package com.solgroup.core.strategies;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.store.BaseStoreModel;

public interface SolgroupBaseCommerceSelectionProductStrategy {
	
	<T extends ProductModel> WarehouseModel getDefaultWarehouseForProduct(T product);
	<T extends ProductModel> BaseStoreModel getDefaultBaseStoreForProduct(T product);

	WarehouseModel getDefaultWarehouseForLegacySystemAndCountry(String legacySystemCode, String countryIsoCode);
	
}
