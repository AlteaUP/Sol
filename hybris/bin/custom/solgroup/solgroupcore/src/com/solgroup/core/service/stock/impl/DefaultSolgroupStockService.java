package com.solgroup.core.service.stock.impl;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.stock.SolgroupStockService;
import com.solgroup.core.strategies.SolgroupStockStrategy;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.commerceservices.stock.impl.DefaultCommerceStockService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;

public class DefaultSolgroupStockService extends DefaultCommerceStockService implements SolgroupStockService {

	private BaseStoreService baseStoreService;
	private ModelService modelService;

	private SolgroupStockStrategy stockStrategy;

	private Logger LOG = Logger.getLogger(DefaultSolgroupStockService.class);

	@Override
	public <T extends ProductModel> StockLevelModel createStockLevel(BaseStoreModel baseStore, T product,
			InStockStatus inStockStatus) {

		StockLevelModel stockLevel = createStockLevel(baseStore, product);
		stockLevel.setInStockStatus(inStockStatus);

		return stockLevel;
	}

	@Override
	public <T extends ProductModel> StockLevelModel createStockLevel(BaseStoreModel baseStore, T product) {

		return createStockLevel(getStockStrategy().estimateDefaultWarehouse(baseStore), product);
	}

	@Override
	public <T extends ProductModel> StockLevelModel createStockLevel(BaseStoreModel baseStore, T product, Integer available) {
		StockLevelModel stockLevel = createStockLevel(baseStore, product);
		stockLevel.setAvailable(available);

		stockLevel.setInStockStatus(getStockStrategy().estimateInStockStatus(available));

		return stockLevel;
	}

	@Override
	public <T extends ProductModel> StockLevelModel createStockLevel(WarehouseModel warehouse, T product) {
		StockLevelModel stockLevel = getModelService().create(StockLevelModel.class);

		stockLevel.setProduct(product);
		stockLevel.setProductCode(product.getCode());
		stockLevel.setWarehouse(warehouse);
		return stockLevel;
	}

	@Override
	public Boolean saveStockLevels(Collection<StockLevelModel> arg0) {

		if (arg0 != null) {
			try {
				getModelService().saveAll(arg0);
				return true;
			} catch (final Exception exc) {
				LOG.error("Error during save collection of products " + arg0.toString() + " : " + exc.getMessage(), exc);
			}
		}

		return false;
	}
	
	@Override
	public Boolean saveStockLevel(StockLevelModel arg0) {
		if (arg0 != null) {
			try {
				getModelService().save(arg0);
				return true;
			} catch (final Exception exc) {
				LOG.error("Error during save product " + arg0.toString() + " : " + exc.getMessage(), exc);
			}
		}

		return false;
	}
	
	@Override
	public <T extends ProductModel> StockLevelModel createSelfEstimateStockLevel(BaseStoreModel baseStore, T product) {
		return createStockLevel(baseStore, product, getStockStrategy().estimateInStockStatus(product));
	}

	
	@Override
	public <T extends ProductModel> StockLevelModel updateSelfEstimateStockLevel(T product) {
		StockLevelModel stockLevel = product.getStockLevels().iterator().next();
		
		stockLevel.setInStockStatus(getStockStrategy().estimateInStockStatus(product, stockLevel.getAvailable()));
		saveStockLevel(stockLevel);
		
		return stockLevel;
	}

	protected BaseStoreService getBaseStoreService() {
		return baseStoreService;
	}

	@Required
	public void setBaseStoreService(BaseStoreService baseStoreService) {
		this.baseStoreService = baseStoreService;
	}

	protected ModelService getModelService() {
		return modelService;
	}

	@Required
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	protected SolgroupStockStrategy getStockStrategy() {
		return stockStrategy;
	}

	@Required
	public void setStockStrategy(SolgroupStockStrategy stockStrategy) {
		this.stockStrategy = stockStrategy;
	}






}
