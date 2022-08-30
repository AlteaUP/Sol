/**
 *
 */
package com.solgroup.facades.populators.solrfacetsearch;

import de.hybris.platform.commercefacades.product.data.CategoryData;
import de.hybris.platform.commercefacades.search.data.SearchStateData;
import de.hybris.platform.commercefacades.search.solrfacetsearch.converters.populator.SolrSearchStatePopulator;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchQueryData;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchQueryTermData;

import org.apache.commons.collections.CollectionUtils;

import com.solgroup.core.constants.SolgroupCoreConstants;


/**
 * @author salvo
 *
 */
public class SolGroupSolrSearchStatePopulator extends SolrSearchStatePopulator
{

	@Override
	protected void populateCategorySearchUrl(final SolrSearchQueryData source, final SearchStateData target)
	{

		if (CollectionUtils.isNotEmpty(source.getFilterTerms()))
		{
			for (final SolrSearchQueryTermData queryTerm : source.getFilterTerms())
			{
				if (queryTerm.getKey().equals(SolgroupCoreConstants.APPLICATION_CATEGORY_LEVEL1_INDEXED_PROPERTY)
						|| queryTerm.getKey().equals(SolgroupCoreConstants.APPLICATION_CATEGORY_LEVEL2_INDEXED_PROPERTY)
						|| queryTerm.getKey().equals(SolgroupCoreConstants.TOP_BAR_CATEGORY_LEVEL1_INDEXED_PROPERTY)
						|| queryTerm.getKey().equals(SolgroupCoreConstants.TOP_BAR_CATEGORY_LEVEL2_INDEXED_PROPERTY))
				{
					final CategoryData categoryData = new CategoryData();
					categoryData.setCode(queryTerm.getValue());
					target.setUrl(getCategoryDataUrlResolver().resolve(categoryData));
					return;
				}
			}
		}

		super.populateCategorySearchUrl(source, target);
	}


}
