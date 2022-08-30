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

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.commerceservices.search.solrfacetsearch.provider.CategorySource;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractPropertyFieldValueProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.model.AbstractNavigationCategoryModel;

import jersey.repackaged.com.google.common.collect.Sets;


/**
 * Category code value provider. Value provider that generates field values for category codes. This implementation uses
 * a {@link CategorySource} to provide the list of categories.
 */
public class SolGroupNavCategoryValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider
{

	private String navigationCategoryTypeCode;
	private Integer navigationCategoryLevel;
	private String propertyName;

	private FieldNameProvider fieldNameProvider;
	private CommonI18NService commonI18NService;




	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
			final Object model) throws FieldValueProviderException
	{

		if (model instanceof ProductModel)
		{
			final ProductModel product = (ProductModel) model;
			final Set<CategoryModel> categoriesToIndex = findCategoriesToIndex(product);
			if (CollectionUtils.isNotEmpty(categoriesToIndex))
			{
				final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();

				if (indexedProperty.isLocalized())
				{
					final Collection<LanguageModel> languages = indexConfig.getLanguages();
					for (final LanguageModel language : languages)
					{
						for (final CategoryModel category : categoriesToIndex)
						{
							fieldValues.addAll(createFieldValue(category, language, indexedProperty));
						}
					}
				}
				else
				{
					for (final CategoryModel category : categoriesToIndex)
					{
						fieldValues.addAll(createFieldValue(category, null, indexedProperty));
					}
				}
				return fieldValues;
			}
			else
			{
				return Collections.emptyList();
			}

		}
		return Collections.emptyList();
	}

	protected List<FieldValue> createFieldValue(final CategoryModel category, final LanguageModel language,
			final IndexedProperty indexedProperty)
	{
		final List<FieldValue> fieldValues = new ArrayList<FieldValue>();

		if (language != null)
		{
			final Locale locale = i18nService.getCurrentLocale();
			Object value = null;
			try
			{
				i18nService.setCurrentLocale(getCommonI18NService().getLocaleForLanguage(language));
				value = getPropertyValue(category);
			}
			finally
			{
				i18nService.setCurrentLocale(locale);
			}

			final Collection<String> fieldNames = getFieldNameProvider().getFieldNames(indexedProperty, language.getIsocode());
			for (final String fieldName : fieldNames)
			{
				fieldValues.add(new FieldValue(fieldName, value));
			}
		}
		else
		{
			final Object value = getPropertyValue(category);
			final Collection<String> fieldNames = getFieldNameProvider().getFieldNames(indexedProperty, null);
			for (final String fieldName : fieldNames)
			{
				fieldValues.add(new FieldValue(fieldName, value));
			}
		}

		return fieldValues;
	}


	/**
	 * Method to find product categories to index
	 *
	 * @param product
	 * @return
	 */
	private Set<CategoryModel> findCategoriesToIndex(final ProductModel product)
	{
		final Set<CategoryModel> result = Sets.newHashSet();

		if (CollectionUtils.isNotEmpty(product.getSupercategories()))
		{

			analizeSuperCategories(product.getSupercategories(), result);
		}
		return result;
	}


	/**
	 * Method to check product superCategories
	 *
	 * @param categories
	 * @param categoriesToIndex
	 */
	private void analizeSuperCategories(final Collection<CategoryModel> categories, final Set<CategoryModel> categoriesToIndex)
	{
		if (CollectionUtils.isNotEmpty(categories))
		{
			for (final CategoryModel category : categories)
			{
				// Current category is a navigationCategory (abstractNavCategory) and typeCode==configuredTypeCode --> this category can be used
				if (category instanceof AbstractNavigationCategoryModel
						&& category.getItemtype().equals(getNavigationCategoryTypeCode()))
				{
					//Check navCategory level : current navCategory.level>configuredNavLevel --> check navCategory.superCategory
					if (((AbstractNavigationCategoryModel) category).getLevel().intValue() > getNavigationCategoryLevel().intValue())
					{
						analizeSuperCategories(category.getSupercategories(), categoriesToIndex);
					}
					//Check navCategory level : current navCategory.level==configuredNavLevel --> add navCategory to categories to index
					else if (((AbstractNavigationCategoryModel) category).getLevel().intValue() == getNavigationCategoryLevel()
							.intValue())
					{
						categoriesToIndex.add(category);
					}

				}

			}

		}
	}

	protected Object getPropertyValue(final ItemModel model)
	{
		return modelService.getAttributeValue(model, getPropertyName());
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

	protected CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	protected String getNavigationCategoryTypeCode()
	{
		return navigationCategoryTypeCode;
	}

	@Required
	public void setNavigationCategoryTypeCode(final String navigationCategoryTypeCode)
	{
		this.navigationCategoryTypeCode = navigationCategoryTypeCode;
	}

	protected Integer getNavigationCategoryLevel()
	{
		return navigationCategoryLevel;
	}

	@Required
	public void setNavigationCategoryLevel(final Integer navigationCategoryLevel)
	{
		this.navigationCategoryLevel = navigationCategoryLevel;
	}

	protected String getPropertyName()
	{
		return propertyName;
	}

	@Required
	public void setPropertyName(final String propertyName)
	{
		this.propertyName = propertyName;
	}





}
