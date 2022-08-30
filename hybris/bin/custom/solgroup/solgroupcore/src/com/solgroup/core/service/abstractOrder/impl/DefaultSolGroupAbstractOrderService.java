package com.solgroup.core.service.abstractOrder.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.service.abstractOrder.SolGroupAbstractOrderService;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

public class DefaultSolGroupAbstractOrderService implements SolGroupAbstractOrderService {
	
	private ModelService modelService;
	private Logger LOG = Logger.getLogger(DefaultSolGroupAbstractOrderService.class);

	@Override
	public Boolean updateAbstractOrderEntryAttribute(AbstractOrderModel abstractOrder, String productCode, String propertyCode, String propertyValue) {

		ServicesUtil.validateParameterNotNull(abstractOrder, "AbstractOrder can not be null");
		Collection<AbstractOrderEntryModel> abstractOrderEntryList = abstractOrder.getEntries();
		
		if(CollectionUtils.isEmpty(abstractOrderEntryList)) {
			return Boolean.FALSE;
		}
		
		for(AbstractOrderEntryModel abstractOrderEntry : abstractOrderEntryList) {
			if(abstractOrderEntry.getProduct().getCode().equals(productCode)) {
				updateAbstractOrderEntryAttributeInternal(abstractOrderEntry, propertyCode, propertyValue);
				return Boolean.TRUE;
			}
		}
		
		return Boolean.FALSE;
	}
	
	@Override
	public Boolean updateAbstractOrderEntryAttribute(AbstractOrderModel abstractOrder, Long entryNumber, String propertyCode, String propertyValue) {

		ServicesUtil.validateParameterNotNull(abstractOrder, "AbstractOrder can not be null");
		Collection<AbstractOrderEntryModel> abstractOrderEntryList = abstractOrder.getEntries();
		
		if(CollectionUtils.isEmpty(abstractOrderEntryList)) {
			return Boolean.FALSE;
		}

		for(AbstractOrderEntryModel abstractOrderEntry : abstractOrderEntryList) {
			if(abstractOrderEntry.getEntryNumber().intValue() == entryNumber.intValue()) {
				updateAbstractOrderEntryAttributeInternal(abstractOrderEntry, propertyCode, propertyValue);
				return Boolean.TRUE;
			}
		}

		
		return Boolean.FALSE;
		
	}
	
	
	private void updateAbstractOrderEntryAttributeInternal(AbstractOrderEntryModel abstractOrderEntry, String propertyCode, String propertyValue) {
		
		if(propertyCode.equals(SolgroupCoreConstants.ORDERENTRY_PURCHASE_ORDER_NUMER)) {
			abstractOrderEntry.setPurchaseOrderNumber(propertyValue);
		}
		else if(propertyCode.equals(SolgroupCoreConstants.ORDERENTRY_CIG)) {
			abstractOrderEntry.setCgi(propertyValue);
		}
		else if(propertyCode.equals(SolgroupCoreConstants.ORDERENTRY_CUP)) {
			abstractOrderEntry.setCup(propertyValue);
		}
		else if(propertyCode.equals(SolgroupCoreConstants.ORDERENTRY_ORDER_DATA)) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SolgroupCoreConstants.ORDERENTRY_ORDER_DATA_FORMAT);
			java.util.Date date = null;
			try {
				date = simpleDateFormat.parse(propertyValue);
			} catch (ParseException e) {
			}
			
			if(date != null) {
				abstractOrderEntry.setDataOrdine(date);
			}
		}
		
		getModelService().save(abstractOrderEntry);
		
	}

	@Override
	public Boolean updateAllAbstractOrderEntryAttribute(AbstractOrderModel abstractOrder,
			Map<String, Object> propertiesMap) {

		ServicesUtil.validateParameterNotNull(abstractOrder, "AbstractOrder can not be null");
		if(CollectionUtils.isNotEmpty(abstractOrder.getEntries())) {
			for(AbstractOrderEntryModel abstractOrderEntry : abstractOrder.getEntries()) {

				if(propertiesMap.containsKey(SolgroupCoreConstants.ORDERENTRY_PURCHASE_ORDER_NUMER) && propertiesMap.get(SolgroupCoreConstants.ORDERENTRY_PURCHASE_ORDER_NUMER)!=null) {
					abstractOrderEntry.setPurchaseOrderNumber(propertiesMap.get(SolgroupCoreConstants.ORDERENTRY_PURCHASE_ORDER_NUMER).toString());
				}
				
				if(propertiesMap.containsKey(SolgroupCoreConstants.ORDERENTRY_CUP) && propertiesMap.get(SolgroupCoreConstants.ORDERENTRY_CUP)!=null) {
					abstractOrderEntry.setCup(propertiesMap.get(SolgroupCoreConstants.ORDERENTRY_CUP).toString());
				}

				if(propertiesMap.containsKey(SolgroupCoreConstants.ORDERENTRY_CIG) && propertiesMap.get(SolgroupCoreConstants.ORDERENTRY_CIG)!=null) {
					abstractOrderEntry.setCgi(propertiesMap.get(SolgroupCoreConstants.ORDERENTRY_CIG).toString());
				}

				if(propertiesMap.containsKey(SolgroupCoreConstants.ORDERENTRY_ORDER_DATA) && propertiesMap.get(SolgroupCoreConstants.ORDERENTRY_ORDER_DATA)!=null && propertiesMap.get(SolgroupCoreConstants.ORDERENTRY_ORDER_DATA) instanceof java.util.Date) {
					abstractOrderEntry.setDataOrdine((java.util.Date)propertiesMap.get(SolgroupCoreConstants.ORDERENTRY_ORDER_DATA));
				}
				
				getModelService().save(abstractOrderEntry);

			}
		}
		
		return Boolean.TRUE;
	}

	
	protected ModelService getModelService() {
		return modelService;
	}

	@Required
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}


	
	
	

}
