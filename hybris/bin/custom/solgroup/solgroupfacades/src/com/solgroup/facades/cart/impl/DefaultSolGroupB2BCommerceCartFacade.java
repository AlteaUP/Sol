package com.solgroup.facades.cart.impl;



import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.cart.SolGroupB2BCommerceCartService;
import com.solgroup.facades.cart.SolGroupB2BCommerceCartFacade;

import de.hybris.platform.b2bacceleratorfacades.order.impl.DefaultB2BCartFacade;


public class DefaultSolGroupB2BCommerceCartFacade extends DefaultB2BCartFacade implements SolGroupB2BCommerceCartFacade{

	private SolGroupB2BCommerceCartService solGroupB2BCommerceCartService;
	
	@Override
	public void clearSessionCart() {
		getSolGroupB2BCommerceCartService().clearSessionCart();
		
	}

	protected SolGroupB2BCommerceCartService getSolGroupB2BCommerceCartService() {
		return solGroupB2BCommerceCartService;
	}

	@Required
	public void setSolGroupB2BCommerceCartService(SolGroupB2BCommerceCartService solGroupB2BCommerceCartService) {
		this.solGroupB2BCommerceCartService = solGroupB2BCommerceCartService;
	}


	

	

}
