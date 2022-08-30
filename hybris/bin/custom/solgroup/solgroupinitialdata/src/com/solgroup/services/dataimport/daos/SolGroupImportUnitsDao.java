/**
 *
 */
package com.solgroup.services.dataimport.daos;

import de.hybris.platform.core.model.product.UnitModel;

import java.util.List;


/**
 * @author marcodanielecoppola
 *
 */
public interface SolGroupImportUnitsDao
{

	public List<UnitModel> findAllUnits();

}
