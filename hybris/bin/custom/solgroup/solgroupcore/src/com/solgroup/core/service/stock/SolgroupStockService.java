package com.solgroup.core.service.stock;

import java.util.Collection;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.commerceservices.stock.CommerceStockService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.store.BaseStoreModel;

public interface SolgroupStockService extends CommerceStockService {

	<T extends ProductModel> StockLevelModel createStockLevel(BaseStoreModel baseStore, T product, InStockStatus inStockStatus);
	<T extends ProductModel> StockLevelModel createStockLevel(BaseStoreModel baseStore, T product, Integer available);

	<T extends ProductModel> StockLevelModel createStockLevel(BaseStoreModel baseStore, T product);

	<T extends ProductModel> StockLevelModel createStockLevel(WarehouseModel warehouse, T product);

	<T extends ProductModel> StockLevelModel createSelfEstimateStockLevel(BaseStoreModel baseStore, T product);
	<T extends ProductModel> StockLevelModel updateSelfEstimateStockLevel(T product);
	
	
	
	Boolean saveStockLevels(Collection<StockLevelModel> arg0);
	Boolean saveStockLevel(StockLevelModel arg0);
	
	
	
}
