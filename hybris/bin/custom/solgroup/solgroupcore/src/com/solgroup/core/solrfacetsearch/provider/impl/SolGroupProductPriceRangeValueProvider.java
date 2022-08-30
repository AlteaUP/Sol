package com.solgroup.core.solrfacetsearch.provider.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Maps;
import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.solrfacetsearch.provider.entity.SolGroupSolrPriceRange;

import de.hybris.platform.commerceservices.util.AbstractComparator;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractPropertyFieldValueProvider;
import de.hybris.platform.variants.model.VariantProductModel;
import jersey.repackaged.com.google.common.collect.Sets;

/**
 * Value Provider for price range of multidimensional products.
 */
public class SolGroupProductPriceRangeValueProvider extends AbstractPropertyFieldValueProvider
        implements FieldValueProvider {

    private FieldNameProvider fieldNameProvider;

    @Override
    public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
            final Object model) throws FieldValueProviderException {
        final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();
        // make sure you have the baseProduct because variantProducts won't have other variants
        ProductModel product = (ProductModel) model;
        final ProductModel baseProduct = getBaseProduct(product);

        final Collection<VariantProductModel> variants = baseProduct.getVariants();
        if (CollectionUtils.isNotEmpty(variants)) {
            
        	Map<String, Map<String,PriceRowModel>> pricesMap = Maps.newHashMap();
        	Set<String> priceListCodes = Sets.newHashSet();
        	

            // STEP 1 : get all price list codes
            for (final VariantProductModel variant : variants) {
                Collection<PriceRowModel> prices = variant.getEurope1Prices();
                for (PriceRowModel price : prices) {
                	
                	if(!isInPresent(price.getStartTime(), price.getEndTime())) {
                		continue;
                	}
                	
                	String priceListCode = "";
                	if(price.getUg()==null) {
                		priceListCode = price.getCurrency().getIsocode().toLowerCase();
                	}
                	else {
                		priceListCode = price.getUg().getCode();
                	}
                	
                	if(StringUtils.isNotEmpty(priceListCode)) {
                		priceListCodes.add(priceListCode);
                		pricesMap.put(priceListCode, new HashMap<String,PriceRowModel>());
                	}

                }// end for to iterate over all priceRow

            } // end for to iterate over all variants
            
            
            // STEP 2 : get all default prices
            for (final VariantProductModel variant : variants) {
                Collection<PriceRowModel> prices = variant.getEurope1Prices();
                for (PriceRowModel price : prices) {
                	
                	if(!isInPresent(price.getStartTime(), price.getEndTime())) {
                		continue;
                	}

                	if(price.getUg()!=null) {
                		continue;
                	}
                	
                	
                	pricesMap.get(price.getCurrency().getIsocode().toLowerCase()).put(variant.getCode(), price);
                	
                }// end for to iterate over all priceRow

            } // end for to iterate over all variants


            // STEP 3 : get all custom prices
            for (final VariantProductModel variant : variants) {
                Collection<PriceRowModel> prices = variant.getEurope1Prices();
                for (PriceRowModel price : prices) {
                	
                	if(!isInPresent(price.getStartTime(), price.getEndTime())) {
                		continue;
                	}

                	if(price.getUg()==null) {
                		continue;
                	}

                	pricesMap.get(price.getUg().getCode()).put(variant.getCode(), price);
                	
                }// end for to iterate over all priceRow

            } // end for to iterate over all variants
            
            

            if (!pricesMap.isEmpty()) {
                getPriceRangeString(pricesMap, fieldValues, indexedProperty);
            }
        }

        return fieldValues;
    }

    protected void getPriceRangeString(final Map<String, Map<String,PriceRowModel>> allPricesInfos,Collection<FieldValue> fieldValues, final IndexedProperty indexedProperty) {
        Set<String> priceListCodes = allPricesInfos.keySet();
        for (String priceListCode : priceListCodes) {
            // ArrayList<PriceRowModel> priceRows = allPricesInfos.get(priceListCode);
        	ArrayList<PriceRowModel> priceRows = new ArrayList<PriceRowModel>();
        	priceRows.addAll(allPricesInfos.get(priceListCode).values());
        	
            String fieldName = fieldNameProvider.getFieldName(indexedProperty, null, FieldNameProvider.FieldType.INDEX);
            if (priceListCode != null) {

                String desc = "priceRange_" + priceListCode;

                if (fieldName != null && !fieldName.isEmpty()) {
                    fieldName = fieldName.replaceAll("priceRange", desc);
                }
            }

            Collections.sort(priceRows, PriceRangeComparator.INSTANCE);
            PriceRowModel lowest = null;
            PriceRowModel highest = null;
            if (priceRows.size() > 1) {
                lowest = priceRows.get(0);
                highest = priceRows.get(priceRows.size() - 1);
            } else {
                lowest = priceRows.get(0);
                highest = priceRows.get(0);
            }

            // FIX31012018: inserita unità di misura sull'indicizzazione solr e cambiato il formato
            String value = SolGroupSolrPriceRange.buildSolrPropertyFromPriceRows(lowest.getPrice().doubleValue(),
                    lowest != null ? lowest.getUnit().getCode() : SolgroupCoreConstants.DEFAULT_UNIT_MEASURE,
                    lowest.getCurrency().getIsocode(), highest.getPrice().doubleValue(),
                    highest != null ? highest.getUnit().getCode() : SolgroupCoreConstants.DEFAULT_UNIT_MEASURE,
                    highest.getCurrency().getIsocode());

            fieldValues.add(new FieldValue(fieldName, value));
        }

    }

    public static class PriceRangeComparator extends AbstractComparator<PriceRowModel> {
        public static final PriceRangeComparator INSTANCE = new PriceRangeComparator();

        @Override
        protected int compareInstances(final PriceRowModel price1, final PriceRowModel price2) {
            if (price1 == null || price1.getPrice() == null) {
                return BEFORE;
            }
            if (price2 == null || price2.getPrice() == null) {
                return AFTER;
            }

            return Double.compare(price1.getPrice().doubleValue(), price2.getPrice().doubleValue());
        }
    }

    public ProductModel getBaseProduct(final ProductModel model) {
        final ProductModel baseProduct;
        if (model instanceof VariantProductModel) {
            final VariantProductModel variant = (VariantProductModel) model;
            baseProduct = variant.getBaseProduct();
        } else {
            baseProduct = model;
        }
        return baseProduct;
    }
    
    private boolean isInPresent(Date startPriceDate, Date endPriceDate) {
    	if(startPriceDate==null || endPriceDate==null) {
    		return true;
    	}
    	Date now = new Date();
    	int compareStartDate = startPriceDate.compareTo(now);
    	int compareEndDate = endPriceDate.compareTo(now);
    	return compareStartDate<=0 && compareEndDate >=0;
    }
    

    public FieldNameProvider getFieldNameProvider() {
        return fieldNameProvider;
    }

    @Required
    public void setFieldNameProvider(final FieldNameProvider fieldNameProvider) {
        this.fieldNameProvider = fieldNameProvider;
    }

}
