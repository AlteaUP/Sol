package com.solgroup.facades.abstractOrder.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.abstractOrder.SolGroupAbstractOrderService;
import com.solgroup.facades.abstractOrder.SolGroupAbstractOrderFacade;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.QuoteService;

public class DefaultSolGroupAbstractOrderFacade implements SolGroupAbstractOrderFacade {

	
	private QuoteService quoteService;
	private CartService cartService;
	private SolGroupAbstractOrderService solGroupAbstractOrderService;
	
	
	@Override
	public Boolean updateAbstractOrderEntryAttribute(String abstractOrderType, String abstractOrderId, String productCode, String propertyCode,
			String propertyValue) {

		AbstractOrderModel abstractOrder = null;
		if(abstractOrderType.equals(CartModel._TYPECODE)) {
			abstractOrder = getCartService().getSessionCart();
		}
		else if(abstractOrderType.equals(QuoteModel._TYPECODE)) {
			abstractOrder = getQuoteService().getCurrentQuoteForCode(abstractOrderId);
		}

		if(abstractOrder!=null) {
			return getSolGroupAbstractOrderService().updateAbstractOrderEntryAttribute(abstractOrder, productCode, propertyCode, propertyValue);
		}

		return Boolean.FALSE;
	}

	@Override
	public Boolean updateAbstractOrderEntryAttribute(String abstractOrderType, String abstractOrderId, Long entryNumber, String propertyCode,
			String propertyValue) {
		AbstractOrderModel abstractOrder = null;
		if(abstractOrderType.equals(CartModel._TYPECODE)) {
			abstractOrder = getCartService().getSessionCart();
		}
		else if(abstractOrderType.equals(QuoteModel._TYPECODE)) {
			abstractOrder = getQuoteService().getCurrentQuoteForCode(abstractOrderId);
		}

		if(abstractOrder!=null) {
			return getSolGroupAbstractOrderService().updateAbstractOrderEntryAttribute(abstractOrder, entryNumber, propertyCode, propertyValue);
		}

		return Boolean.FALSE;
	}

	
	@Override
	public Boolean updateAllAbstractOrderEntryAttribute(String abstractOrderType, String abstractOrderId,
			Map<String, Object> propertiesMap) {

		AbstractOrderModel abstractOrder = null;
		if(abstractOrderType.equals(CartModel._TYPECODE)) {
			abstractOrder = getCartService().getSessionCart();
		}
		else if(abstractOrderType.equals(QuoteModel._TYPECODE)) {
			abstractOrder = getQuoteService().getCurrentQuoteForCode(abstractOrderId);
		}

		if(abstractOrder!=null) {
			return getSolGroupAbstractOrderService().updateAllAbstractOrderEntryAttribute(abstractOrder, propertiesMap);
		}

		return Boolean.FALSE;
	}

	
	protected QuoteService getQuoteService() {
		return quoteService;
	}

	@Required
	public void setQuoteService(QuoteService quoteService) {
		this.quoteService = quoteService;
	}

	protected CartService getCartService() {
		return cartService;
	}

	@Required
	public void setCartService(CartService cartService) {
		this.cartService = cartService;
	}

	protected SolGroupAbstractOrderService getSolGroupAbstractOrderService() {
		return solGroupAbstractOrderService;
	}

	@Required
	public void setSolGroupAbstractOrderService(SolGroupAbstractOrderService solGroupAbstractOrderService) {
		this.solGroupAbstractOrderService = solGroupAbstractOrderService;
	}

	
	
	
	
	

}
