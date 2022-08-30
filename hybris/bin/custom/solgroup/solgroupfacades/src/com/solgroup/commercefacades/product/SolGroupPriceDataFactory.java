package com.solgroup.commercefacades.product;

import java.math.BigDecimal;

import de.hybris.platform.commercefacades.product.PriceDataFactory;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;


public interface SolGroupPriceDataFactory extends PriceDataFactory {
	
	PriceData create(final PriceDataType priceType, final BigDecimal value, final String currencyIso, String unitMeasureCode);

}
