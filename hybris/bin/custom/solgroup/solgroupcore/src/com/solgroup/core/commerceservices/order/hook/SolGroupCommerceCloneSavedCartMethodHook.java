package com.solgroup.core.commerceservices.order.hook;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.commerceservices.order.CommerceSaveCartException;
import de.hybris.platform.commerceservices.order.hook.CommerceCloneSavedCartMethodHook;
import de.hybris.platform.commerceservices.service.data.CommerceSaveCartParameter;
import de.hybris.platform.commerceservices.service.data.CommerceSaveCartResult;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.model.ModelService;


public class SolGroupCommerceCloneSavedCartMethodHook implements CommerceCloneSavedCartMethodHook {
	
	private Logger log = Logger.getLogger(SolGroupCommerceCloneSavedCartMethodHook.class);
	
	private ModelService modelService;

	@Override
	public void beforeCloneSavedCart(CommerceSaveCartParameter parameters) throws CommerceSaveCartException {
		CartModel cart = parameters.getCart();
		
		if(cart==null) {
			log.warn("CartModel is null");
			return;
		}
		
		
		cart.setRefillOrderEntryNumber(0);
		getModelService().save(cart);

	}

	@Override
	public void afterCloneSavedCart(CommerceSaveCartParameter parameters, CommerceSaveCartResult saveCartResult)
			throws CommerceSaveCartException {
		
		
		log.info("After cloneSavedCart with id = " + parameters.getCart().getCode());
		int refillEntries = 0;
		Collection<AbstractOrderEntryModel> cartEntriesCollection = parameters.getCart().getEntries();
		if(CollectionUtils.isNotEmpty(cartEntriesCollection)) {
			for(AbstractOrderEntryModel abstractOrderEntry : cartEntriesCollection) {
				if(BooleanUtils.isTrue(abstractOrderEntry.getRefill())) {
					refillEntries++;
				}
			}
		}
		else {
			log.info("Cart " + parameters.getCart().getCode() + " has no entries");
		}
		
		log.info("After cloneSavedCart with id " + parameters.getCart().getCode() + ". Refill entries = " + refillEntries);
		parameters.getCart().setRefillOrderEntryNumber(refillEntries);
		getModelService().save(parameters.getCart());
		
	}

	protected ModelService getModelService() {
		return modelService;
	}

	@Required
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	
	

}
