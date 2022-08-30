package com.solgroup.facades.populators.cart;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.commercefacades.order.data.InvoicePaymentInfoData;
import com.solgroup.core.service.company.SolGroupCompanyService;

import de.hybris.platform.b2bacceleratorfacades.order.populators.B2BCartPopulator;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.core.model.order.payment.InvoicePaymentInfoModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

public class SolGroupB2BCartPopulator<T extends CartData> extends B2BCartPopulator<T> {
	
	private Converter<InvoicePaymentInfoModel, InvoicePaymentInfoData> invoicePaymentInfoConverter;
    private SolGroupCompanyService solgroupCompanyService;
    private ConfigurationService configurationService;
	
	@Override
	public void populate(final CartModel source, final T target) throws ConversionException
	{
		super.populate(source, target);
		addPaymentInformation(source, target);
        showAgentVoucher(source, target);

	}
	
	
	@Override
    protected void addPaymentInformation(final AbstractOrderModel source, final AbstractOrderData prototype)
	{
		final PaymentInfoModel paymentInfo = source.getPaymentInfo();
		if (paymentInfo instanceof CreditCardPaymentInfoModel)
		{
			final CCPaymentInfoData ccPaymentInfoData = getCreditCardPaymentInfoConverter().convert((CreditCardPaymentInfoModel) paymentInfo);
			prototype.setPaymentInfo(ccPaymentInfoData);
		}
		else if(paymentInfo instanceof InvoicePaymentInfoModel) {
			final InvoicePaymentInfoData invoicePaymentInfoData = getInvoicePaymentInfoConverter().convert((InvoicePaymentInfoModel)paymentInfo);
			prototype.setInvoicePaymentInfo(invoicePaymentInfoData);
		}
		else {
			final InvoicePaymentInfoData invoicePaymentInfoData = new InvoicePaymentInfoData();
			invoicePaymentInfoData.setShowDeliveryCost(Boolean.FALSE);
			invoicePaymentInfoData.setShowTax(Boolean.FALSE);
			prototype.setInvoicePaymentInfo(invoicePaymentInfoData);
		}
	}

    private void showAgentVoucher(final AbstractOrderModel source, final AbstractOrderData target) {
        final EmployeeModel employeeModel = getSolgroupCompanyService().findAssignedAgents(source.getUnit());
        if (employeeModel != null) {
            target.setShowAgentVoucher(Boolean.FALSE);
        } else {
            target.setShowAgentVoucher(Boolean.TRUE);
            if (StringUtils.isNotEmpty(source.getAgentVoucher())) {
                target.setAgentVoucher(source.getAgentVoucher());
            } else {
                target.setAgentVoucher(getConfigurationService().getConfiguration().getString("default.agent.voucher"));
            }
        }
    }

	protected Converter<InvoicePaymentInfoModel, InvoicePaymentInfoData> getInvoicePaymentInfoConverter() {
		return invoicePaymentInfoConverter;
	}

	@Required
	public void setInvoicePaymentInfoConverter(
			final Converter<InvoicePaymentInfoModel, InvoicePaymentInfoData> invoicePaymentInfoConverter) {
		this.invoicePaymentInfoConverter = invoicePaymentInfoConverter;
	}

    protected SolGroupCompanyService getSolgroupCompanyService() {
        return solgroupCompanyService;
    }

    @Required
    public void setSolgroupCompanyService(final SolGroupCompanyService solgroupCompanyService) {
        this.solgroupCompanyService = solgroupCompanyService;
    }

    protected ConfigurationService getConfigurationService() {
        return configurationService;
    }

    @Required
    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
