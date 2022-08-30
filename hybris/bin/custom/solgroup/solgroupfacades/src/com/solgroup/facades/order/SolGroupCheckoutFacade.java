package com.solgroup.facades.order;


import java.util.List;

import de.hybris.platform.b2bacceleratorfacades.api.cart.CheckoutFacade;
import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BDaysOfWeekData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.InvalidCartException;

public interface SolGroupCheckoutFacade extends CheckoutFacade{
	
	public boolean setDeliveryAddressIfAvailable();
	public CartData getCheckoutCart();
	@Override
    public List<B2BPaymentTypeData> getPaymentTypes();
	@Override
    public CartData updateCheckoutCart(final CartData cartData);
	public boolean hasValidCart();
	public boolean hasNoDeliveryAddress();
	public boolean hasNoDeliveryMode();
	public boolean hasNoPaymentInfo();
	public boolean hasShippingItems();
	@Override
    public List<B2BDaysOfWeekData> getDaysOfWeekForReplenishmentCheckoutSummary();
	@Override
    public <T extends AbstractOrderData> T placeOrder(final PlaceOrderData placeOrderData) throws InvalidCartException;
	public boolean containsTaxValues();
	public CartData updatePurchaseOrder(String purchaseOrderNumber, Long entryNumber);
	public CartData updateMultiPurchaseOrder(String purchaseOrderNumber, String productCode);
	public CartData updateMultiCgi(String cgi, String productCode);
	public CartData updateCgi(String cgi, Long entryNumber);
	public CartData updateMultiCup(String cup, String productCode);
	public CartData updateCup(String cup, Long entryNumber);
	public CartData updateMultiDataOrdine(String dataOrdine, String productCode);
	public CartData updateDataOrdine(String dataOrdine, Long entryNumber);
	public OrderData getOrderDetailsForCode(final String code);
	public AddressData getDeliveryAddressForCode(final String code);
	public boolean setDeliveryAddress(final AddressData addressData);
	public boolean isExpressCheckoutEnabledForStore();
	public void setPaymentTypeForCart(final String paymentType, final CartModel cartModel);
	public boolean authorizePayment(final String securityCode);
	public String getNoteFromSessionCart();
    public String getAgentVoucherFromSessionCart();
    public void updateNoteInSessionCart(String note, String agentVoucher);
	public CartData getDefaultCheckoutCart();
	

}
