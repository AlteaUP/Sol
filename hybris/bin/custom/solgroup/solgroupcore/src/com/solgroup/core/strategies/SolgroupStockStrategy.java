package com.solgroup.core.strategies;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.store.BaseStoreModel;

public interface SolgroupStockStrategy {

	InStockStatus estimateInStockStatus(Integer available); 
	
	WarehouseModel estimateDefaultWarehouse(BaseStoreModel baseStore);
	
	<T extends ProductModel> InStockStatus estimateInStockStatus(T product); 

	<T extends ProductModel> InStockStatus estimateInStockStatus(T product, Integer available); 

}
