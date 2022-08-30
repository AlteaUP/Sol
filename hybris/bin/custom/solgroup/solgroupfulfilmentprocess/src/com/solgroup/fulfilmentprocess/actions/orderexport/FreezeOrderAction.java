package com.solgroup.fulfilmentprocess.actions.orderexport;

import com.solgroup.core.enums.ActionToLegacyEnum;
import com.solgroup.core.model.SubOrderModel;
import com.solgroup.core.model.SubOrderProcessModel;
import com.solgroup.service.utils.SolgroupWebServiceUtils;
import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class FreezeOrderAction extends AbstractSimpleDecisionAction<SubOrderProcessModel> {
    private static final Logger LOG = Logger.getLogger(FreezeOrderAction.class);
    private ModelService modelService;
    private B2BOrderService b2bOrderService;

    @Override public Transition executeAction(final SubOrderProcessModel process) {

        final SubOrderModel subOrder = process.getSubOrder();
        OrderModel order = null;
        String orderCode = subOrder.getOrder().getCode();

        try {
            order = getB2bOrderService().getOrderForCode(orderCode);
        } catch (Exception e) {
            LOG.error(e);
            LOG.error(String.format("Error retrieving the order on hybris with code [%s] !", orderCode));
        }

        try {
            if (order != null){
                //orderModel.setUpdateStatus(SolgroupWebServiceUtils.subOrderProcessStatus.ORDER_FREEZED.toString());
            	order.setFreezed(Boolean.TRUE);
                getModelService().save(order);
//                getModelService().refresh(order);

                subOrder.setActionToLegacy(ActionToLegacyEnum.SUBORDER_FREEZED);
                getModelService().save(subOrder);
//                getModelService().refresh(subOrder);

                return Transition.OK;
            } else {
                LOG.error(String.format("Error!! orderModel null [%s] !!!", orderCode));
                LOG.error(String.format("Error saving orderModel freezed [%s] !!!", orderCode));
                return Transition.NOK;
            }
        } catch (Exception e) {

            LOG.error(e);
            LOG.error(String.format("Error saving orderModel freezed [%s] !!!", e.getMessage()));
            return Transition.NOK;
        }
    }

    public ModelService getModelService() {
        return modelService;
    }

    @Required public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public B2BOrderService getB2bOrderService() {
        return b2bOrderService;
    }

    @Required public void setB2bOrderService(final B2BOrderService b2bOrderService) {
        this.b2bOrderService = b2bOrderService;
    }

}

