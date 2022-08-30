package com.solgroup.core.search.solrfacetsearch.search.impl.populators;

import java.util.ArrayList;
import java.util.List;

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.search.FacetValue;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.impl.populators.FacetSearchResultFacetsPopulator;
import org.apache.solr.client.solrj.response.FacetField.Count;

public class SolGroupFacetSearchResultFacetsPopulator extends FacetSearchResultFacetsPopulator {

	
	protected List<FacetValue> populateFacetValues(List<Count> sourceFacetValues, SearchQuery searchQuery,
			IndexedProperty indexedProperty, String fieldName, boolean showFacet, long maxFacetValueCount) {
		ArrayList facetValues = new ArrayList();
		Object facetValueDisplayNameProvider = this
				.getFacetValueDisplayNameProvider(indexedProperty.getFacetDisplayNameProvider());
		sourceFacetValues.stream().filter((count) -> {
			return showFacet || count.getCount() <= maxFacetValueCount;
		}).forEach((count) -> {
			boolean selected = this.isFacetValueSelected(searchQuery, fieldName, count.getName());
			String displayName = this.getFacetValueDisplayName(searchQuery, indexedProperty,
					facetValueDisplayNameProvider, count.getName());
			if (displayName == null) {
				facetValues.add(new FacetValue(count.getName(), count.getCount(), selected));
			} else {
				facetValues.add(new FacetValue(count.getName(), displayName, count.getCount(), selected));
			}

		});
		return facetValues;
	}

	protected List<FacetValue> populateFacetValues(List<Count> sourceFacetValues, SearchQuery searchQuery,
			IndexedProperty indexedProperty, de.hybris.platform.solrfacetsearch.search.FacetField facetField,
			String fieldName, boolean showFacet, long maxFacetValueCount) {
		ArrayList facetValues = new ArrayList();
		Object facetValueDisplayNameProvider = this
				.getFacetValueDisplayNameProvider(facetField.getDisplayNameProvider());
		sourceFacetValues.stream().filter((count) -> {
			return showFacet || count.getCount() <= maxFacetValueCount;
		}).forEach((count) -> {
			boolean selected = this.isFacetValueSelected(searchQuery, fieldName, count.getName());
			String displayName = this.getFacetValueDisplayName(searchQuery, indexedProperty,
					facetValueDisplayNameProvider, count.getName());
			if (displayName == null) {
				facetValues.add(new FacetValue(count.getName(), count.getCount(), selected));
			} else {
				facetValues.add(new FacetValue(count.getName(), displayName, count.getCount(), selected));
			}

		});
		return facetValues;
	}
	
	
}
