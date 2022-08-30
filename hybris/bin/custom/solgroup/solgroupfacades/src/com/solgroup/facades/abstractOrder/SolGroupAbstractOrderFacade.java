package com.solgroup.facades.abstractOrder;

import java.util.Map;


public interface SolGroupAbstractOrderFacade {

	Boolean updateAbstractOrderEntryAttribute(String abstractOrderType, String abstractOrderId, String productCode, String propertyCode, String propertyValue);
	
	Boolean updateAbstractOrderEntryAttribute(String abstractOrderType, String abstractOrderId, Long entryNumber, String propertyCode, String propertyValue);
	
	Boolean updateAllAbstractOrderEntryAttribute(String abstractOrderType, String abstractOrderId, Map<String, Object> propertiesMap);
	
}
