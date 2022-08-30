package com.solgroup.facades.order.impl;

import static de.hybris.platform.util.localization.Localization.getLocalizedString;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.util.Assert;

import com.solgroup.commercefacades.order.data.InvoicePaymentInfoData;
import com.solgroup.core.service.cart.SolGroupB2BCommerceCartService;
import com.solgroup.core.service.checkout.SolGroupB2BCommerceCheckoutService;
import com.solgroup.core.service.company.SolGroupCompanyService;
import com.solgroup.facades.order.SolGroupCheckoutFacade;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BCommentData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BDaysOfWeekData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BReplenishmentRecurrenceEnum;
import de.hybris.platform.b2bacceleratorfacades.order.data.TriggerData;
import de.hybris.platform.b2bacceleratorfacades.order.impl.DefaultB2BCheckoutFacade;
import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.PriceDataFactory;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.InvoicePaymentInfoModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.cronjob.enums.DayOfWeek;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.BaseStoreModel;

public class DefaultSolGroupCheckoutFacade extends DefaultB2BCheckoutFacade implements SolGroupCheckoutFacade {

    private static final String CART_CHECKOUT_PAYMENTTYPE_INVALID = "cart.paymenttype.invalid";
    private static final String CART_CHECKOUT_DELIVERYADDRESS_INVALID = "cart.deliveryAddress.invalid";
    private static final String CART_CHECKOUT_TRANSACTION_NOT_AUTHORIZED = "cart.transation.notAuthorized";
    private static final String CART_CHECKOUT_PAYMENTINFO_EMPTY = "cart.paymentInfo.empty";
    private static final String CART_CHECKOUT_NOT_CALCULATED = "cart.not.calculated";
    private static final String CART_CHECKOUT_TERM_UNCHECKED = "cart.term.unchecked";
    private static final String CART_CHECKOUT_NO_QUOTE_DESCRIPTION = "cart.no.quote.description";
    private static final String CART_CHECKOUT_QUOTE_REQUIREMENTS_NOT_SATISFIED = "cart.quote.requirements.not.satisfied";
    private static final String CART_CHECKOUT_REPLENISHMENT_NO_STARTDATE = "cart.replenishment.no.startdate";
    private static final String CART_CHECKOUT_REPLENISHMENT_NO_FREQUENCY = "cart.replenishment.no.frequency";
    private static final String ORDER_NOT_FOUND_FOR_USER_AND_BASE_STORE = "Order with guid %s not found for current user in current BaseStore";

    @Resource(name = "priceDataFactory")
    PriceDataFactory priceDataFactory;

    @Resource(name = "modelService")
    ModelService modelService;

    @Resource(name = "cartService")
    CartService cartService;

    @Resource(name = "b2bCommerceCartService")
    private SolGroupB2BCommerceCartService commerceCartService;

    @Resource(name = "b2bCommerceCheckoutService")
    private SolGroupB2BCommerceCheckoutService b2bCommerceCheckoutService;

    @Resource(name = "invoicePaymentInfoConverter")
    private Converter<InvoicePaymentInfoModel, InvoicePaymentInfoData> invoicePaymentInfoConverter;
    
    @Resource(name = "solgroupCompanyService")
    private SolGroupCompanyService solgroupCompanyService;

    @Override
    public CartData updateCheckoutCart(final CartData cartData) {
        final CartModel cartModel = getCart();
        if (cartModel == null)
            return null;
        // set payment type
        if (cartData.getPaymentType() != null) {
            final String newPaymentTypeCode = cartData.getPaymentType().getCode();

            setPaymentTypeForCart(newPaymentTypeCode, cartModel);
        }

        getModelService().save(cartModel);
        return getCheckoutCart();

    }

    @Override
    public CartData getDefaultCheckoutCart() {
        final CartData cartData = getCartFacade().getSessionCart();
        updateCheckoutCart(cartData);
        return cartData;
    }

    protected String getNote() {
        final CartModel cartModel = getCart();
        if (cartModel != null)
            return cartModel.getNote();
        return "";
    }

    @Override
    public boolean setDeliveryAddress(final AddressData addressData) {
        final CartModel cartModel = getCart();
        if (cartModel != null) {
            AddressModel addressModel = null;
            if (addressData != null) {
                addressModel = addressData.getId() == null ? createDeliveryAddressModel(addressData, cartModel)
                        : getDeliveryAddressModelForCode(addressData.getId());
            }

            final CommerceCheckoutParameter parameter = createCommerceCheckoutParameter(cartModel, true);
            parameter.setAddress(addressModel);
            parameter.setIsDeliveryAddress(false);
            return getCommerceCheckoutService().setDeliveryAddress(parameter);
        }
        return false;
    }

    protected OrderEntryData getPurchaseOrderNumber(final OrderEntryData entry) {

        final CartModel cart = getCart();

        final List<AbstractOrderEntryModel> entries = cart.getEntries();
        for (final AbstractOrderEntryModel entryModel : entries) {
            if (entryModel.getEntryNumber().compareTo(entry.getEntryNumber()) == 0) {
                entry.setPurchaseOrderNumber(entryModel.getPurchaseOrderNumber());
            }
        }

        return entry;
    }

    protected OrderEntryData getCgi(final OrderEntryData entry) {

        final CartModel cart = getCart();

        final List<AbstractOrderEntryModel> entries = cart.getEntries();
        for (final AbstractOrderEntryModel entryModel : entries) {
            if (entryModel.getEntryNumber().compareTo(entry.getEntryNumber()) == 0) {
                entry.setCgi(entryModel.getCgi());
            }
        }

        return entry;
    }

    protected OrderEntryData getCup(final OrderEntryData entry) {

        final CartModel cart = getCart();

        final List<AbstractOrderEntryModel> entries = cart.getEntries();
        for (final AbstractOrderEntryModel entryModel : entries) {
            if (entryModel.getEntryNumber().compareTo(entry.getEntryNumber()) == 0) {
                entry.setCup(entryModel.getCup());
            }
        }

        return entry;
    }

    protected PriceData getDeliveryCost() {
        final CartModel cart = getCart();

        return cart == null || cart.getDeliveryCost() == null ? null
                : priceDataFactory.create(PriceDataType.BUY, BigDecimal.valueOf(cart.getDeliveryCost()),
                        cart.getCurrency());
    }

    @Override
    public AddressData getDeliveryAddressForCode(final String code) {
        Assert.notNull(code, "Parameter code cannot be null.");
        for (final AddressData address : getSupportedDeliveryAddresses(false)) {
            if (code.equals(address.getId()))
                return address;
        }
        return null;
    }

    @Override
    public List<AddressData> getSupportedDeliveryAddresses(final boolean visibleAddressesOnly) {
        final CartModel cartModel = getCart();
        return cartModel == null ? Collections.emptyList()
                : getAddressConverter().convertAll(
                        getDeliveryService().getSupportedDeliveryAddressesForOrder(cartModel, visibleAddressesOnly));
    }

    @Override
    public boolean setDeliveryAddressIfAvailable() {
        final CartModel cartModel = getCart();

        if (cartModel == null)
            return false;

        if (cartModel.getDeliveryAddress() != null)
            return true;

        // final B2BCustomerModel currentUser = (B2BCustomerModel)getCurrentUserForCheckout();
        // CompanyModel company = currentUser.getDefaultB2BUnit();

        final B2BUnitModel b2bUnit = cartModel.getUnit();

        AddressModel shippingAddress = null;
        if (b2bUnit.getShippingAddress() != null) {
            shippingAddress = b2bUnit.getShippingAddress();
        } else if (CollectionUtils.isNotEmpty(b2bUnit.getAddresses())) {
            for (final AddressModel address : b2bUnit.getAddresses()) {
                if (BooleanUtils.isTrue(address.getShippingAddress())) {
                    shippingAddress = address;
                    break;
                }
            }
        }

        if (shippingAddress != null) {
            final CommerceCheckoutParameter parameter = createCommerceCheckoutParameter(cartModel, true);
            parameter.setAddress(shippingAddress);
            parameter.setIsDeliveryAddress(true);
            return getCommerceCheckoutService().setDeliveryAddress(parameter);
        }

        return false;

        // Collection<AddressModel> addresses = company.getAddresses();
        // AddressModel currentUserDefaultShipmentAddress = null;
        // for(AddressModel address : addresses){
        // if(address.getShippingAddress()){
        // currentUserDefaultShipmentAddress = address;
        // break;
        // }
        // }
        //
        //
        //
        // if (cartModel.getUser().equals(currentUser))
        // {
        //
        // if (currentUserDefaultShipmentAddress != null)
        // {
        // final AddressModel supportedDeliveryAddress =
        // getDeliveryAddressModelForCode(currentUserDefaultShipmentAddress
        // .getPk().toString());
        // if (supportedDeliveryAddress != null)
        // {
        // final CommerceCheckoutParameter parameter = createCommerceCheckoutParameter(cartModel, true);
        // parameter.setAddress(supportedDeliveryAddress);
        // parameter.setIsDeliveryAddress(true);
        // return getCommerceCheckoutService().setDeliveryAddress(parameter);
        // }
        // }
        // }
        //
        // // Could not use default address, try any address
        // final List<AddressModel> supportedDeliveryAddresses =
        // getDeliveryService().getSupportedDeliveryAddressesForOrder(cartModel,
        // true);
        // if (supportedDeliveryAddresses != null && !supportedDeliveryAddresses.isEmpty())
        // {
        // final CommerceCheckoutParameter parameter = createCommerceCheckoutParameter(cartModel, true);
        // parameter.setAddress(supportedDeliveryAddresses.get(0));
        // parameter.setIsDeliveryAddress(true);
        // return getCommerceCheckoutService().setDeliveryAddress(parameter);
        // }
        // return false;
    }

    @Override
    protected AddressData getDeliveryAddress() {
        final CartModel cart = getCart();
        if (cart != null) {
            final AddressModel deliveryAddress = cart.getDeliveryAddress();
            if (deliveryAddress != null) {
                // Ensure that the delivery address is in the set of supported addresses
                final AddressModel supportedAddress = getDeliveryAddressModelForCode(
                        deliveryAddress.getPk().toString());
                if (supportedAddress != null)
                    return getAddressConverter().convert(supportedAddress);
            }
        }
        return null;
    }

    @Override
    public List<B2BPaymentTypeData> getPaymentTypes() {
        final List<CheckoutPaymentType> tempCheckoutPaymentTypes = getEnumerationService()
                .getEnumerationValues(CheckoutPaymentType._TYPECODE);
        final List<CheckoutPaymentType> checkoutPaymentTypes = new ArrayList<CheckoutPaymentType>();
        for (final CheckoutPaymentType checkoutPaymentType : tempCheckoutPaymentTypes) {
            if (!checkoutPaymentType.getCode().equalsIgnoreCase(CheckoutPaymentType.CARD.getCode())) {
                checkoutPaymentTypes.add(checkoutPaymentType);
            }
        }

        return Converters.convertAll(checkoutPaymentTypes, getB2bPaymentTypeDataConverter());
    }

    @Override
    public List<B2BDaysOfWeekData> getDaysOfWeekForReplenishmentCheckoutSummary() {
        final List<DayOfWeek> daysOfWeek = getEnumerationService().getEnumerationValues(DayOfWeek._TYPECODE);

        return Converters.convertAll(daysOfWeek, getB2bDaysOfWeekConverter());
    }

    @Override
    public <T extends AbstractOrderData> T placeOrder(final PlaceOrderData placeOrderData) throws InvalidCartException {
        // term must be checked
        if (!placeOrderData.getTermsCheck().equals(Boolean.TRUE))
            throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_TERM_UNCHECKED));

        // for CARD type, transaction must be authorized before placing order
        boolean isCardtPaymentType = false;
        final CheckoutPaymentType checkoutPaymentType = getCart().getPaymentType();
        if (checkoutPaymentType != null) {
            isCardtPaymentType = CheckoutPaymentType.CARD.getCode().equals(checkoutPaymentType.getCode());
        }
        // final boolean isCardtPaymentType =
        // CheckoutPaymentType.CARD.getCode().equals(getCart().getPaymentType().getCode());
        if (isCardtPaymentType) {
            final List<PaymentTransactionModel> transactions = getCart().getPaymentTransactions();
            boolean authorized = false;
            for (final PaymentTransactionModel transaction : transactions) {
                for (final PaymentTransactionEntryModel entry : transaction.getEntries()) {
                    if (entry.getType().equals(PaymentTransactionType.AUTHORIZATION)
                            && TransactionStatus.ACCEPTED.name().equals(entry.getTransactionStatus())) {
                        authorized = true;
                        break;
                    }
                }
            }
            if (!authorized)
                // FIXME - change error message
                throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_TRANSACTION_NOT_AUTHORIZED));
        }

        if (isValidCheckoutCart(placeOrderData)) {
            // validate quote negotiation
            if (placeOrderData.getNegotiateQuote() != null && placeOrderData.getNegotiateQuote().equals(Boolean.TRUE)) {
                if (StringUtils.isBlank(placeOrderData.getQuoteRequestDescription()))
                    throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_NO_QUOTE_DESCRIPTION));
                else {
                    final B2BCommentData b2BComment = new B2BCommentData();
                    b2BComment.setComment(placeOrderData.getQuoteRequestDescription());

                    final CartData cartData = new CartData();
                    cartData.setB2BComment(b2BComment);

                    updateCheckoutCart(cartData);
                }
            }

            // validate replenishment
            if (placeOrderData.getReplenishmentOrder() != null
                    && placeOrderData.getReplenishmentOrder().equals(Boolean.TRUE)) {
                if (placeOrderData.getReplenishmentStartDate() == null)
                    throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_REPLENISHMENT_NO_STARTDATE));

                if (placeOrderData.getReplenishmentRecurrence().equals(B2BReplenishmentRecurrenceEnum.WEEKLY)
                        && CollectionUtils.isEmpty(placeOrderData.getNDaysOfWeek()))
                    throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_REPLENISHMENT_NO_FREQUENCY));

                final TriggerData triggerData = new TriggerData();
                populateTriggerDataFromPlaceOrderData(placeOrderData, triggerData);

                return (T) scheduleOrder(triggerData);
            }
            final AbstractOrderData orderData = super.placeOrder();
            final PriceData deliveryCost = priceDataFactory.create(PriceDataType.BUY,
                    BigDecimal.valueOf(getCart().getDeliveryCost()), getCart().getCurrency());
            orderData.setDeliveryCost(deliveryCost);
            return (T) orderData;
        }

        return null;
    }

    @Override
    protected boolean isValidCheckoutCart(final PlaceOrderData placeOrderData) {
        final CartData cartData = getCheckoutCart();
        final boolean valid = true;

        if (!cartData.isCalculated())
            throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_NOT_CALCULATED));

        if (cartData.getDeliveryAddress() == null)
            throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_DELIVERYADDRESS_INVALID));

        // if (cartData.getDeliveryMode() == null)
        // {
        // throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_DELIVERYMODE_INVALID));
        // }

        final boolean accountPaymentType = CheckoutPaymentType.ACCOUNT.getCode()
                .equals(cartData.getPaymentType().getCode());
        if (!accountPaymentType && cartData.getPaymentInfo() == null)
            throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_PAYMENTINFO_EMPTY));

        if (Boolean.TRUE.equals(placeOrderData.getNegotiateQuote()) && !cartData.getQuoteAllowed())
            throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_QUOTE_REQUIREMENTS_NOT_SATISFIED));

        return valid;
    }

    @Override
    public boolean hasValidCart() {
        final CartData cartData = getCheckoutCart();
        return cartData.getEntries() != null && !cartData.getEntries().isEmpty();
    }

    @Override
    public boolean hasNoDeliveryAddress() {
        final CartData cartData = getCheckoutCart();
        return hasShippingItems() && (cartData == null || cartData.getDeliveryAddress() == null);
    }

    @Override
    public boolean hasNoDeliveryMode() {
        final CartData cartData = getCheckoutCart();
        return hasShippingItems() && (cartData == null || cartData.getDeliveryMode() == null);
    }

    @Override
    public boolean hasNoPaymentInfo() {
        final CartData cartData = getCheckoutCart();
        return cartData == null || cartData.getPaymentInfo() == null;
    }

    @Override
    public boolean hasShippingItems() {
        return hasItemsMatchingPredicate(e -> e.getDeliveryPointOfService() == null);
    }

    @Override
    public boolean containsTaxValues() {
        final CartModel cartModel = getCart();

        if (cartModel == null)
            return false;

        if (cartModel.getPaymentInfo() == null || cartModel.getPaymentInfo() instanceof InvoicePaymentInfoModel)
            return true;

        if (cartModel.getTotalTaxValues() != null && !cartModel.getTotalTaxValues().isEmpty())
            return true;
        for (final Iterator<AbstractOrderEntryModel> orderEntryModelIterator = cartModel.getEntries()
                .iterator(); orderEntryModelIterator.hasNext();) {
            final AbstractOrderEntryModel entryModel = orderEntryModelIterator.next();
            if (entryModel.getTaxValues() != null && !entryModel.getTaxValues().isEmpty())
                return true;
        }
        return false;
    }

    @Override
    public CartData updatePurchaseOrder(final String purchaseOrderNumber, final Long entryNumber) {
        final CartModel cartModel = getCart();

        final AbstractOrderEntryModel entry = getEntryForNumber(cartModel, entryNumber.toString());

        if (entry != null) {
            entry.setPurchaseOrderNumber(purchaseOrderNumber);

            modelService.save(entry);
            modelService.refresh(entry);
            modelService.refresh(cartModel);

            cartService.setSessionCart(cartModel);
        }

        return getCheckoutCart();
    }

    @Override
    public CartData updateMultiPurchaseOrder(final String purchaseOrderNumber, final String productCode) {
        final CartModel cartModel = getCart();

        final AbstractOrderEntryModel entry = getEntryForNumber(cartModel,
                getOrderEntryNumberForMultiD(productCode).toString());
        if (entry != null) {
            entry.setPurchaseOrderNumber(purchaseOrderNumber);

            modelService.save(entry);
            modelService.refresh(entry);
            modelService.save(cartModel);
            modelService.refresh(cartModel);

            cartService.setSessionCart(cartModel);
        }

        return getCheckoutCart();
    }

    protected Integer getOrderEntryNumberForMultiD(final String productCode) {
        for (final OrderEntryData orderEntry : getCheckoutCart().getEntries()) {
            // check sub entries
            if (CollectionUtils.isNotEmpty(orderEntry.getEntries())) {
                for (final OrderEntryData subEntry : orderEntry.getEntries()) {
                    // find the entry
                    if (subEntry.getProduct().getCode().equals(productCode))
                        return subEntry.getEntryNumber();
                }
            } else {
                if (orderEntry.getProduct().getCode().equals(productCode))
                    return orderEntry.getEntryNumber();

            }
        }
        return null;
    }

    @Override
    public CartData updateCgi(final String cgi, final Long entryNumber) {
        final CartModel cartModel = getCart();

        final AbstractOrderEntryModel entry = getEntryForNumber(cartModel, entryNumber.toString());
        if (entry != null) {
            entry.setCgi(cgi);

            modelService.save(entry);
            modelService.refresh(entry);
            modelService.refresh(cartModel);

            cartService.setSessionCart(cartModel);
        }

        return getCheckoutCart();
    }

    @Override
    public CartData updateMultiCgi(final String cgi, final String productCode) {
        final CartModel cartModel = getCart();

        final AbstractOrderEntryModel entry = getEntryForNumber(cartModel,
                getOrderEntryNumberForMultiD(productCode).toString());

        if (entry != null) {
            entry.setCgi(cgi);

            modelService.save(entry);
            modelService.refresh(entry);
            modelService.save(cartModel);
            modelService.refresh(cartModel);

            cartService.setSessionCart(cartModel);
        }

        return getCheckoutCart();
    }

    @Override
    public CartData updateCup(final String cup, final Long entryNumber) {
        final CartModel cartModel = getCart();

        final AbstractOrderEntryModel entry = getEntryForNumber(cartModel, entryNumber.toString());
        if (entry != null) {
            entry.setCup(cup);

            modelService.save(entry);
            modelService.refresh(entry);
            modelService.refresh(cartModel);

            cartService.setSessionCart(cartModel);
        }

        return getCheckoutCart();
    }

    @Override
    public CartData updateMultiCup(final String cup, final String productCode) {
        final CartModel cartModel = getCart();

        getOrderEntryNumberForMultiD(productCode);
        final AbstractOrderEntryModel entry = getEntryForNumber(cartModel,
                getOrderEntryNumberForMultiD(productCode).toString());

        if (entry != null) {
            entry.setCup(cup);

            modelService.save(entry);
            modelService.refresh(entry);
            modelService.save(cartModel);
            modelService.refresh(cartModel);

            cartService.setSessionCart(cartModel);
        }

        return getCheckoutCart();
    }

    @Override
    public CartData updateDataOrdine(final String dataOrdine, final Long entryNumber) {
        final CartModel cartModel = getCart();

        final SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");
        final AbstractOrderEntryModel entry = getEntryForNumber(cartModel, entryNumber.toString());

        if (entry != null) {

            try {
                entry.setDataOrdine(sdf.parse(dataOrdine));
            } catch (final Exception e) {
                entry.setDataOrdine(null);
            }

            modelService.save(entry);
            modelService.refresh(entry);
            modelService.refresh(cartModel);

            cartService.setSessionCart(cartModel);
        }

        return getCheckoutCart();
    }

    @Override
    public CartData updateMultiDataOrdine(final String dataOrdine, final String productCode) {
        final CartModel cartModel = getCart();

        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        final AbstractOrderEntryModel entry = getEntryForNumber(cartModel,
                getOrderEntryNumberForMultiD(productCode).toString());

        if (entry != null) {
            try {
                entry.setDataOrdine(sdf.parse(dataOrdine));
            } catch (final Exception e) {
                entry.setDataOrdine(null);
            }

            modelService.save(entry);
            modelService.refresh(entry);
            modelService.refresh(cartModel);

            cartService.setSessionCart(cartModel);
        }

        return getCheckoutCart();
    }

    protected AbstractOrderEntryModel getEntryForNumber(final CartModel cartModel, final String entryNumber) {
        final List<AbstractOrderEntryModel> entries = cartModel.getEntries();
        if (entries != null && !entries.isEmpty()) {
            final Integer requestedEntryNumber = Integer.valueOf(entryNumber);
            for (final AbstractOrderEntryModel entry : entries) {
                if (entry != null && requestedEntryNumber.equals(entry.getEntryNumber()))
                    return entry;
            }
        }

        return null;
    }

    @Override
    public OrderData getOrderDetailsForCode(final String code) {
        final BaseStoreModel baseStoreModel = getBaseStoreService().getCurrentBaseStore();

        OrderModel orderModel = null;
        if (getCheckoutCustomerStrategy().isAnonymousCheckout()) {
            orderModel = getCustomerAccountService().getOrderDetailsForGUID(code, baseStoreModel);
        } else {
            try {
                orderModel = getCustomerAccountService()
                        .getOrderForCode((CustomerModel) getUserService().getCurrentUser(), code, baseStoreModel);
            } catch (final ModelNotFoundException e) {
                throw new UnknownIdentifierException(String.format(ORDER_NOT_FOUND_FOR_USER_AND_BASE_STORE, code));
            }
        }

        if (orderModel == null)
            throw new UnknownIdentifierException(String.format(ORDER_NOT_FOUND_FOR_USER_AND_BASE_STORE, code));

        final OrderData orderData = getOrderConverter().convert(orderModel);

        final PriceData deliveryCost = priceDataFactory.create(PriceDataType.BUY,
                BigDecimal.valueOf(orderModel.getDeliveryCost()), orderModel.getCurrency());
        orderData.setDeliveryCost(deliveryCost);

        // orderData.setShowTotal(false);

        return orderData;
    }

    @Override
    public boolean isExpressCheckoutEnabledForStore() {
        return false;
    }

    @Override
    public void setPaymentTypeForCart(final String paymentType, final CartModel cartModel) {
        final List<CheckoutPaymentType> checkoutPaymentTypes = getEnumerationService()
                .getEnumerationValues(CheckoutPaymentType._TYPECODE);
        if (!checkoutPaymentTypes.contains(CheckoutPaymentType.valueOf(paymentType)))
            throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_PAYMENTTYPE_INVALID));

        cartModel.setPaymentType(CheckoutPaymentType.valueOf(paymentType));

        if (CheckoutPaymentType.ACCOUNT.getCode().equals(paymentType)) {
            cartModel.setPaymentInfo(getCommerceCartService().createInvoicePaymentInfo(cartModel));
        } else {
            cartModel.setPaymentInfo(commerceCartService.createHiPayPaymentInfo(cartModel));
        }

        getCommerceCartService().calculateCartForPaymentTypeChange(cartModel);
    }

    protected InvoicePaymentInfoData getInvoicePaymentDetails() {
        final CartModel cart = getCart();
        if (cart != null) {
            final PaymentInfoModel paymentInfo = cart.getPaymentInfo();
            if (paymentInfo instanceof InvoicePaymentInfoModel)
                return invoicePaymentInfoConverter.convert((InvoicePaymentInfoModel) paymentInfo);
        }
        return null;
    }

    @Override
    public String getNoteFromSessionCart() {
        return b2bCommerceCheckoutService.getNoteFromSessionCart();
    }

    @Override
    public String getAgentVoucherFromSessionCart() {
        return b2bCommerceCheckoutService.getAgentVoucherFromSessionCart();
    }

    @Override
    public void updateNoteInSessionCart(final String note, final String agentVoucher) {
        b2bCommerceCheckoutService.setNoteInSessionCart(note);
        b2bCommerceCheckoutService.setAgentVoucherInSessionCart(agentVoucher);
    }

}
