/**
 *
 */
package com.solgroup.core.common.daos.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.solgroup.core.common.daos.CommonDao;


/**
 * @author salvo
 *
 */
public class DefaultCommonDao extends AbstractItemDao implements CommonDao
{

	private final Logger log = Logger.getLogger(DefaultCommonDao.class);


	@Override
	public <T extends ItemModel> T findItemByUniqueCode(final String itemType, final String key, final String value,
			final Class<T> returnType)
	{
		final String query = "select {item." + ItemModel.PK + "} from {" + itemType + " as item} where {item." + key + "}=?value";
		final FlexibleSearchQuery fsq = new FlexibleSearchQuery(query);
		final Map params = new HashMap();
		params.put("value", value);
		fsq.addQueryParameters(params);
		final List<ItemModel> itemModels = getFlexibleSearchService().<ItemModel> search(fsq).getResult();

		if (CollectionUtils.isNotEmpty(itemModels))
		{

			// More item found --> ERROR
			if (itemModels.size() > 1)
			{
				log.error("More items for type " + itemType + " found with " + key + " = " + value);
				return null;
			}

			// Only 1 item found --> IT'S OK
			final T t = returnType.cast(itemModels.get(0));
			return t;

			// return (T) itemModels.get(0);
		}

		// No item found
		else
		{
			return null;
		}
	}

}
