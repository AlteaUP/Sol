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
package com.solgroup.core.search.solrfacetsearch.provider.impl;

import de.hybris.platform.commerceservices.search.solrfacetsearch.provider.CategorySource;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractPropertyFieldValueProvider;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Category code value provider. Value provider that generates field values for category codes. This implementation uses
 * a {@link CategorySource} to provide the list of categories.
 */
public class SolGroupVariantCodesValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider
{

	private FieldNameProvider fieldNameProvider;




	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
			final Object model) throws FieldValueProviderException
	{

		if (model instanceof ProductModel)
		{
			final ProductModel product = (ProductModel) model;
			final Collection<VariantProductModel> variants = product.getVariants();

			if (CollectionUtils.isNotEmpty(variants))
			{
				final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();
				for (final VariantProductModel variant : variants)
				{
					fieldValues.addAll(createFieldValue(variant, null, indexedProperty));
				}
				return fieldValues;
			}
		}
		return Collections.emptyList();

	}

	protected List<FieldValue> createFieldValue(final VariantProductModel variant, final LanguageModel language,
			final IndexedProperty indexedProperty)
	{
		final List<FieldValue> fieldValues = new ArrayList<FieldValue>();
		final Object value = getPropertyValue(variant);
		final Collection<String> fieldNames = getFieldNameProvider().getFieldNames(indexedProperty, null);
		for (final String fieldName : fieldNames)
		{
			fieldValues.add(new FieldValue(fieldName, value));
		}
		return fieldValues;
	}



	protected Object getPropertyValue(final ItemModel model)
	{
		return modelService.getAttributeValue(model, VariantProductModel.ERPCODE);
	}


	//////////////////////////////////////////////////////////
	/////////// SETTER and GETTER ////////////////////////////
	//////////////////////////////////////////////////////////

	protected FieldNameProvider getFieldNameProvider()
	{
		return fieldNameProvider;
	}

	@Required
	public void setFieldNameProvider(final FieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}






}
