package com.solgroup.core.service.checkout;

import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

public interface SolGroupB2BCommerceCheckoutService {

    PaymentTransactionEntryModel authorizePayment(final CommerceCheckoutParameter parameter);
    
    String getNoteFromSessionCart();
    
    String getAgentVoucherFromSessionCart();

    void setNoteInSessionCart(String note);

    void setAgentVoucherInSessionCart(String agentVoucher);

}
