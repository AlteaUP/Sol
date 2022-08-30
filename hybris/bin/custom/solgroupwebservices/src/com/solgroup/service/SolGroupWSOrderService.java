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
package com.solgroup.service;

import com.solgroup.core.enums.ActionToLegacyEnum;
import com.solgroup.core.enums.SubOrderStatus;
import com.solgroup.core.model.SubOrderModel;
import com.solgroup.core.ws.services.orderIntegrationLegacy.xml.Order;



public interface SolGroupWSOrderService
{
	void setSubOrderStatus(SubOrderModel subOrder, SubOrderStatus subOrderStatus, String errorDescription);

	void setSubOrderActionToLegacy(SubOrderModel subOrder, ActionToLegacyEnum actionToLegacy);
	
	void sendOrderInsert(SubOrderModel SubOrder) throws Exception;
	
	void checkSubOrderConfirmAction(SubOrderModel subOrderModel, Order wsOrder) throws Exception;
	
	
	


}
