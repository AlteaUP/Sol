package com.solgroup.core.strategies.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.strategies.SolgroupStockStrategy;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.store.BaseStoreModel;

public class DefaultSolgroupStockStrategy implements SolgroupStockStrategy {

	private Integer threshold;

	private Logger LOG = Logger.getLogger(DefaultSolgroupStockStrategy.class);

	@Override
	public InStockStatus estimateInStockStatus(Integer available) {

		try {
			if (available >= threshold)
				return InStockStatus.FORCEINSTOCK;
			else
				return InStockStatus.FORCEOUTOFSTOCK;

		} catch (NullPointerException e) {
			if (LOG.isInfoEnabled())
				LOG.info("stock level available value is NULL; return null");
			return null;
		}

	}
	
	@Override
	public InStockStatus estimateInStockStatus(ProductModel product) {
		try {
			if (product.getStockable())
				return InStockStatus.FORCEOUTOFSTOCK;
			else
				return InStockStatus.FORCEINSTOCK;

		} catch (NullPointerException e) {
			if (LOG.isInfoEnabled())
				LOG.info("product or stockable valuse is NULL; return null");
			return null;
		}
	}
	
	

	@Override
	public WarehouseModel estimateDefaultWarehouse(BaseStoreModel baseStore) {

		return baseStore.getWarehouses().get(0);
	}

	protected Integer getThreshold() {
		return threshold;
	}

	@Required
	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
	}

	@Override
	public <T extends ProductModel> InStockStatus estimateInStockStatus(T product, Integer available) {
		try {
			if (product.getStockable())
				return estimateInStockStatus(available);
			else
				return InStockStatus.FORCEINSTOCK;

		} catch (NullPointerException e) {
			if (LOG.isInfoEnabled())
				LOG.info("product or stockable valuse is NULL; return null");
			return null;
		}
	}



}
