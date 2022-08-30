package com.solgroup.core.search.solrfacetsearch.search.context.listeners;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.constants.SolgroupCoreConstants;

import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.search.FacetField;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.FacetValueField;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchListener;
import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Sets;

public class SolrQueryPriceRangeListener implements FacetSearchListener {

	private static final Logger LOG = Logger.getLogger(SolrQueryPriceRangeListener.class);
	
	private SessionService sessionService;
	
	
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
		
		String priceListCode = sessionService.getAttribute(SolgroupCoreConstants.SESSION_PARAM_PRICE_LIST_CODE);
		if(StringUtils.isNotEmpty(priceListCode)) {
			context.getSearchQuery().getFields().add("priceRange_" + priceListCode + "_string");
			context.getSearchQuery().getFields().add("priceValue__" + priceListCode + "_string");
		}
		
	}

	protected SessionService getSessionService() {
		return sessionService;
	}

	@Required
	public void setSessionService(SessionService sessionService) {
		this.sessionService = sessionService;
	}
	
	
	

}
