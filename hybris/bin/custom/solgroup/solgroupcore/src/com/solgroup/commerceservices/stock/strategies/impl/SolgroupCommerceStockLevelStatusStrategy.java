package com.solgroup.commerceservices.stock.strategies.impl;

import org.apache.commons.lang.BooleanUtils;
import org.codehaus.groovy.tools.shell.util.Logger;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.commerceservices.stock.strategies.impl.CommerceStockLevelStatusStrategy;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.stock.strategy.StockLevelStatusStrategy;

public class SolgroupCommerceStockLevelStatusStrategy extends CommerceStockLevelStatusStrategy
		implements StockLevelStatusStrategy {

	private Logger LOG = Logger.create(this.getClass());

	@Override
	public StockLevelStatus checkStatus(final StockLevelModel stockLevel) {
		StockLevelStatus resultStatus = StockLevelStatus.OUTOFSTOCK;
		if (stockLevel == null) {
			LOG.warn("StockLevel object is NULL: no checks can be performed! Return OUTOFSTOCK");
			resultStatus = StockLevelStatus.OUTOFSTOCK;
		}

		else if (BooleanUtils.isFalse(stockLevel.getProduct().getStockable())
				|| InStockStatus.FORCEINSTOCK.equals(stockLevel.getInStockStatus())) {
			resultStatus = StockLevelStatus.INSTOCK;
		}

		else if (InStockStatus.FORCEOUTOFSTOCK.equals(stockLevel.getInStockStatus())) {
			resultStatus = StockLevelStatus.OUTOFSTOCK;
		} else {
			LOG.warn("StockLevel is inconsistent for '" + stockLevel.getWarehouse().getCode() + "-"
					+ stockLevel.getProductCode() + "': inStockStatus is 'n/a' or 'Not Specified'! Return OUTOFSTOCK");
			return StockLevelStatus.OUTOFSTOCK;
		}

		return resultStatus;
	}

	@Required
	@Override
	public void setDefaultLowStockThreshold(int lowStockThreshold) {
		super.setDefaultLowStockThreshold(lowStockThreshold);
	}
}
