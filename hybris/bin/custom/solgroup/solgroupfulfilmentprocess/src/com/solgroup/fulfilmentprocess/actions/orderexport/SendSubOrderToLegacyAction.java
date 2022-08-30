package com.solgroup.fulfilmentprocess.actions.orderexport;

import com.solgroup.core.model.SubOrderProcessModel;
import com.solgroup.core.enums.ActionToLegacyEnum;
import com.solgroup.core.enums.SubOrderStatus;
import com.solgroup.core.model.SubOrderModel;
import com.solgroup.service.SolGroupWSOrderService;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class SendSubOrderToLegacyAction extends AbstractSimpleDecisionAction<SubOrderProcessModel> {
    private static final Logger LOG = Logger.getLogger(SendSubOrderToLegacyAction.class);

    private SolGroupWSOrderService solGroupWSOrderService;

    @Override public Transition executeAction(final SubOrderProcessModel process) throws Exception {
        final SubOrderModel subOrder = process.getSubOrder();
        try {
            // Send subOrder to legacy
        	getSolGroupWSOrderService().sendOrderInsert(subOrder);
        	// Set actionToLegacy
        	getSolGroupWSOrderService().setSubOrderActionToLegacy(subOrder, ActionToLegacyEnum.SENT_CREATION_TO_LEGACY);
        	// Set OK status on subOrder
        	getSolGroupWSOrderService().setSubOrderStatus(subOrder, SubOrderStatus.OK, "");
        	return Transition.OK;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            // set KO status
            getSolGroupWSOrderService().setSubOrderStatus(subOrder, SubOrderStatus.ERROR, "");
            return Transition.NOK;
        }
        
    }

    protected SolGroupWSOrderService getSolGroupWSOrderService() {
        return solGroupWSOrderService;
    }

    @Required public void setSolGroupWSOrderService(SolGroupWSOrderService solGroupWSOrderService) {
        this.solGroupWSOrderService = solGroupWSOrderService;
    }

}
