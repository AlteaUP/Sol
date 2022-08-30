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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.common.daos.CommonDao;
import com.solgroup.core.model.LegacySystemModel;
import com.solgroup.core.model.SubOrderEntryModel;
import com.solgroup.core.model.SubOrderModel;
import com.solgroup.core.service.consignment.SolGroupConsignmentService;
import com.solgroup.core.ws.services.orderConsignment.xml.Consignment;
import com.solgroup.fulfilmentprocess.actions.consignment.AllowShipmentAction.Transition;
import com.solgroup.service.utils.SolgroupWebServiceUtils;
import com.solgroup.ws.importws.DefaultOrderConsignmentResponseWS;

import de.hybris.platform.acceleratorservices.enums.CheckoutFlowEnum;
import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.GenericSearchConstants.LOG;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import de.hybris.platform.task.RetryLaterException;

/**
 * Check Consignment.
 */
public class CheckConsignmentAction extends AbstractAction<ConsignmentProcessModel> {
    private CommonDao commonDao;
    private GenericDao<ConsignmentModel> consignmentGenericDao;
    private B2BOrderService b2bOrderService;
    private SolGroupConsignmentService solGroupConsignmentService;
    
    
    private final Logger LOG = Logger.getLogger(CheckConsignmentAction.class);

    
    
    public enum Transition {
        NOTFINAL, FINAL, OK;

        public static Set<String> getStringValues() {
            final Set<String> res = new HashSet<String>();

            for (final Transition transition : Transition.values()) {
                res.add(transition.toString());
            }
            return res;
        }
    }

    public CommonDao getCommonDao() {
        return commonDao;
    }

    public void setCommonDao(CommonDao commonDao) {
        this.commonDao = commonDao;
    }

    public GenericDao<ConsignmentModel> getConsignmentGenericDao() {
        return consignmentGenericDao;
    }

    public void setConsignmentGenericDao(GenericDao<ConsignmentModel> consignmentGenericDao) {
        this.consignmentGenericDao = consignmentGenericDao;
    }

    public B2BOrderService getB2bOrderService() {
        return b2bOrderService;
    }

    @Required public void setB2bOrderService(final B2BOrderService b2bOrderService) {
        this.b2bOrderService = b2bOrderService;
    }

    @Override public Set<String> getTransitions() {
        return Transition.getStringValues();
    }

    @Override public String execute(ConsignmentProcessModel process) throws RetryLaterException, Exception {

        final ConsignmentModel consignment = process.getConsignment();

        Set<String> consignmentStatus = new HashSet<String>();

        for (ConsignmentEntryModel consignmentEntryModel : consignment.getConsignmentEntries()) {
            consignmentStatus.add(consignmentEntryModel.getStatus().getCode());
        }

        String returnTransition = Transition.FINAL.toString();

        if (consignmentStatus.contains(ConsignmentStatus.DELETED.getCode())) {
            consignment.setStatus(ConsignmentStatus.DELETED);

        } else if (consignmentStatus.contains(ConsignmentStatus.DELIVERED.getCode())) {
            consignment.setStatus(ConsignmentStatus.DELIVERED);

        } else if (consignmentStatus.contains(ConsignmentStatus.CANCELLED.getCode())) {
            consignment.setStatus(ConsignmentStatus.CANCELLED);

        } else {

        	if(getSolGroupConsignmentService().isFinalStatus(consignment.getStatus().getCode())) {
                LOG.warn(String.format("Consignment [%s] is already in final status [%s]", consignment.getCode(), consignment.getStatus().getCode()));
        		returnTransition = Transition.OK.toString();
        	}
        	else {
        		
        		if (consignmentStatus.contains(ConsignmentStatus.SHIPPED.getCode())) {
        			consignment.setStatus(ConsignmentStatus.SHIPPED);
        		}
        		else if (consignmentStatus.contains(ConsignmentStatus.PLANNED.getCode())) {
        			consignment.setStatus(ConsignmentStatus.PLANNED);
        		}
        		else if (consignmentStatus.contains(ConsignmentStatus.ACCEPTED.getCode())) {
        			consignment.setStatus(ConsignmentStatus.ACCEPTED);
        		}

        		returnTransition = Transition.NOTFINAL.toString();
        		
        	}
        	
        }
        	
        getModelService().save(consignment);
        LOG.info("Consignment " + consignment.getCode() + " status: " + consignment.getStatus());

        return returnTransition;

    }

	protected SolGroupConsignmentService getSolGroupConsignmentService() {
		return solGroupConsignmentService;
	}

	@Required
	public void setSolGroupConsignmentService(SolGroupConsignmentService solGroupConsignmentService) {
		this.solGroupConsignmentService = solGroupConsignmentService;
	}


}
