package com.solgroup.core.search.solrfacetsearch.search.context.listeners;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.search.FacetField;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.FacetValueField;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchListener;
import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Sets;

public class SolrQueryFacetRemoveListener implements FacetSearchListener {

	private static final Logger LOG = Logger.getLogger(SolrQueryFacetRemoveListener.class);
	
	
	@Override
	public void afterSearch(FacetSearchContext context) throws FacetSearchException {
	}

	@Override
	public void afterSearchError(FacetSearchContext context) throws FacetSearchException {
	}

	@Override
	public void beforeSearch(FacetSearchContext context) throws FacetSearchException {
		
		if(context==null || context.getIndexedType()==null) {
			return;
		}
		
		
		
		
		Map<String,IndexedProperty> indexedPropertyMap = context.getIndexedType().getIndexedProperties();
		
		if(indexedPropertyMap==null || indexedPropertyMap.isEmpty()) {
			return;
		}
		
		Set<String> visibleFacets = Sets.newHashSet();
		
		for(IndexedProperty indexedProperty : indexedPropertyMap.values()) {
			if(indexedProperty!=null && indexedProperty.isVisible()) {
				visibleFacets.add(indexedProperty.getName());
			}
		}
		
		for(FacetValueField facetValueField : context.getSearchQuery().getFacetValues()) {
			visibleFacets.add(facetValueField.getField());
		}
		
		List<FacetField> facetsToRemove = Lists.newArrayList();
		if(CollectionUtils.isNotEmpty(context.getSearchQuery().getFacets())) {
			for(FacetField facet : context.getSearchQuery().getFacets()) {
				if(!visibleFacets.contains(facet.getField())) {
					facetsToRemove.add(facet);
				}
			}
			context.getSearchQuery().getFacets().removeAll(facetsToRemove);
		}
		
	}

}
