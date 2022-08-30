package com.solgroup.commerceservices.stock.strategies.impl;

import java.util.Collection;

import org.apache.commons.lang.BooleanUtils;
import org.apache.log4j.Logger;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.commerceservices.stock.strategies.CommerceAvailabilityCalculationStrategy;
import de.hybris.platform.commerceservices.stock.strategies.impl.DefaultCommerceAvailabilityCalculationStrategy;
import de.hybris.platform.ordersplitting.model.StockLevelModel;

public class DefaultSolgroupCommerceAvailabilityCalculationStrategy
        extends DefaultCommerceAvailabilityCalculationStrategy implements CommerceAvailabilityCalculationStrategy {

    private Logger LOG = Logger.getLogger(this.getClass());

    @Override
    public Long calculateAvailability(final Collection<StockLevelModel> stockLevels) {
        long totalActualAmount = 0;
        for (final StockLevelModel stockLevel : stockLevels) {
            // If any stock level is flagged as FORCEINSTOCK then return null to indicate in stock
            if (InStockStatus.FORCEINSTOCK.equals(stockLevel.getInStockStatus())
                    || BooleanUtils.isFalse(stockLevel.getProduct().getStockable())) {
                /* CR sulla visualizzazione della disponibilità residua dello stock */
                if (stockLevel.getProduct().getStockable()) {
                    return new Long(stockLevel.getAvailable());
                }
                return Long.MAX_VALUE;
            }

            // If any stock level is flagged as FORCEOUTOFSTOCK then we skip over it
            if (!InStockStatus.FORCEOUTOFSTOCK.equals(stockLevel.getInStockStatus())) {
                final long availableToSellQuantity = getAvailableToSellQuantity(stockLevel);
                if (availableToSellQuantity > 0 || !stockLevel.isTreatNegativeAsZero()) {
                    LOG.warn("StockLevel for product " + stockLevel.getProductCode() + " and warehouse "
                            + stockLevel.getWarehouse().getCode() + " have inStockStatus inconsistent");
                    totalActualAmount += availableToSellQuantity;
                }
            }
        }
        if (stockLevels.size() > 1)
            LOG.warn("Product " + stockLevels.iterator().next().getProductCode() + " have more of 1 stock Level");

        return Long.valueOf(totalActualAmount);
    }
}
