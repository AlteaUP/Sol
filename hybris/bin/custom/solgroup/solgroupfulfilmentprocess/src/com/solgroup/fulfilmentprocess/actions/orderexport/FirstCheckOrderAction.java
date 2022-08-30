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
package com.solgroup.fulfilmentprocess.actions.orderexport;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;

import org.apache.log4j.Logger;

/**
 * This example action checks the order for required data in the business process. Skipping this action may result in
 * failure in one of the subsequent steps of the process. The relation between the order and the business process is
 * defined in basecommerce extension through item OrderProcess. Therefore if your business process has to access the
 * order (a typical case), it is recommended to use the OrderProcess as a parentClass instead of the plain
 * BusinessProcess.
 */
public class FirstCheckOrderAction extends AbstractSimpleDecisionAction<OrderProcessModel> {
    private static final Logger LOG = Logger.getLogger(FirstCheckOrderAction.class);

    @Override public Transition executeAction(final OrderProcessModel process) {
        final OrderModel order = process.getOrder();
        //if order is create
        if (order == null) {
            LOG.error("Missing the order, exiting the process");
            return Transition.NOK;
        }
        //Order must have some lines
        if (order.getEntries().isEmpty()) {
            LOG.error("Order must have some lines");
            return Transition.NOK;
        }
        if (order.getUnit() == null) {
            LOG.error("Order must have a b2bUnit");
            return Transition.NOK;
        }
        // Order must have an overall delivery mode
        /*if (order.getDeliveryMode() == null) {
            LOG.error("Order must have an overall delivery mode");
            return Transition.NOK;
        }*/

        if (order.getDeliveryAddress() == null) {
            LOG.error("Order must have a delivery address");
            return Transition.NOK;
        }
        return Transition.OK;
    }
}
