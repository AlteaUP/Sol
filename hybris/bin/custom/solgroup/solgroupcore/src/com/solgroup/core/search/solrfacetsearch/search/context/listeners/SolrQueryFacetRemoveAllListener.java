package com.solgroup.core.search.solrfacetsearch.search.context.listeners;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.search.FacetField;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.FacetValueField;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchListener;
import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Sets;

public class SolrQueryFacetRemoveAllListener implements FacetSearchListener {

	private static final Logger LOG = Logger.getLogger(SolrQueryFacetRemoveAllListener.class);
	
	private CMSSiteService cmsSiteService;
	
	
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
		
		
		List<FacetValueField> facetValueFields = context.getSearchQuery().getFacetValues();
		if(CollectionUtils.isNotEmpty(facetValueFields)) {
			for(FacetValueField facetValueField : facetValueFields) {
				if(facetValueField.getField().equals("allCategories")) {
					Set<String> values = facetValueField.getValues();
					if(values.contains(getCmsSiteService().getCurrentSite().getUid() + "_1")) {
						context.getSearchQuery().getFacets().clear();
						break;
					}
				}
			}
		}
		
	}

	protected CMSSiteService getCmsSiteService() {
		return cmsSiteService;
	}

	@Required
	public void setCmsSiteService(CMSSiteService cmsSiteService) {
		this.cmsSiteService = cmsSiteService;
	}
	
	
	
	

}
