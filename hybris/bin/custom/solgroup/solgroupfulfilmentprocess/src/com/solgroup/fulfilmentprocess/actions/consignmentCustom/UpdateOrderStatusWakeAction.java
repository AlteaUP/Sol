/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.solgroup.fulfilmentprocess.actions.consignmentCustom;

import com.solgroup.core.model.SubOrderModel;
import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.processengine.enums.ProcessState;

import java.util.Collection;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

/**
 * Check Consignment.
 */
public class UpdateOrderStatusWakeAction extends AbstractProceduralAction<ConsignmentProcessModel> {
    private B2BOrderService b2bOrderService;
    private BusinessProcessService businessProcessService;
    
    private final Logger LOG = Logger.getLogger(UpdateOrderStatusWakeAction.class);

    @Override public void executeAction(final ConsignmentProcessModel process) throws Exception {
        //risveglio l'order status per controllare che tutti i consignments siano in stato finale e settare lo stato dell'ordine stesso
        final ConsignmentModel consignment = process.getConsignment();
        if (consignment != null) {

            OrderModel orderModel = (OrderModel) consignment.getOrder();
            String orderCode = orderModel.getCode();

            OrderProcessModel orderProcessModel = null;
            Collection<OrderProcessModel> orderProcessModels = orderModel.getOrderProcess();
            if (orderProcessModels.size() > 0) {
                Optional<OrderProcessModel> orderProcessModelOptional = orderProcessModels.stream().findFirst();
                orderProcessModel = orderProcessModelOptional.get();
            } else {

                LOG.error(String.format(
                        "On the BBP we assumed there was only one process per Order, If an errore arise please change this logic!!! Number of OrderProcess:  [%s] ",
                        orderModel.getOrderProcess().size()));
            }

            try {
                if (orderProcessModel != null) {
                    ProcessState processState = orderProcessModel.getProcessState();
                    if (processState != null) {
                        LOG.info("Process state = " + processState.getCode());
                    }

                    if (processState != null && processState.equals(ProcessState.SUCCEEDED)) {
                        LOG.info("Restart orderProcess from action updateOrderStatus");
                        getBusinessProcessService().restartProcess(orderProcessModel, "updateOrderStatus");
                    } else if (processState != null && processState.equals(ProcessState.WAITING)) {
                        String orderProcessCode = orderProcessModel.getCode();
                        final String eventName = orderProcessCode + "_waitForConsignmentFinal";
                        LOG.info("Triggering process: " + orderProcessCode);
                        getBusinessProcessService().triggerEvent(eventName);
                    }

                } else {

                    LOG.error("Error impossible to retrive any orderProcessModel for orderCode: " + orderCode);
                }
            } catch (Exception e) {
                LOG.error("Error triggering orderProcess for orderCode: " + orderCode + " process: " + orderProcessModel
                        .getCode());
            }
        }
    }

    public B2BOrderService getB2bOrderService() {
        return b2bOrderService;
    }

    @Required public void setB2bOrderService(final B2BOrderService b2bOrderService) {
        this.b2bOrderService = b2bOrderService;
    }

    protected BusinessProcessService getBusinessProcessService() {
        return businessProcessService;
    }

    @Required public void setBusinessProcessService(final BusinessProcessService businessProcessService) {
        this.businessProcessService = businessProcessService;
    }
}
