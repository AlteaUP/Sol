package com.solgroup.core.service.checkout.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.checkout.SolGroupB2BCommerceCheckoutService;

import de.hybris.platform.b2bacceleratorservices.order.impl.DefaultB2BCommerceCheckoutService;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.InvoicePaymentInfoModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.dto.TransactionStatusDetails;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;

public class DefaultSolGroupB2BCommerceCheckoutService extends DefaultB2BCommerceCheckoutService
        implements SolGroupB2BCommerceCheckoutService {

    private static final String CREDIT_STATE_ACCEPTED_VALUE = "1";

    private CartService cartService;

    @Override
    public PaymentTransactionEntryModel authorizePayment(final CommerceCheckoutParameter parameter) {
        final CartModel cartModel = parameter.getCart();
        validateParameterNotNull(cartModel, "Cart model cannot be null");
        // validateParameterNotNull(cartModel.getPaymentInfo(), "Payment information on cart cannot be null");

        if (cartModel.getPaymentInfo() instanceof InvoicePaymentInfoModel || cartModel.getPaymentInfo() == null) {
            // create payment transaction
            final PaymentTransactionModel transaction = getModelService().create(PaymentTransactionModel.class);
            final BigDecimal amount = calculateAuthAmount(cartModel);
            transaction.setCode(getGenerateMerchantTransactionCodeStrategy().generateCode(cartModel));
            transaction.setPlannedAmount(amount);
            transaction.setOrder(cartModel);
            transaction.setInfo(cartModel.getPaymentInfo());
            final PaymentTransactionType paymentTransactionType = PaymentTransactionType.AUTHORIZATION;
            final String newEntryCode = getPaymentService().getNewPaymentTransactionEntryCode(transaction, paymentTransactionType);

            // create payment transaction entry. Always successful for account payment
            final PaymentTransactionEntryModel entry = getModelService().create(PaymentTransactionEntryModel.class);
            entry.setAmount(amount);
            entry.setCurrency(cartModel.getCurrency());
            entry.setType(PaymentTransactionType.AUTHORIZATION);
            entry.setTime(new Date());
            entry.setPaymentTransaction(transaction);
            if (cartModel.getUnit().getCreditState().getCode().equals(CREDIT_STATE_ACCEPTED_VALUE)) {
                entry.setTransactionStatus(TransactionStatus.ACCEPTED.name());
                entry.setTransactionStatusDetails(TransactionStatusDetails.SUCCESFULL.toString());
            } else {
                entry.setTransactionStatus(TransactionStatus.REJECTED.name());
                entry.setTransactionStatusDetails(TransactionStatusDetails.CREDIT_LIMIT_REACHED.toString());
            }
            entry.setCode(newEntryCode);
            getModelService().saveAll(cartModel, transaction, entry);

            return entry;
        }
        return super.authorizePayment(parameter);
    }

    @Override
    public String getNoteFromSessionCart() {
        final CartModel cart = getCartService().getSessionCart();
        if (cart != null && cart.getNote() != null)
            return cart.getNote();
        return "";
    }

    public String getAgentVoucherFromSessionCart() {
        final CartModel cart = getCartService().getSessionCart();
        if (cart != null && cart.getAgentVoucher() != null)
            return cart.getAgentVoucher();
        return "";
    }

    @Override
    public void setNoteInSessionCart(final String note) {
        final CartModel cart = getCartService().getSessionCart();
        if (cart != null) {
            cart.setNote(note);
            getModelService().save(cart);
        }
    }

    @Override
    public void setAgentVoucherInSessionCart(final String agentVoucher) {
        final CartModel cart = getCartService().getSessionCart();
        if (cart != null) {
            cart.setAgentVoucher(agentVoucher);
            getModelService().save(cart);
        }
    }

    protected CartService getCartService() {
        return cartService;
    }

    @Required
    public void setCartService(final CartService cartService) {
        this.cartService = cartService;
    }

}
