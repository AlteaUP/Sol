/*
 * Put your copyright text here
 */
 package com.solgroup.fulfilmentprocess.actions.orderexport;

import org.apache.log4j.Logger;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;

public class CaptureAction extends AbstractSimpleDecisionAction<OrderProcessModel> {
    private static final Logger LOG = Logger.getLogger(CaptureAction.class);

    @Override
    public Transition executeAction(final OrderProcessModel process) {
        final OrderModel order = process.getOrder();

        return Transition.OK;

    }
}
