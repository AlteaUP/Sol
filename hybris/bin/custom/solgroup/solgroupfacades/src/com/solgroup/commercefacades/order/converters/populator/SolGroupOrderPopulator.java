package com.solgroup.commercefacades.order.converters.populator;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.commercefacades.order.data.InvoicePaymentInfoData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.order.converters.populator.OrderPopulator;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.core.model.order.payment.InvoicePaymentInfoModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.util.mail.MailUtils;

/**
 * @author fmilazzo
 *
 * @param <OrderModel>
 * @param <OrderData>
 */
public class SolGroupOrderPopulator<OrderModel, OrderData> extends OrderPopulator {

    private Converter<InvoicePaymentInfoModel, InvoicePaymentInfoData> invoicePaymentInfoConverter;

    @Override
    protected void addPaymentInformation(final AbstractOrderModel source, final AbstractOrderData prototype) {
        final PaymentInfoModel paymentInfo = source.getPaymentInfo();
        if (paymentInfo instanceof CreditCardPaymentInfoModel) {
            final CCPaymentInfoData paymentInfoData = getCreditCardPaymentInfoConverter()
                    .convert((CreditCardPaymentInfoModel) paymentInfo);
            prototype.setPaymentInfo(paymentInfoData);
        } else if (paymentInfo instanceof InvoicePaymentInfoModel) {
            final InvoicePaymentInfoData paymentInfoData = getInvoicePaymentInfoConverter()
                    .convert((InvoicePaymentInfoModel) paymentInfo);
            prototype.setInvoicePaymentInfo(paymentInfoData);
        }

    }

    @Override
    protected void addCommon(final AbstractOrderModel source, final AbstractOrderData prototype) {
        super.addCommon(source, prototype);
        prototype.setB2bUnitName(source.getUnit().getName());
        prototype.setErpCode(source.getUnit().getErpCode());
        UserModel user = source.getUser();
        if(user instanceof B2BCustomerModel) {
        	B2BCustomerModel b2bCustomer = (B2BCustomerModel)user;
        	if(StringUtils.isNotEmpty(b2bCustomer.getEmail())) {
        		try {
            		MailUtils.validateEmailAddress(b2bCustomer.getEmail(), "b2bCustomer email");
            		prototype.setUserEmail(b2bCustomer.getEmail());
        		}catch(Exception exc) {
        			// No action
        		}
        	}
        }
    }

    protected Converter<InvoicePaymentInfoModel, InvoicePaymentInfoData> getInvoicePaymentInfoConverter() {
        return invoicePaymentInfoConverter;
    }

    @Required
    public void setInvoicePaymentInfoConverter(
            Converter<InvoicePaymentInfoModel, InvoicePaymentInfoData> invoicePaymentInfoConverter) {
        this.invoicePaymentInfoConverter = invoicePaymentInfoConverter;
    }

}
