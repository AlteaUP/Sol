package com.solgroup.commercefacades.product.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.util.Assert;

import com.solgroup.commercefacades.product.SolGroupPriceDataFactory;
import com.solgroup.core.constants.SolgroupCoreConstants;

import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.product.impl.DefaultPriceDataFactory;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;

public class DefaultSolGroupPriceDataFactory extends DefaultPriceDataFactory implements SolGroupPriceDataFactory {
	
	@Override
	public PriceData create(final PriceDataType priceType, final BigDecimal value, final String currencyIso, String unitMeasureCode)
	{
		Assert.notNull(priceType, "Parameter priceType cannot be null.");
		Assert.notNull(value, "Parameter value cannot be null.");
		Assert.notNull(currencyIso, "Parameter currencyIso cannot be null.");
		
		final PriceData priceData = createPriceData();

		priceData.setPriceType(priceType);
		priceData.setValue(value);
		priceData.setCurrencyIso(currencyIso);
		priceData.setUnitMeasure(unitMeasureCode);
		priceData.setFormattedValue(formatPrice(value, currencyIso, unitMeasureCode));

		return priceData;
	}

	protected String formatPrice(final BigDecimal value, final String currencyIso, String unitMeasureCode)
	{
		final LanguageModel currentLanguage = getCommonI18NService().getCurrentLanguage();
		Locale locale = getCommerceCommonI18NService().getLocaleForLanguage(currentLanguage);
		if (locale == null)
		{
			// Fallback to session locale
			locale = getI18NService().getCurrentLocale();
		}

		final CurrencyModel currency = getCommonI18NService().getCurrency(currencyIso);
		final NumberFormat currencyFormat = createCurrencyFormat(locale, currency);
		String priceWithCurrency = currencyFormat.format(value);
		
		if(unitMeasureCode!=null && !unitMeasureCode.isEmpty() && !unitMeasureCode.equals(SolgroupCoreConstants.DEFAULT_UNIT_MEASURE)) {
			return priceWithCurrency.replaceAll(currency.getSymbol(), currency.getSymbol()+ SolgroupCoreConstants.UNIT_MEASURE_SEPARATOR + unitMeasureCode);
			//return priceWithCurrency + SolgroupCoreConstants.UNIT_MEASURE_SEPARATOR + unitMeasureCode;
		}
		else {
			return priceWithCurrency;
		}
		 
	}
	
	@Override
	protected DecimalFormat adjustDigits(final DecimalFormat format, final CurrencyModel currencyModel)
	{
	
		final int tempDigits = currencyModel.getDigits() == null ? 0 : currencyModel.getDigits().intValue();
		final int digits = Math.max(0, tempDigits);

		format.setMaximumFractionDigits(digits);
		format.setMinimumFractionDigits(2);
		if (digits == 0)
		{
			format.setDecimalSeparatorAlwaysShown(false);
		}

		return format;
	}


}
