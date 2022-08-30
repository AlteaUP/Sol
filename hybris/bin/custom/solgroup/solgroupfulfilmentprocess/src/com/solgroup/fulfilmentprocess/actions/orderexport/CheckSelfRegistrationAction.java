package com.solgroup.fulfilmentprocess.actions.orderexport;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import org.apache.log4j.Logger;

public class CheckSelfRegistrationAction extends AbstractSimpleDecisionAction<OrderProcessModel> {
    private static final Logger LOG = Logger.getLogger(CheckSelfRegistrationAction.class);

    @Override public Transition executeAction(final OrderProcessModel process) {
        final OrderModel order = process.getOrder();
        boolean b2bUnitSelfRegistration = false;
        if (b2bUnitSelfRegistration) {
            //self registration go to export to legacy
            return Transition.OK;
        } else {
            //Erp user go to check payment method
            return Transition.NOK;
        }

    }
}
