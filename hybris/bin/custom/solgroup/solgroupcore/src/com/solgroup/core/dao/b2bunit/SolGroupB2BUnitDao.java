/**
 *
 */
package com.solgroup.core.dao.b2bunit;

import java.util.List;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.user.EmployeeModel;

/**
 * @author salvo
 *
 */
public interface SolGroupB2BUnitDao
{

	CompanyModel findCompany(String erpCode, CMSSiteModel cmsSite);
	
	B2BUnitModel findB2BUnit(CompanyModel legalEntyìityErpCode, String erpCode, CMSSiteModel cmsSite);

	List<String> findVisibilityCategories(B2BUnitModel b2bUnit);
	
	EmployeeModel findAgentForB2BUnit(B2BUnitModel b2bUnit);
	
	
}
