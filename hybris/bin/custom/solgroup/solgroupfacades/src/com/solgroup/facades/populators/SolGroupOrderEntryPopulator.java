package com.solgroup.facades.populators;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.commercefacades.product.SolGroupPriceDataFactory;
import com.solgroup.core.constants.SolgroupCoreConstants;

import de.hybris.platform.commercefacades.order.converters.populator.OrderEntryPopulator;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;

public class SolGroupOrderEntryPopulator extends OrderEntryPopulator {

    @Resource(name = "configurationService")
    ConfigurationService configurationService;

    private SolGroupPriceDataFactory solGroupPriceDataFactory;

    @Override
    public void populate(final AbstractOrderEntryModel source, final OrderEntryData target) {

        super.populate(source, target);

        if (source.getQuantity() != null) {
            target.setUnit(source.getUnit().getCode());
            target.setCgi(source.getCgi());
            target.setCup(source.getCup());
            target.setPurchaseOrderNumber(source.getPurchaseOrderNumber());

            SimpleDateFormat sdf = new SimpleDateFormat(SolgroupCoreConstants.ORDERENTRY_ORDER_DATA_FORMAT);
            if (source.getDataOrdine() != null) {
                target.setDataOrdine(sdf.format(source.getDataOrdine()));
            }

            addTotals(source, target);

            if (BooleanUtils.isTrue(source.getRefill())) {
                target.setShowPrice(false);
            } else {
                target.setShowPrice(true);
            }
        }

    }

    @Override
    protected void addTotals(final AbstractOrderEntryModel orderEntry, final OrderEntryData entry) {
        if (orderEntry.getBasePrice() != null) {
            entry.setBasePrice(createPriceWithUnitMeasure(orderEntry, orderEntry.getBasePrice()));
        }
        if (orderEntry.getTotalPrice() != null) {
            entry.setTotalPrice(createPrice(orderEntry, orderEntry.getTotalPrice()));
        }
    }
 
    @Override
	protected void addCommon(final AbstractOrderEntryModel orderEntry, final OrderEntryData entry)
	{

		final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    	entry.setEntryNumber(orderEntry.getEntryNumber());
		entry.setQuantity(orderEntry.getQuantity());
		entry.setPurchaseOrderNumber(orderEntry.getPurchaseOrderNumber());
		entry.setCgi(orderEntry.getCgi());
		entry.setCup(orderEntry.getCup());
		Date orderDate = orderEntry.getDataOrdine();
		if(orderDate!=null) {
			entry.setDataOrdine(sdf.format(orderDate));
		}
		adjustUpdateable(entry, orderEntry);
	}


    protected PriceData createPriceWithUnitMeasure(final AbstractOrderEntryModel orderEntry, final Double val) {
        return getSolGroupPriceDataFactory().create(PriceDataType.BUY, BigDecimal.valueOf(val.doubleValue()),
                orderEntry.getOrder().getCurrency().getIsocode(), orderEntry.getUnit().getCode());
    }

    protected SolGroupPriceDataFactory getSolGroupPriceDataFactory() {
        return solGroupPriceDataFactory;
    }

    @Required
    public void setSolGroupPriceDataFactory(SolGroupPriceDataFactory solGroupPriceDataFactory) {
        this.solGroupPriceDataFactory = solGroupPriceDataFactory;
    }

}
