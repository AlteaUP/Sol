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
package com.solgroup.core.event;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.company.SolGroupCompanyService;

import de.hybris.platform.acceleratorservices.site.AbstractAcceleratorSiteEventListener;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.enums.SiteChannel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.orderprocessing.events.OrderPlacedEvent;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;


/**
 * Listener for order confirmation events.
 */
public class OrderConfirmationEventListener extends AbstractAcceleratorSiteEventListener<OrderPlacedEvent> {

    private ModelService modelService;
    private BusinessProcessService businessProcessService;
    private SolGroupCompanyService solgroupCompanyService;

    protected BusinessProcessService getBusinessProcessService() {
        return businessProcessService;
    }

    @Required
    public void setBusinessProcessService(final BusinessProcessService businessProcessService) {
        this.businessProcessService = businessProcessService;
    }

    protected ModelService getModelService() {
        return modelService;
    }

    @Required
    public void setModelService(final ModelService modelService) {
        this.modelService = modelService;
    }

    @Override
    protected void onSiteEvent(final OrderPlacedEvent orderPlacedEvent) {
        final OrderModel orderModel = orderPlacedEvent.getProcess().getOrder();
        final OrderProcessModel orderProcessModel1 = (OrderProcessModel) getBusinessProcessService().createProcess(
                "orderConfirmationEmailProcess-" + orderModel.getCode() + "-" + System.currentTimeMillis(),
                "orderConfirmationEmailProcess");
        orderProcessModel1.setOrder(orderModel);
        getModelService().save(orderProcessModel1);
        getBusinessProcessService().startProcess(orderProcessModel1);

        // Email per l'associazione dell'agente al customer
        final EmployeeModel employeeModel = getSolgroupCompanyService().findAssignedAgents(orderModel.getUnit());
        if (employeeModel == null) {
            // if (StringUtils.isEmpty(orderModel.getUnit().getVendorCode())) {
            final OrderProcessModel orderProcessModel2 = (OrderProcessModel) getBusinessProcessService().createProcess(
                    "agentAssociationRequestEmailProcess-" + orderModel.getCode() + "-" + System.currentTimeMillis(),
                    "agentAssociationRequestEmailProcess");
            orderProcessModel2.setOrder(orderModel);
            getModelService().save(orderProcessModel2);
            getBusinessProcessService().startProcess(orderProcessModel2);
        }

    }

    @Override
    protected SiteChannel getSiteChannelForEvent(final OrderPlacedEvent event) {
        final OrderModel order = event.getProcess().getOrder();
        ServicesUtil.validateParameterNotNullStandardMessage("event.order", order);
        final BaseSiteModel site = order.getSite();
        ServicesUtil.validateParameterNotNullStandardMessage("event.order.site", site);
        return site.getChannel();
    }

    protected SolGroupCompanyService getSolgroupCompanyService() {
        return solgroupCompanyService;
    }

    @Required
    public void setSolgroupCompanyService(final SolGroupCompanyService solgroupCompanyService) {
        this.solgroupCompanyService = solgroupCompanyService;
    }
}
