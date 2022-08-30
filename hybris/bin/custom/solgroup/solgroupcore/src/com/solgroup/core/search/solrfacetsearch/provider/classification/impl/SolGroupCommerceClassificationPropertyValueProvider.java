package com.solgroup.core.search.solrfacetsearch.provider.classification.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.hybris.platform.catalog.jalo.classification.ClassificationAttributeUnit;
import de.hybris.platform.catalog.jalo.classification.ClassificationAttributeValue;
import de.hybris.platform.catalog.jalo.classification.util.FeatureValue;
import de.hybris.platform.commerceservices.search.solrfacetsearch.provider.impl.CommerceClassificationPropertyValueProvider;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;

public class SolGroupCommerceClassificationPropertyValueProvider extends
CommerceClassificationPropertyValueProvider {

    @SuppressWarnings("all")
    @Override
    protected List<FieldValue> extractFieldValues(IndexedProperty indexedProperty, LanguageModel language,
            List<FeatureValue> list) throws FieldValueProviderException {

        ArrayList result = new ArrayList();
        Iterator arg5 = list.iterator();

        label44: while (arg5.hasNext()) {
            FeatureValue featureValue = (FeatureValue) arg5.next();
            Object value = featureValue.getValue();
            
            if (value instanceof ClassificationAttributeValue) {
                value = ((ClassificationAttributeValue) value).getName();
            }

            Object unit = featureValue.getUnit();

            if (unit instanceof ClassificationAttributeUnit) {
                if (unit != null) {
                    unit = ((ClassificationAttributeUnit) unit).getSymbol();
                    value = (String) value + StringUtils.SPACE + (String) unit;
                }
            }

            List rangeNameList = this.getRangeNameList(indexedProperty, value);

            Collection fieldNames = super.getFieldNameProvider().getFieldNames(indexedProperty,
                    language == null ? null : language.getIsocode());
            Iterator arg10 = fieldNames.iterator();

            while (true) {
                while (true) {
                    if (!arg10.hasNext()) {
                        continue label44;
                    }

                    String fieldName = (String) arg10.next();
                    if (rangeNameList.isEmpty()) {
                        result.add(new FieldValue(fieldName, value));
                    } else {
                        Iterator arg12 = rangeNameList.iterator();

                        while (arg12.hasNext()) {
                            String rangeName = (String) arg12.next();
                            result.add(new FieldValue(fieldName, rangeName == null ? value : rangeName));
                        }
                    }
                }
            }
        }

        return result;
    }

}
