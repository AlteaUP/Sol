/**
 *
 */
package com.solgroup.core.service.categories.impl;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.impl.DefaultCategoryService;
import de.hybris.platform.category.model.CategoryModel;
import java.util.Collection;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.solgroup.core.service.categories.SolGroupCategoryService;
import com.solgroup.core.util.SolGroupUtilities;


/**
 * @author salvo
 *
 */
public class DefaultSolGroupCategoryService extends DefaultCategoryService implements SolGroupCategoryService
{

	private final Logger log = Logger.getLogger(SolGroupUtilities.class);

	@Override
	public boolean addCategoryToCollection(final String categoryCode, final CatalogVersionModel catalogVersion,
			final Collection<CategoryModel> categoryCollection)
	{
		// Check initial param
		if (categoryCollection == null)
		{
			log.error("categoryCollection param is null");
			return false;
		}
		if (StringUtils.isEmpty(categoryCode))
		{
			log.error("categoryCode param is empty or null");
			return false;
		}
		if (catalogVersion == null)
		{
			log.error("catalogVersion param is null");
			return false;
		}


		try
		{
			final CategoryModel category = getCategoryForCode(catalogVersion, categoryCode);
			if (category != null)
			{
				categoryCollection.add(category);
				return true;
			}
			else
			{
				log.warn("No category found for code " + categoryCode);
			}

		}
		catch (final Exception exc)
		{
			log.warn("Exception : " + exc.getMessage() + ". No category found for code " + categoryCode);
		}
		return false;
	}
	
	@Override
	public String getHybrisCategoryCode(String categoryErpCpde, BaseSiteModel site) {
		
		if(StringUtils.isEmpty(categoryErpCpde)) {
			log.error("Category code is empty");
			return "";
		}
		
		if(site == null) {
			log.error("CMSSite is null");
			return "";
		}
		
		return site.getUid() + "_" + categoryErpCpde;
	}

}
