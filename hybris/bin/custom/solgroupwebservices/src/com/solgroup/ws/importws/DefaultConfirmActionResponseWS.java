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

import com.solgroup.core.common.daos.CommonDao;

import com.solgroup.core.model.LegacySystemModel;
import com.solgroup.core.model.SubOrderModel;

import com.solgroup.core.ws.services.orderIntegrationLegacy.xml.ConfirmActionHybris;
import com.solgroup.core.ws.services.orderIntegrationLegacy.xml.Header;
import com.solgroup.core.ws.services.orderIntegrationLegacy.xml.Order;
import com.solgroup.service.SolGroupWSOrderService;

import com.solgroup.service.utils.SolgroupWebServiceUtils;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import jersey.repackaged.com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.ws.service.CommonWsService;
import com.solgroup.ws.ImportWS;

import de.hybris.platform.b2b.services.B2BOrderService;

/**
 *
 */

public class DefaultConfirmActionResponseWS implements ImportWS<ConfirmActionHybris> {

	private B2BOrderService b2bOrderService;
	private CommonDao commonDao;
	private GenericDao<SubOrderModel> subOrderGenericDao;
	private SolGroupWSOrderService solGroupWSOrderService;
	private final Logger LOG = Logger.getLogger(DefaultConfirmActionResponseWS.class);
	

	@Override
	public void importData(final ConfirmActionHybris data) throws Exception {

		// Call Process
		try {
			processLegacySubOrder(data);
		}catch(Exception exc) {
			LOG.error(exc.getMessage(),exc);
			throw exc;
		}

		LOG.info("WS DefaultConfirmActionResponseWS processed");
	}


	private void processLegacySubOrder(final ConfirmActionHybris orderExportResponseImport) throws Exception {

		if (orderExportResponseImport == null || orderExportResponseImport.getOrder() == null
				|| orderExportResponseImport.getHeader() == null) {
			throw new Exception("Invalid Request");
		}

		final Order wsOrder = orderExportResponseImport.getOrder();
		final Header wsHeader = orderExportResponseImport.getHeader();

		LOG.info("Process LegacySystem " + wsHeader.getSourceSystem());
		LOG.info("Process Order " + wsOrder.getCode());

		if (wsOrder.getCode() == null) {
			throw new Exception("Message without order code");
		}
		if(wsHeader.getSourceSystem() == null) {
			throw new Exception("Message without source system");
		}
		if(wsOrder.getStatus() == null) {
			throw new Exception("Message without confirm action status");
		}
		

		// Get order model
		OrderModel order = getB2bOrderService().getOrderForCode(wsOrder.getCode());
		// Order not found --> skip
		if (order == null) {
			throw new Exception(String.format("No order retrieved for code [%s] !!!", wsOrder.getCode()));
		}

		// Order freezed : can not accept a new confim message
		if(order.getFreezed()!=null && order.getFreezed().booleanValue()) {
			LOG.error(String.format("Order [%s] is alredy freezed. Can not accept confirm messages", wsOrder.getCode()));
			return;
		}

		// Get legacy system
		LegacySystemModel legacySystem = getCommonDao().findItemByUniqueCode(LegacySystemModel._TYPECODE,
				LegacySystemModel.CODE, wsHeader.getSourceSystem(), LegacySystemModel.class);
		if (legacySystem == null) {
			throw new Exception(String.format("Source System [%s] is not valid",wsHeader.getSourceSystem()));
		}

		// Get subOrder (from order and legacy system)
		SubOrderModel subOrder = null;
		Map<String, Object> subOrderSearchParams = Maps.newHashMap();
		subOrderSearchParams.put(SubOrderModel.LEGACYSYSTEM, legacySystem);
		subOrderSearchParams.put(SubOrderModel.ORDER, order);
		List<SubOrderModel> subOrdersSearchResult = getSubOrderGenericDao().find(subOrderSearchParams);
		if (CollectionUtils.isNotEmpty(subOrdersSearchResult)) {
			subOrder = subOrdersSearchResult.get(0);
		}
		if(subOrder==null) {
			LOG.error(String.format("No subOrder found for order [%s] and legacy system [%s]", wsOrder.getCode(), wsHeader.getSourceSystem()));
			return;
		}

		LOG.info(String.format("Confirm action status for order [%s] and legacy system [%s] is : [%s]", wsOrder.getCode(), wsHeader.getSourceSystem(), wsOrder.getStatus()));
		getSolGroupWSOrderService().checkSubOrderConfirmAction(subOrder, wsOrder);
		
	}

	

	public B2BOrderService getB2bOrderService() {
		return b2bOrderService;
	}

	@Required
	public void setB2bOrderService(final B2BOrderService b2bOrderService) {
		this.b2bOrderService = b2bOrderService;
	}

	protected CommonDao getCommonDao() {
		return commonDao;
	}

	@Required
	public void setCommonDao(final CommonDao commonDao) {
		this.commonDao = commonDao;
	}


	protected GenericDao<SubOrderModel> getSubOrderGenericDao() {
		return subOrderGenericDao;
	}

	@Required
	public void setSubOrderGenericDao(GenericDao<SubOrderModel> subOrderGenericDao) {
		this.subOrderGenericDao = subOrderGenericDao;
	}

	protected SolGroupWSOrderService getSolGroupWSOrderService() {
		return solGroupWSOrderService;
	}

	@Required
	public void setSolGroupWSOrderService(SolGroupWSOrderService solGroupWSOrderService) {
		this.solGroupWSOrderService = solGroupWSOrderService;
	}


}