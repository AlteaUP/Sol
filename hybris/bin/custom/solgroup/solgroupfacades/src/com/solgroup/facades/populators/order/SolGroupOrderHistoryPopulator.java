package com.solgroup.facades.populators.order;

import de.hybris.platform.commercefacades.order.converters.populator.OrderHistoryPopulator;
import de.hybris.platform.commercefacades.order.data.OrderHistoryData;
import de.hybris.platform.core.model.order.OrderModel;

/**
 * @author fmilazzo
 *
 */
public class SolGroupOrderHistoryPopulator extends OrderHistoryPopulator {

    @Override
    public void populate(final OrderModel source, final OrderHistoryData target) {
        super.populate(source, target);
        if (source.getRefillOrderEntryNumber() != null) {
            if (source.getRefillOrderEntryNumber().intValue() > 0) {
                target.setRefill(true);
            } else {
                target.setRefill(false);
            }
        }
    }

}
