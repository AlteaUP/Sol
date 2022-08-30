package com.solgroup.core.service.order.impl;

import java.util.Set;


import com.solgroup.core.service.order.SolGroupOrderService;

import de.hybris.platform.core.enums.OrderStatus;
import jersey.repackaged.com.google.common.collect.Sets;

public class DefaultSolGroupOrderService implements SolGroupOrderService {

	@Override
	public boolean isFinalStatus(String orderStatus) {
		
		Set<String> finalStatuses = Sets.newHashSet();
		finalStatuses.add(OrderStatus.DELIVERED.getCode());
		finalStatuses.add(OrderStatus.CANCELLED.getCode());
		
		if(finalStatuses.contains(orderStatus)) {
			return true;
		}
		
		return false;
	}

}
