package com.solgroup.core.service.cart.impl;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.b2bunit.SolGroupB2BUnitService;
import com.solgroup.core.service.cart.SolGroupB2BCommerceCartService;
import de.hybris.platform.b2bacceleratorservices.order.impl.DefaultB2BCommerceCartService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.core.model.order.payment.InvoicePaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.impl.DefaultCartService;
import de.hybris.platform.order.CartFactory;

public class DefaultSolGroupB2BCommerceCartService extends DefaultB2BCommerceCartService implements SolGroupB2BCommerceCartService {
	
	
	private SolGroupB2BUnitService solGroupB2BUnitService;
	private CartFactory cartFactory;

    @Override
    public CreditCardPaymentInfoModel createHiPayPaymentInfo(final CartModel cartModel) {

    	final CreditCardPaymentInfoModel paymentInfoModel = getModelService().create(InvoicePaymentInfoModel.class);
        paymentInfoModel.setUser(cartModel.getUser());
        paymentInfoModel.setCode(cartModel.getCode());
        paymentInfoModel.setRequireDeliveryCostCalculation(true);
        paymentInfoModel.setRequireTaxCalculation(true);
        return paymentInfoModel;
    }
    
	@Override
	public InvoicePaymentInfoModel createInvoicePaymentInfo(final CartModel cartModel)
	{
		if(cartModel.getPaymentInfo()!=null && cartModel.getPaymentInfo() instanceof InvoicePaymentInfoModel) {
			return (InvoicePaymentInfoModel)cartModel.getPaymentInfo();
		}
		
		final InvoicePaymentInfoModel invoicePaymentInfoModel = getModelService().create(InvoicePaymentInfoModel.class);
		invoicePaymentInfoModel.setUser(cartModel.getUser());
		invoicePaymentInfoModel.setCode(getGuidKeyGenerator().generate().toString());
		AddressModel billingAddress = getSolGroupB2BUnitService().getBillingAddress(cartModel.getUnit());
		invoicePaymentInfoModel.setBillingAddress(billingAddress);
		return invoicePaymentInfoModel;
	}

	@Override
	public void clearSessionCart() {
		CartModel newCart = getCartFactory().createCart();
		getSessionService().setAttribute(DefaultCartService.SESSION_CART_PARAMETER_NAME, newCart);
	}

	
	protected SolGroupB2BUnitService getSolGroupB2BUnitService() {
		return solGroupB2BUnitService;
	}

	@Required
	public void setSolGroupB2BUnitService(SolGroupB2BUnitService solGroupB2BUnitService) {
		this.solGroupB2BUnitService = solGroupB2BUnitService;
	}

	protected CartFactory getCartFactory() {
		return cartFactory;
	}

	@Required
	public void setCartFactory(CartFactory cartFactory) {
		this.cartFactory = cartFactory;
	}



	
	
    
    
}
