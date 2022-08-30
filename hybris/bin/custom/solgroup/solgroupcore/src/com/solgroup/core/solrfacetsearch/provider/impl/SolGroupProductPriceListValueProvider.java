package com.solgroup.core.solrfacetsearch.provider.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.constants.SolgroupCoreConstants;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractPropertyFieldValueProvider;

public class SolGroupProductPriceListValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider{

	protected static final Logger LOG = Logger.getLogger(SolGroupProductPriceListValueProvider.class.getName());

    private FieldNameProvider fieldNameProvider;

    @Override
    public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
            final Object model) throws FieldValueProviderException {
        final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();
        try {
            if (!(model instanceof ProductModel)) {
                throw new FieldValueProviderException("Cannot sellIn in price ");
            }

            
            ProductModel product = (ProductModel) model;

            Collection<PriceRowModel> priceRows = product.getEurope1Prices();

            for (PriceRowModel priceRowModel : priceRows) {
            	
            	if(!isInPresent(priceRowModel.getStartTime(), priceRowModel.getEndTime())) {
            		continue;
            	}
            	

            	if(priceRowModel.getUg() != null){
	            
	            	String desc = "priceValue__" + priceRowModel.getUg().getCode();
		
		            // LOG.warn("Indexing: " + priceRowModel.getCustomerPriceList().getType() + " " + desc);
		
		            String fieldName = fieldNameProvider.getFieldName(indexedProperty, null,
		                    FieldNameProvider.FieldType.INDEX);
		
		            if (fieldName != null && !fieldName.isEmpty()) {
		                fieldName = fieldName.replaceAll("priceValue", desc);
		            }
		            
//		            if (fieldName != null && !fieldName.isEmpty()) {
//		                fieldName = fieldName.replaceAll("price_string", desc + "_string");
//		            }
		
	
		            String value_str = "" + priceRowModel.getPrice().doubleValue();
		            if(priceRowModel.getUnit()!=null && !priceRowModel.getUnit().getCode().equals(SolgroupCoreConstants.DEFAULT_UNIT_MEASURE)) {
	                	value_str = value_str + ":" + priceRowModel.getUnit().getCode();
	                }
	                fieldValues.add(new FieldValue(fieldName, value_str));
            	}
            	else{
            		String desc = "priceValue_" + priceRowModel.getCurrency().getIsocode().toLowerCase();
            		
		            // LOG.warn("Indexing: " + priceRowModel.getCustomerPriceList().getType() + " " + desc);
		
		            String fieldName = fieldNameProvider.getFieldName(indexedProperty, null,
		                    FieldNameProvider.FieldType.INDEX);
		
		            if (fieldName != null && !fieldName.isEmpty()) {
		                fieldName = fieldName.replaceAll("priceValue", desc);
		            }
		            
//		            if (fieldName != null && !fieldName.isEmpty()) {
//		                fieldName = fieldName.replaceAll("price_string", "price_" + priceRowModel.getCurrency().getIsocode() + "_string");
//		            }
		
		            String value_str = "" + priceRowModel.getPrice().doubleValue();
		            if(priceRowModel.getUnit()!=null && !priceRowModel.getUnit().getCode().equals(SolgroupCoreConstants.DEFAULT_UNIT_MEASURE)) {
	                	value_str = value_str + ":" + priceRowModel.getUnit().getCode();
	                }
	                fieldValues.add(new FieldValue(fieldName, value_str));
            	}

            }

        } catch (final Exception e) {
            LOG.error("Failed to generate sellInPrice", e);
            throw new FieldValueProviderException("Cannot evaluate " + indexedProperty.getName() + " using "
                    + this.getClass().getName(), e);
        }
        return fieldValues;
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
