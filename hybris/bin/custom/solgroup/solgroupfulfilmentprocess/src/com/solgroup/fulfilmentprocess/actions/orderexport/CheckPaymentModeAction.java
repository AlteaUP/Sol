package com.solgroup.fulfilmentprocess.actions.orderexport;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.core.model.order.payment.InvoicePaymentInfoModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import org.apache.log4j.Logger;

public class CheckPaymentModeAction extends AbstractSimpleDecisionAction<OrderProcessModel> {
    private static final Logger LOG = Logger.getLogger(CheckPaymentModeAction.class);

    @Override
    public Transition executeAction(final OrderProcessModel process)
    {
        final OrderModel order = process.getOrder();
        
        // Invoice paymentInfo --> no capture required
        if(order.getPaymentInfo()==null || order.getPaymentInfo() instanceof InvoicePaymentInfoModel) {
        	return Transition.NOK;
        }
        // CreditCard paymentInfo --> capture required
        else if(order.getPaymentInfo() instanceof CreditCardPaymentInfoModel) {
        	return Transition.OK;
        }
        // Fault back condition
        else {
        	return Transition.NOK;
        }
        
        
    }
}
