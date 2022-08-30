package com.solgroup.facades.search.converters.populator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.commercefacades.product.SolGroupPriceDataFactory;
import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.solrfacetsearch.provider.entity.SolGroupSolrPriceRange;

import de.hybris.platform.acceleratorfacades.order.data.PriceRangeData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.search.converters.populator.SearchResultProductPopulator;
import de.hybris.platform.commerceservices.search.resultdata.SearchResultValueData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.session.SessionService;

public class SolGroupSearchResultProductPopulator extends SearchResultProductPopulator
        implements Populator<SearchResultValueData, ProductData> {

    @Resource(name = "sessionService")
    SessionService sessionService;

    @Resource(name = "commonI18NService")
    private CommonI18NService commonI18NService;

    private SolGroupPriceDataFactory priceDataFactory;

    //public static final String DEFAULT_PRICE_LIST = "B2B_DEFAULT_PRICE_GROUP";

    protected static final Logger LOG = Logger.getLogger(SolGroupSearchResultProductPopulator.class);
    
    private String priceValue_propertyName_pattern = "priceValue_%s_string";
    private String priceRange_propertyName_pattern = "priceRange_%s_string";


    @Override
    public void populate(final SearchResultValueData source, final ProductData target) {
        super.populate(source, target);
        populatePrices(source, target);
        populateStockable(source, target);
        populateUnit(source, target);
        target.setErpCode(this.<String> getValue(source, "erpCode"));
    }

    @Override
    protected void populatePrices(final SearchResultValueData source, final ProductData target) {
        // Pull the volume prices flag
        final Boolean volumePrices = this.<Boolean> getValue(source, "volumePrices");
        target.setVolumePricesFlag(volumePrices == null ? Boolean.FALSE : volumePrices);

        // Get customer price list
        String priceListCode = sessionService.getAttribute(SolgroupCoreConstants.SESSION_PARAM_PRICE_LIST_CODE);
//        // Get session currency
//        CurrencyModel sessionCurrency = commonI18NService.getCurrentCurrency();
//        String currency = sessionCurrency.getIsocode().toLowerCase();
        
        
        
        Boolean isCustomPrice = false;


        String priceValue_custom_propertyName = "";
        String priceRange_custom_propertyName = "";
        
        String priceValue_default_propertyName = "priceValue";
        String priceRange_default_propertyName = "priceRange";
        
        
        if(StringUtils.isNotEmpty(priceListCode)) {
        	priceValue_custom_propertyName = String.format(priceValue_propertyName_pattern, "_"+priceListCode);
        	priceRange_custom_propertyName = String.format(priceRange_propertyName_pattern, priceListCode);
        }
        
        // Get multiDimensional attribute
        Boolean multidimensional = this.<Boolean> getValue(source, "multidimensional");
        
        // MULTIDIMENSIONAL PRODUCT
        if (BooleanUtils.isTrue(multidimensional)) {
            String priceRange_str = this.<String> getValue(source, priceRange_custom_propertyName);
            if (StringUtils.isEmpty(priceRange_str)) {
                priceRange_str = this.<String> getValue(source, priceRange_default_propertyName);
            }
            else {
            	isCustomPrice = Boolean.TRUE;
            }

            if (priceRange_str != null && !priceRange_str.isEmpty()) {
                List<String[]> result = SolGroupSolrPriceRange.splitSolrPropertyFromPriceRows(priceRange_str);

                if (CollectionUtils.isNotEmpty(result)) {
                    PriceData minPrice = null;
                    PriceData maxPrice = null;
                    if (result.get(0) != null && result.get(0).length == 3 && result.get(1) != null
                            && result.get(1).length == 3) {
                        minPrice = priceDataFactory.create(PriceDataType.BUY,
                                BigDecimal.valueOf(new Double(result.get(0)[0] != null ? result.get(0)[0] : "0")),
                                result.get(0)[2] != null ? result.get(0)[2] : "",
                                result.get(0)[1] != null ? result.get(0)[1] : "");
                        minPrice.setCustomPrice(isCustomPrice);
                        maxPrice = priceDataFactory.create(PriceDataType.BUY,
                                BigDecimal.valueOf(new Double(result.get(1)[0] != null ? result.get(1)[0] : "0")),
                                result.get(1)[2] != null ? result.get(1)[2] : "",
                                result.get(1)[1] != null ? result.get(1)[1] : "");
                    }

                    final PriceRangeData priceRangeData = new PriceRangeData();
                    priceRangeData.setMaxPrice(maxPrice);
                    priceRangeData.setMinPrice(minPrice);
                    target.setPriceRange(priceRangeData);

                }
            }

        } // end if to manage MULTIDIMENSIONAL product
        
        // NO MULTIDIMENSIONAL PRODUCT
        else {
            
            
            String priceValue_str = this.<String> getValue(source, priceValue_custom_propertyName);
            if (StringUtils.isEmpty(priceValue_str)) {
                priceValue_str = this.<String> getValue(source, priceValue_default_propertyName);
            }
            else {
            	isCustomPrice = Boolean.TRUE;
            }
            
            if (priceValue_str != null && !priceValue_str.isEmpty()) {

                Double priceValue = new Double(0d);
                String unitMeasure = "";
                PriceData priceData = null;

                if (priceValue_str.indexOf(":") > 0) {
                    priceValue = Double.valueOf(priceValue_str.split(":")[0]);
                    unitMeasure = priceValue_str.split(":")[1];
                    priceData = getPriceDataFactory().create(PriceDataType.BUY,
                            BigDecimal.valueOf(priceValue.doubleValue()),
                            getCommonI18NService().getCurrentCurrency().getIsocode(), unitMeasure);
                } else {
                    priceValue = Double.valueOf(priceValue_str);
                    priceData = getPriceDataFactory().create(PriceDataType.BUY,
                            BigDecimal.valueOf(priceValue.doubleValue()), getCommonI18NService().getCurrentCurrency());
                }

                priceData.setCustomPrice(isCustomPrice);

                target.setPrice(priceData);
            }

        }




    }

    protected void populateStockable(final SearchResultValueData source, final ProductData target) {
        Boolean stockable = this.<Boolean> getValue(source, "stockable");
        if (stockable == null) {
            target.setStockable(false);
        } else {
            target.setStockable(stockable.booleanValue());
        }

    }

    protected void populateUnit(final SearchResultValueData source, final ProductData target) {
        String priceListCode = sessionService.getAttribute("priceListCode");

        if (priceListCode == null) {
            //priceListCode = DEFAULT_PRICE_LIST;
        	priceListCode = "";
        }

        String unit = this.<String> getValue(source, "unit_" + priceListCode + "_string");

        if (unit == null) {
            unit = this.<String> getValue(source, "unit_string");
        }
        Boolean multidimensional = this.<Boolean> getValue(source, "multidimensional");

        target.setUnit(unit);

        if (multidimensional == null) {
            target.setMultidimensional(false);
        } else {
            target.setMultidimensional(multidimensional.booleanValue());
        }

    }

	@Override
    protected SolGroupPriceDataFactory getPriceDataFactory() {
		return priceDataFactory;
	}

	@Required
	public void setPriceDataFactory(SolGroupPriceDataFactory priceDataFactory) {
		this.priceDataFactory = priceDataFactory;
	}
    
    
}
