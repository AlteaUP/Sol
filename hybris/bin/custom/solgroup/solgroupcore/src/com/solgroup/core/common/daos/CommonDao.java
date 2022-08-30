/**
 *
 */
package com.solgroup.core.common.daos;

import de.hybris.platform.core.model.ItemModel;


/**
 * @author salvo
 *
 */
public interface CommonDao
{

	//ItemModel findItemByUniqueCode(String itemType, String key, String value);

	<T extends ItemModel> T findItemByUniqueCode(final String itemType, final String key, final String value,
			final Class<T> returnType);



}
