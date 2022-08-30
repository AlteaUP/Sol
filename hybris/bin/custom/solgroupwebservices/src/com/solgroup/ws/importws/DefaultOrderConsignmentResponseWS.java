/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2017 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.solgroup.ws.importws;


import com.solgroup.constants.SolgroupwebservicesConstants;
import com.solgroup.core.common.daos.CommonDao;
import com.solgroup.core.enums.ActionToLegacyEnum;
import com.solgroup.core.enums.SubOrderProcessStatus;
import com.solgroup.core.enums.SubOrderStatus;
import com.solgroup.core.model.LegacySystemModel;
import com.solgroup.core.model.SubOrderEntryModel;
import com.solgroup.core.model.SubOrderModel;
import com.solgroup.core.model.SubOrderProcessModel;
import com.solgroup.core.service.consignment.SolGroupConsignmentService;
import com.solgroup.core.service.product.SolGroupProductService;
import com.solgroup.core.ws.services.orderConsignment.xml.OrderConsignmentHybris;
import com.solgroup.service.SolGroupWSOrderService;
import com.solgroup.service.utils.SolgroupWebServiceUtils;
import com.solgroup.core.ws.services.orderConsignment.xml.Consignment;
import com.solgroup.core.ws.services.orderConsignment.xml.Header;
import com.solgroup.core.ws.services.orderConsignment.xml.Order;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.enums.ProcessState;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import jersey.repackaged.com.google.common.collect.Sets;

import java.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.log4j.Logger;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.ws.service.CommonWsService;
import com.solgroup.ws.ImportWS;

import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.basecommerce.enums.ConsignmentStatus;

/**
 *
 */

public class DefaultOrderConsignmentResponseWS implements ImportWS<OrderConsignmentHybris> {

    private CommonWsService commonWsService;
    private B2BOrderService b2bOrderService;
    private CommonDao commonDao;
    private BusinessProcessService businessProcessService;
    private SolGroupWSOrderService solGroupWSOrderService;
    private ModelService modelService;
    private GenericDao<SubOrderModel> subOrderGenericDao;
    private SolGroupConsignmentService solGroupConsignmentService;
    private SolGroupProductService solGroupProductService;
    private EnumerationService enumerationService;
    private SessionService sessionService;
    private UserService userService;
    
    private final Logger LOG = Logger.getLogger(DefaultOrderConsignmentResponseWS.class);

    @Override
    public void importData(final OrderConsignmentHybris data) throws Exception {

        // Call Process
    	try {
    		processConsignments(data);
    	}catch(Exception exc) {
    		LOG.error(exc.getMessage(), exc);
    		throw exc;
    	}
        LOG.info("WS DefaultOrderConsignmentResponseWS processed");
    }


    private void processConsignments(final OrderConsignmentHybris data) throws Exception {

        if (data == null || data.getOrder() == null || data.getHeader() == null) {
            throw new Exception("Invalid Request");
        }

        // Get order in header
        final Order wsOrder = data.getOrder();
        final Header wsHeader = data.getHeader();

        LOG.info("Process LegacySystem " + wsHeader.getSourceSystem());
        LOG.info("Process Order " + wsOrder.getOrderCode());

		if (wsOrder.getOrderCode() == null) {
			throw new Exception("Message without order code");
		}
		if(wsHeader.getSourceSystem() == null) {
			throw new Exception("Message without source system");
		}
        
        
        // STEP CHECK 1 : get order by code
        LOG.info("Get order by code " + wsOrder.getOrderCode());
        final OrderModel order = getB2bOrderService().getOrderForCode(wsOrder.getOrderCode());
        // Order not found --> skip
        if (order == null) {
            LOG.error(String.format("No order retrieved for code [%s] !!!", wsOrder.getOrderCode()));
            throw new Exception(String.format("No order retrieved for code [%s] !!!", wsOrder.getOrderCode()));
        }
        
       
        
        // STEP CHECK 2 : get legacy system
        LOG.info("Get legacySystem item model for " + wsHeader.getSourceSystem());
        LegacySystemModel legacySystem = getCommonDao()
                .findItemByUniqueCode(LegacySystemModel._TYPECODE, LegacySystemModel.CODE, wsHeader.getSourceSystem(),
                        LegacySystemModel.class);
        if (legacySystem == null || legacySystem.getCode().isEmpty()) {
            throw new Exception("Error! The source System is not valid!");
        }
        
        // STEP CHECK 3 : check if message has consignments for current order
        LOG.info("Check consignments list size");
        if(CollectionUtils.isEmpty(wsOrder.getConsignments().getConsignment())) {
        	throw new Exception("No consignment received for order " + wsOrder.getOrderCode());
        }

        
            
        // STEP CHECK 4 : get subOrder
        LOG.info("Get subOrder for order " + wsOrder.getOrderCode() + " and legacySystem " + wsHeader.getSourceSystem());
        SubOrderModel subOrder = retrieveSubOrder(order, legacySystem);
        if(subOrder==null) {
            throw new Exception(String.format(
                    "Error!! No subOrder found for order [%s] and legacySystem [%s]",
                    wsOrder.getOrderCode(), wsHeader.getSourceSystem()));
        }
            

        // STEP CHECK 5 : check if message contains consignments with invalid products
        LOG.info("Check if message contains consignments with invalid products");
        Set<String> subOrderProductCodes = Sets.newHashSet();
        for(SubOrderEntryModel subOrderEntry : subOrder.getSubOrderEntry()) {
        	subOrderProductCodes.add(subOrderEntry.getOrderEntry().getProduct().getErpCode());
        }
        
        for (Consignment consignment : wsOrder.getConsignments().getConsignment()) {
            boolean invalidProductCode = subOrderProductCodes.contains(consignment.getProductCode());
            
            if (!invalidProductCode) {
                throw new Exception(String.format(
                        "Error!! Consignment [%s] contains product [%s] not included in subOrder [%s] [%s] !!!",
                        consignment.getConsignmentCode(), invalidProductCode, wsHeader.getSourceSystem(), wsOrder.getOrderCode()));
            }
        }


        // STEP 6 : Freeze subOrder
        LOG.info("Freeze subOrder");
        freezeSubOrder(subOrder);
        
        
    	Set<ConsignmentModel> consignmentSet = Sets.newHashSet();
    	if(CollectionUtils.isNotEmpty(order.getConsignments())) {
    		consignmentSet.addAll(order.getConsignments());
    	}
    	
        for (Consignment wsConsignment : wsOrder.getConsignments().getConsignment()) {

            final String wsConsignmentCode = wsConsignment.getConsignmentCode();

            LOG.info(String.format("Processing consignment : code [%s], product [%s], orderEntryNumber [%s]", wsConsignmentCode, wsConsignment.getProductCode(), wsConsignment.getOrderEntryNumber()));
            

            // Get consignment from order
            ConsignmentModel consignment = consignmentSet.stream().filter(currentConsignment -> currentConsignment.getCode().equals(wsConsignmentCode)).findAny().orElse(null);
            
            // No consignment wiht current code found into order --> Create it
            if(consignment==null) {
            	consignment = getModelService().create(ConsignmentModel.class);
                consignment.setCode(wsConsignmentCode);
                consignment.setWarehouse(order.getStore().getWarehouses().get(0));
                consignment.setShippingAddress(order.getDeliveryAddress());
                consignment.setOrder(order);
                consignment.setStatus(ConsignmentStatus.ACCEPTED);
            }

            // Set consignment parameter
            consignment.setTrackingID(wsConsignment.getTrackingNumber());
            consignment.setTrackingLink(wsConsignment.getTrackingLink());
            consignment.setDocumentType(wsConsignment.getDocumentType());
            consignment.setDocumentNumber(wsConsignment.getDocumentNumber());
            consignment.setLegacySystem(legacySystem);

            // Get consignment entry from consignment
            ConsignmentEntryModel consignmentEntry = null;
            if(consignment.getPk()!=null) {
            	final ConsignmentModel myConsignment = consignment;
            	consignmentEntry = sessionService.executeInLocalView(new SessionExecutionBody()
				{
					@Override
					public ConsignmentEntryModel execute()
					{
						return getSolGroupConsignmentService().getConsignmentEntryByProductCode(myConsignment, getSolGroupProductService().getHybrisProductCode(wsConsignment.getProductCode(),order.getSite()));
						
					}
				}, userService.getAdminUser());
            }
            // No consignment entry found for product code --> Create it
            if(consignmentEntry==null) {
            	consignmentEntry = getModelService().create(ConsignmentEntryModel.class);
                OrderEntryModel orderEntry = retrieveOrderEntriesModel(order,wsConsignment.getProductCode());
                consignmentEntry.setConsignment(consignment);
                consignmentEntry.setOrderEntry(orderEntry);
            }

            String wsConsignmentStatus = wsConsignment.getStatus();
            if(wsConsignmentStatus.equalsIgnoreCase("CANCELED")) {
            	wsConsignmentStatus = "CANCELLED";
            }

            
            //Check if consignment entry status can be updated. If not --> continue
            if(consignmentEntry.getStatus()!=null && getSolGroupConsignmentService().isFinalStatus(consignmentEntry.getStatus().getCode()) && !getSolGroupConsignmentService().isFinalStatus(wsConsignmentStatus)) {
        		LOG.error(String.format("Consignment entry [%s] [%s] can not be updated. It is already in final status [%s]. New status is [%s]", wsConsignment.getConsignmentCode(), wsConsignment.getProductCode(), consignmentEntry.getStatus().getCode(), wsConsignment.getStatus()));
        		continue;
            }
            
            Long quantity = Long.valueOf(wsConsignment.getQuantity());
            consignmentEntry.setQuantity(quantity);
            
            
            ConsignmentStatus consignmentStatus = getEnumerationService().getEnumerationValue(ConsignmentStatus.class,wsConsignmentStatus);
            consignmentEntry.setStatus(consignmentStatus);
            consignmentEntry.setShippedQuantity(quantity);

            // Save consignmentEntry and consignment
            getModelService().save(consignmentEntry);
            getModelService().save(consignment);
            
            consignmentSet.add(consignment);

        }
        
    	for(ConsignmentModel consignment : consignmentSet) {
    		startConsignmentProcess(consignment, order.getOrderProcess().iterator().next(), legacySystem);
    	}
            

    }

    private SubOrderModel retrieveSubOrder(OrderModel oderModel, LegacySystemModel legacySystemModel) {

        return oderModel.getSubOrders().stream()
                .filter(subOrder -> subOrder.getLegacySystem().getCode().equalsIgnoreCase(legacySystemModel.getCode()))
                .findAny().orElse(null);

    }

    private void freezeSubOrder(SubOrderModel subOrder)
            throws Exception {
     
    	if (subOrder.getActionToLegacy().equals(ActionToLegacyEnum.SUBORDER_FREEZED)) {
    		return;
    	}
    	
        if (subOrder.getActionToLegacy().equals(ActionToLegacyEnum.CREATED_TO_LEGACY) && subOrder.getStatus().equals(SubOrderStatus.OK)) {
            final String subOrderProcessCode =
                    subOrder.getOrder().getCode() + "_" + subOrder.getLegacySystem().getCode() + "_" + SolgroupwebservicesConstants.SUBORDER_EXPORT_PROCESS;

            final String eventName = subOrderProcessCode + "_externalAction";
            SubOrderProcessModel subOrderProcess = getBusinessProcessService().getProcess(subOrderProcessCode);
            if (subOrderProcess != null) {
                if(BooleanUtils.isFalse(subOrder.getOrder().getFreezed())) {
	            	LOG.info(String.format("Set status [%s] for subOrderProcess [%s]", SubOrderProcessStatus.ORDER_FREEZED.getCode(), subOrderProcess.getCode()));
	                subOrderProcess.setStatus(SubOrderProcessStatus.ORDER_FREEZED);
	            	getModelService().save(subOrderProcess);
	            	LOG.info("Triggering event " + eventName);
	                getBusinessProcessService().triggerEvent(eventName);
                }
                else {
                	LOG.info(String.format("Order [%s] is already freezed", subOrder.getOrder().getCode()));
                }

            } else {
            	throw new Exception("No business process for code " + subOrderProcessCode);
            }
            
        } else {
            throw new Exception("subOrder " + subOrder.getOrder().getCode() + " (" + subOrder.getLegacySystem().getCode() + ") actionToLegacy or status is not valid to receive consignment. Current subOrder.actionToLegacy is " + subOrder.getActionToLegacy().getCode() + " and subOrder.status is " + subOrder.getStatus().getCode());
        }
    }

    private void startConsignmentProcess(ConsignmentModel consignment, OrderProcessModel orderProcess, LegacySystemModel legacySystem) throws Exception {

		String consignmentProcessCode = consignment.getOrder().getCode() + "_" + legacySystem.getCode() + "_" + consignment.getCode() + "_"
                + SolgroupwebservicesConstants.CONSIGNMENT_PROCESS_NAME;

    	
        ConsignmentProcessModel consignmentProcess = getBusinessProcessService().getProcess(consignmentProcessCode);

        // Check if the consignmentProcess is setted (trigger the process in wait)
        if (consignmentProcess != null) {

            if (consignmentProcess.getProcessState().toString().equals(ProcessState.WAITING.toString())) {
                final String eventName = consignmentProcessCode + "_waitForConsignmentUpdate";
//                consignmentProcess.setConsignment(consignment);
//                saveProcessModel(consignmentProcess);
                LOG.info(String.format("Trigger event [%s] for process [%s]", eventName, consignmentProcessCode));
                getBusinessProcessService().triggerEvent(eventName);
            } 
            //else if (consignmentProcess.getProcessState().equals(ProcessState.SUCCEEDED) && ArrayUtils.contains(SolgroupWebServiceUtils.ConsignmentStateFinalArray,consignment.getStatus().toString())) {
            	
            else if (consignmentProcess.getProcessState().equals(ProcessState.SUCCEEDED) && getSolGroupConsignmentService().isFinalStatus(consignment.getStatus().getCode())) {

                //Risveglio il processo in stato finale per cambiarlo in uno stato finale
                LOG.info("Consignment (" + consignment.getCode() + ") is already in a FINAL status! Consignment process is in state " + consignmentProcess.getProcessState().getCode() + " -> " + "Raise event checkConsignment for process " + consignmentProcess.getCode());
                getBusinessProcessService().restartProcess(consignmentProcess, "checkConsignment");
            } 
            //else if (ArrayUtils.contains(SolgroupWebServiceUtils.ConsignmentStateNotFinalArray,consignment.getStatus().toString())) {
            else if (!getSolGroupConsignmentService().isFinalStatus(consignment.getStatus().getCode())) {

                // Voglio risvegliare il processo per cambiare in uno stato non finale e questo non si può fare
            	LOG.error("Consignment (" + consignment.getCode() + ") is already in a FINAL status! Consignment process is in state " + consignmentProcess.getProcessState().getCode() + " -> " + "Process " + consignmentProcess.getCode() + " can not be restarted");
            }
        } else {
            consignmentProcess = getBusinessProcessService().createProcess(consignmentProcessCode, SolgroupwebservicesConstants.CONSIGNMENT_PROCESS_NAME);
            consignmentProcess.setConsignment(consignment);
            consignmentProcess.setParentProcess(orderProcess);
            consignmentProcess.setState(ProcessState.CREATED);
            getModelService().save(consignmentProcess);
            LOG.info("Starting Process: " + consignmentProcessCode);
            getBusinessProcessService().startProcess(consignmentProcess);
        }
    }

//    private void saveProcessModel(ConsignmentProcessModel consignmentProcess) throws Exception {
//
//        try {
//            // Metto a DB l'entry nuova inserita
//            getModelService().save(consignmentProcess);
//            getModelService().refresh(consignmentProcess);
//        } catch (Exception e) {
//
//            LOG.error(String.format("Error saving consignmentProcess [%s] !!!", e.getMessage()));
//            throw new Exception(String.format("Error saving consignmentProcess [%s] !!!", e.getMessage()));
//        }
//    }

    private OrderEntryModel retrieveOrderEntriesModel(OrderModel order, String wsProductCode) {

        return (OrderEntryModel) SolgroupWebServiceUtils
                .findByProperty(order.getEntries(), orderEntry -> orderEntry.getProduct().getErpCode().equals(wsProductCode));

    }

//    private ConsignmentEntryModel retrieveConsignmentEntryModel(ConsignmentModel consignmentModel,
//                                                                Consignment consignment) {
//
//        return consignmentModel.getConsignmentEntries().stream()
//                .filter(consignmentEntry -> consignmentEntry.getOrderEntry().getEntryNumber()
//                        .equals(consignment.getOrderEntryNumber()))
//                .filter(consignmentEntry -> consignmentEntry.getOrderEntry().getProduct().getErpCode()
//                        .equals(consignment.getProductCode())).findFirst().orElse(null);
//
//    }

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
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    protected CommonWsService getCommonWsService() {
        return commonWsService;
    }

    @Required
    public void setCommonWsService(final CommonWsService commonWsService) {
        this.commonWsService = commonWsService;
    }

    protected CommonDao getCommonDao() {
        return commonDao;
    }

    @Required
    public void setCommonDao(final CommonDao commonDao) {
        this.commonDao = commonDao;
    }

    public B2BOrderService getB2bOrderService() {
        return b2bOrderService;
    }

    @Required
    public void setB2bOrderService(final B2BOrderService b2bOrderService) {
        this.b2bOrderService = b2bOrderService;
    }

    protected SolGroupWSOrderService getSolGroupWSOrderService() {
        return solGroupWSOrderService;
    }

    @Required
    public void setSolGroupWSOrderService(SolGroupWSOrderService solGroupWSOrderService) {
        this.solGroupWSOrderService = solGroupWSOrderService;
    }

    protected GenericDao<SubOrderModel> getSubOrderGenericDao() {
        return subOrderGenericDao;
    }

    @Required
    public void setSubOrderGenericDao(GenericDao<SubOrderModel> subOrderGenericDao) {
        this.subOrderGenericDao = subOrderGenericDao;
    }

	protected SolGroupConsignmentService getSolGroupConsignmentService() {
		return solGroupConsignmentService;
	}

	@Required
	public void setSolGroupConsignmentService(SolGroupConsignmentService solGroupConsignmentService) {
		this.solGroupConsignmentService = solGroupConsignmentService;
	}

	protected SolGroupProductService getSolGroupProductService() {
		return solGroupProductService;
	}

	@Required
	public void setSolGroupProductService(SolGroupProductService solGroupProductService) {
		this.solGroupProductService = solGroupProductService;
	}

	protected EnumerationService getEnumerationService() {
		return enumerationService;
	}

	@Required
	public void setEnumerationService(EnumerationService enumerationService) {
		this.enumerationService = enumerationService;
	}

	protected SessionService getSessionService() {
		return sessionService;
	}

	@Required
	public void setSessionService(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	protected UserService getUserService() {
		return userService;
	}

	@Required
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	
    
    
}
