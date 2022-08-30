package com.solgroup.fulfilmentprocess.actions.orderexport;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import org.apache.log4j.Logger;

public class ExportClientToLegacyAction extends AbstractSimpleDecisionAction<OrderProcessModel> {
    private static final Logger LOG = Logger.getLogger(ExportClientToLegacyAction.class);

    @Override
    public Transition executeAction(final OrderProcessModel process)
    {
        final OrderModel order = process.getOrder();

        return Transition.OK;

    }
}
