/**
 *
 */
package com.solgroup.services.dataimport.daos.impl;

import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;

import java.util.List;

import com.solgroup.services.dataimport.daos.SolGroupImportUnitsDao;


/**
 * @author marcodanielecoppola
 *
 */
public class DefaultSolGroupImportUnitsDao extends AbstractItemDao implements SolGroupImportUnitsDao
{

	@Override
	public List<UnitModel> findAllUnits()
	{

		final String query = "SELECT {u." + UnitModel.PK + "} from {" + UnitModel._TYPECODE + " as u}";
		final FlexibleSearchQuery flexQuery = new FlexibleSearchQuery(query);
		return getFlexibleSearchService().<UnitModel> search(flexQuery).getResult();
	}




}
