package com.solgroup.core.service.cart;

import de.hybris.platform.b2bacceleratorservices.order.B2BCommerceCartService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;


public interface SolGroupB2BCommerceCartService extends B2BCommerceCartService{

	CreditCardPaymentInfoModel createHiPayPaymentInfo(final CartModel cartModel);
	
	void clearSessionCart();
}
