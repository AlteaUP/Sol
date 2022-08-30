/**
 *
 */
package com.solgroup.core.service.categories;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;


import java.util.Collection;


/**
 * @author salvo
 *
 */
public interface SolGroupCategoryService extends CategoryService
{

	boolean addCategoryToCollection(final String categoryCode, final CatalogVersionModel catalogVersion,
			final Collection<CategoryModel> categoryCollection);
	
	String getHybrisCategoryCode(String categoryErpCpde, BaseSiteModel site);

}
