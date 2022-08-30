package com.solgroup.core.search.solrfacetsearch.search.context.listeners;

import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.service.categories.SolGroupCategoryService;

import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.SearchQuery.Operator;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchListener;
import jersey.repackaged.com.google.common.collect.Sets;

public class SolrQueryVisibilityCategoryListener implements FacetSearchListener {

	private SessionService sessionService;
	private CMSSiteService cmsSiteService;
	private SolGroupCategoryService solGroupCategoryService;
	private ConfigurationService configurationService;
	
	private static final Logger LOG = Logger.getLogger(SolrQueryVisibilityCategoryListener.class);
	
	
	
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
		
		
		Set<String> visibilityCategories = Sets.newHashSet();
		// Set default visibility category
		visibilityCategories.add(getSolGroupCategoryService().getHybrisCategoryCode(getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CATEGORY_VISIBILITY_ALL), getCmsSiteService().getCurrentSite()));
		
		// Get visibility categories related to current b2bUnit
		if(getSessionService().getAttribute(SolgroupCoreConstants.SESSION_PARAM_VSIBILITY_CATEGORIES)!=null) {
			visibilityCategories.addAll(getSessionService().getAttribute(SolgroupCoreConstants.SESSION_PARAM_VSIBILITY_CATEGORIES));
		}
		
		
		
		// Create new filter query for visibility categories
		context.getSearchQuery().addFilterQuery("allCategories_string_mv", Operator.OR, visibilityCategories);
				
	}


	protected SessionService getSessionService() {
		return sessionService;
	}

	@Required
	public void setSessionService(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	protected CMSSiteService getCmsSiteService() {
		return cmsSiteService;
	}

	@Required
	public void setCmsSiteService(CMSSiteService cmsSiteService) {
		this.cmsSiteService = cmsSiteService;
	}

	protected SolGroupCategoryService getSolGroupCategoryService() {
		return solGroupCategoryService;
	}
	
	@Required
	public void setSolGroupCategoryService(SolGroupCategoryService solGroupCategoryService) {
		this.solGroupCategoryService = solGroupCategoryService;
	}

	protected ConfigurationService getConfigurationService() {
		return configurationService;
	}

	@Required
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
	
	
	


	

	
	

}
