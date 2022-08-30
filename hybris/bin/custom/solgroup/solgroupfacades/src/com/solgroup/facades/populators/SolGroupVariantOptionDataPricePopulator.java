/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.solgroup.facades.populators;

import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.product.data.VariantOptionData;
import de.hybris.platform.commerceservices.price.CommercePriceService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.europe1.jalo.PriceRow;
import de.hybris.platform.jalo.order.price.PriceInformation;
import de.hybris.platform.jalo.product.Unit;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.variants.model.VariantProductModel;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.commercefacades.product.SolGroupPriceDataFactory;


public class SolGroupVariantOptionDataPricePopulator<SOURCE extends VariantProductModel, TARGET extends VariantOptionData>
		implements Populator<SOURCE, TARGET>
{

	private SolGroupPriceDataFactory solGroupPriceDataFactory;
	private CommercePriceService commercePriceService;


	@Override
	public void populate(final VariantProductModel variantProductModel, final VariantOptionData variantOptionData)
			throws ConversionException
	{

		final PriceInformation priceInformation = getCommercePriceService().getWebPriceForProduct(variantProductModel);

		PriceData priceData;
		if (priceInformation != null && priceInformation.getPriceValue() != null)
		{

			String unitMeasureCode = null;
			//Get unit measure
			if( priceInformation.getQualifierValue("pricerow")!=null && priceInformation.getQualifierValue("pricerow") instanceof PriceRow) {
				PriceRow priceRow =  (PriceRow) priceInformation.getQualifierValue("pricerow");
				if(priceRow!=null && priceRow.getUnit()!=null) {
					Unit unit = priceRow.getUnit();
					unitMeasureCode = unit.getCode();
				}
			}
			
			priceData = getSolGroupPriceDataFactory().create(PriceDataType.FROM,
					BigDecimal.valueOf(priceInformation.getPriceValue().getValue()),
					priceInformation.getPriceValue().getCurrencyIso(),unitMeasureCode);
		}
		else
		{
			priceData = new PriceData();
			priceData.setValue(BigDecimal.ZERO);
			priceData.setFormattedValue("0");
		}
		


		variantOptionData.setPriceData(priceData);

	}

	protected CommercePriceService getCommercePriceService()
	{
		return commercePriceService;
	}

	@Required
	public void setCommercePriceService(final CommercePriceService commercePriceService)
	{
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