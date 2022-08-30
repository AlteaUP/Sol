package com.solgroup.commercefacades.order.converters.populator;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.commercefacades.order.data.InvoicePaymentInfoData;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.payment.InvoicePaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

/**
 * @author fmilazzo
 *
 */
public class SolGroupInvoicePaymentInfoPopulator implements Populator<InvoicePaymentInfoModel, InvoicePaymentInfoData> {

    private Converter<AddressModel, AddressData> addressConverter;

    @Override
    public void populate(InvoicePaymentInfoModel source, InvoicePaymentInfoData target) throws ConversionException {
//        target.setId(source.getPk().toString());
//        target.setSaved(source.isSaved());
        if (source.getBillingAddress() != null) {
            target.setBillingAddress(getAddressConverter().convert(source.getBillingAddress()));
        }
        target.setShowDeliveryCost(source.getRequireDeliveryCostCalculation());
        target.setShowTax(source.getRequireTaxCalculation());
    }

    protected Converter<AddressModel, AddressData> getAddressConverter() {
        return addressConverter;
    }

    @Required
    public void setAddressConverter(Converter<AddressModel, AddressData> addressConverter) {
        this.addressConverter = addressConverter;
    }

}
