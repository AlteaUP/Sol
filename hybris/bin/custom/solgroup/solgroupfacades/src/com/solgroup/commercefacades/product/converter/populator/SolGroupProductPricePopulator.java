package com.solgroup.commercefacades.product.converter.populator;

import java.math.BigDecimal;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.commercefacades.product.SolGroupPriceDataFactory;

import de.hybris.platform.commercefacades.product.converters.populator.AbstractProductPopulator;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.price.CommercePriceService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.europe1.jalo.PriceRow;
import de.hybris.platform.jalo.order.price.PriceInformation;
import de.hybris.platform.jalo.product.Unit;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class SolGroupProductPricePopulator<SOURCE extends ProductModel, TARGET extends ProductData> extends AbstractProductPopulator<SOURCE, TARGET> {

	private CommercePriceService commercePriceService;
	private SolGroupPriceDataFactory solGroupPriceDataFactory;
		
	@Override
	public void populate(SOURCE productModel, TARGET productData) throws ConversionException {
		final PriceDataType priceType;
		final PriceInformation info;
		if (CollectionUtils.isEmpty(productModel.getVariants()))
		{
			priceType = PriceDataType.BUY;
			info = getCommercePriceService().getWebPriceForProduct(productModel);
		}
		else
		{
			priceType = PriceDataType.FROM;
			info = getCommercePriceService().getFromPriceForProduct(productModel);
		}

		if (info != null)
		{
			
			PriceRow priceRow =  (PriceRow) info.getQualifierValue("pricerow");
			String unitMeasureCode = null;
			if(priceRow!=null && priceRow.getUnit()!=null) {
				Unit unit = priceRow.getUnit();
				unitMeasureCode = unit.getCode();
			}
			
			
			final PriceData priceData = getSolGroupPriceDataFactory().create(priceType, BigDecimal.valueOf(info.getPriceValue().getValue()),
					info.getPriceValue().getCurrencyIso(), unitMeasureCode);
			
			
			
			
			
			productData.setPrice(priceData);
		}
		else
		{
			productData.setPurchasable(Boolean.FALSE);
		}
	}

	protected CommercePriceService getCommercePriceService() {
		return commercePriceService;
	}

	@Required
	public void setCommercePriceService(CommercePriceService commercePriceService) {
		this.commercePriceService = commercePriceService;
	}

	protected SolGroupPriceDataFactory getSolGroupPriceDataFactory() {
		return solGroupPriceDataFactory;
	}

	@Required
	public void setSolGroupPriceDataFactory(SolGroupPriceDataFactory solGroupPriceDataFactory) {
		this.solGroupPriceDataFactory = solGroupPriceDataFactory;
	}

	
	
}
