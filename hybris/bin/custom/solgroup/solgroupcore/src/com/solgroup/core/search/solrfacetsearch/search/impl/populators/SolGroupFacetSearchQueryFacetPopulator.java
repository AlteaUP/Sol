package com.solgroup.core.search.solrfacetsearch.search.impl.populators;
import org.apache.solr.client.solrj.SolrQuery;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;
import de.hybris.platform.solrfacetsearch.search.impl.populators.FacetSearchQueryFacetsPopulator;

public class SolGroupFacetSearchQueryFacetPopulator extends FacetSearchQueryFacetsPopulator {

	@Override
	public void populate(SearchQueryConverterData source, SolrQuery target) {
		
		super.populate(source, target);
		target.removeFacetField("allCategories_string_mv");
	}
	
}
