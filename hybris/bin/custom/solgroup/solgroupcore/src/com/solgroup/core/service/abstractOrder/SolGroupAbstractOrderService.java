package com.solgroup.core.service.abstractOrder;

import java.util.Map;

import de.hybris.platform.core.model.order.AbstractOrderModel;

public interface SolGroupAbstractOrderService {
	
	Boolean updateAbstractOrderEntryAttribute(AbstractOrderModel abstractOrder, String productCode, String propertyCode, String propertyValue);
	
	Boolean updateAbstractOrderEntryAttribute(AbstractOrderModel abstractOrder, Long entryNumber, String propertyCode, String propertyValue);
	
	Boolean updateAllAbstractOrderEntryAttribute(AbstractOrderModel abstractOrder, Map<String,Object> propertiesMap);
	

}
