package com.solgroup.core.search.solrfacetsearch;

import de.hybris.platform.core.Registry;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchResult;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;
import de.hybris.platform.solrfacetsearch.search.impl.DefaultFacetSearchStrategy;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;
import de.hybris.platform.solrfacetsearch.search.impl.SearchResultConverterData;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.IOUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SolGroupFacetSearchStrategy extends DefaultFacetSearchStrategy {

	private static final Logger LOG = Logger.getLogger(SolGroupFacetSearchStrategy.class);

	private UserService userService;

	@Value("#{'${exclude.product.usergroups_category}'.split(',')}")
	private List<String> categoryToExclude;

    @Override
    public SearchResult search(SearchQuery searchQuery, Map<String, String> searchHints) throws FacetSearchException {
        this.checkQuery(searchQuery);
        SolrClient solrClient = null;

        SearchResult result;
        try {
            FacetSearchConfig e = searchQuery.getFacetSearchConfig();
            IndexedType indexedType = searchQuery.getIndexedType();
            FacetSearchContext facetSearchContext = super.getFacetSearchContextFactory().createContext(e, indexedType, searchQuery);
            facetSearchContext.getSearchHints().putAll(searchHints);
            super.getFacetSearchContextFactory().initializeContext();
            SolrSearchProvider solrSearchProvider = super.getSolrSearchProviderFactory().getSearchProvider(e, indexedType);
            SolrIndexModel activeIndex = super.getSolrIndexService().getActiveIndex(e.getName(), indexedType.getIdentifier());
            Index index = solrSearchProvider.resolveIndex(e, indexedType, activeIndex.getQualifier());
            solrClient = solrSearchProvider.getClient(index);
            SearchQueryConverterData searchQueryConverterData = new SearchQueryConverterData();
            searchQueryConverterData.setFacetSearchContext(facetSearchContext);
            searchQueryConverterData.setSearchQuery(searchQuery);
            SolrQuery solrQuery = super.getFacetSearchQueryConverter().convert(searchQueryConverterData);
            if (LOG.isDebugEnabled()) {
                LOG.debug(solrQuery);
            }
			final ApplicationContext appCtx = Registry.getApplicationContext();

			addCategoryFilters(solrQuery);
			// Change solr client to do post request
            QueryResponse queryResponse = solrClient.query(index.getName(), solrQuery, SolrRequest.METHOD.POST);

            SearchResultConverterData searchResultConverterData = new SearchResultConverterData();
            searchResultConverterData.setFacetSearchContext(facetSearchContext);
            searchResultConverterData.setQueryResponse(queryResponse);
            SearchResult searchResult = super.getFacetSearchResultConverter().convert(searchResultConverterData);
            super.getFacetSearchContextFactory().getContext().setSearchResult(searchResult);
            super.getFacetSearchContextFactory().destroyContext();
            result = searchResult;
        } catch (SolrServerException | IOException | RuntimeException | SolrServiceException var19) {
            super.getFacetSearchContextFactory().destroyContext(var19);
            throw new FacetSearchException(var19.getMessage(), var19);
        } finally {
            IOUtils.closeQuietly(solrClient);
        }

        return result;

    }

	private void addCategoryFilters(final SolrQuery solrQuery) {

    	for (String exclude : categoryToExclude) {
    		try {
				String userGroup = exclude.split("\\|")[0];
				String category = exclude.split("\\|")[1];
				if (!userService.getCurrentUser().getAllGroups().stream().anyMatch(f -> f.getUid().equals(userGroup))) {
					solrQuery.addFilterQuery("-allCategories_string_mv:"+category);
				}
			}catch (IndexOutOfBoundsException ex){
				LOG.error(ex, ex);
			}
		}
	}

	public UserService getUserService() {
		return userService;
	}

	@Required
	public void setUserService(final UserService userService) {
		this.userService = userService;
	}
}
