package com.solgroup.core.search.solrfacetsearch.provider.impl;

import java.util.List;

import com.google.common.collect.Lists;

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.config.exceptions.PropertyOutOfRangeException;
import de.hybris.platform.solrfacetsearch.provider.impl.DefaultRangeNameProvider;

public class DefaultSolGroupRangeNameProvider extends DefaultRangeNameProvider {

	@Override
	public List<String> getRangeNameList(IndexedProperty property, Object value, String qualifier)
			throws FieldValueProviderException {
		
		try 
		{
			return super.getRangeNameList(property,value,qualifier);
		} 
		catch(PropertyOutOfRangeException ofr_exc) 
		{
			return Lists.newArrayList();
		}
		catch(Exception exc) {
			throw exc;
		}
	}
	
}
