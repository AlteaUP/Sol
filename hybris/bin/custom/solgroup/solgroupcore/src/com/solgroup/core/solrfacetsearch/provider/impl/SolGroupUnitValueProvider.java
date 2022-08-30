package com.solgroup.core.solrfacetsearch.provider.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractPropertyFieldValueProvider;

public class SolGroupUnitValueProvider  extends AbstractPropertyFieldValueProvider implements FieldValueProvider{
	protected static final Logger LOG = Logger.getLogger(SolGroupProductPriceListValueProvider.class.getName());

    private FieldNameProvider fieldNameProvider;

    @Override
    public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
            final Object model) throws FieldValueProviderException {
        final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();
        try {
            if (!(model instanceof ProductModel)) {
                throw new FieldValueProviderException("Cannot get Unit ");
            }

            
            ProductModel product = (ProductModel) model;
            
            Collection<PriceRowModel> priceRows = product.getEurope1Prices();

            for (PriceRowModel priceRowModel : priceRows) {
            	UnitModel unit = priceRowModel.getUnit();
            	if(unit != null){
                    String value = unit.getCode();
            		String fieldName = fieldNameProvider.getFieldName(indexedProperty, null,
    	                    FieldNameProvider.FieldType.INDEX);
                    // FIX31012018: controllo se è un listino custom con il null sulla customerPriceList
                    if (priceRowModel.getUg() != null) {
	            		String desc = "unit_" + priceRowModel.getUg().getCode();
			            if (fieldName != null && !fieldName.isEmpty()) {
                            fieldName = fieldName.replaceAll("unit_string", desc + "_string");
			            }
	            	}
    	            fieldValues.add(new FieldValue(fieldName, value));
                }
            }

        } catch (final Exception e) {
            LOG.error("Failed to generate sellInPrice", e);
            throw new FieldValueProviderException("Cannot evaluate " + indexedProperty.getName() + " using "
                    + this.getClass().getName(), e);
        }
        return fieldValues;
    }

    public FieldNameProvider getFieldNameProvider() {
        return fieldNameProvider;
    }

    @Required
    public void setFieldNameProvider(final FieldNameProvider fieldNameProvider) {
        this.fieldNameProvider = fieldNameProvider;
    }
}
