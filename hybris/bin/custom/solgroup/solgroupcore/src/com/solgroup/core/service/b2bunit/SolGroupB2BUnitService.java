package com.solgroup.core.service.b2bunit;

import java.util.List;

import com.solgroup.core.enums.B2BUnitTypeEnum;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.EmployeeModel;

public interface SolGroupB2BUnitService extends B2BUnitService<B2BUnitModel, B2BCustomerModel> {

	AddressModel getBillingAddress(B2BUnitModel b2bUnit);

	List<String> findVisibilityCategories(B2BUnitModel b2bUnit);

	String getUpgCode(B2BUnitModel unit);
	
	String getHybrisCode(String legalEntityErpCode, String erpCode, CMSSiteModel cmsSite, B2BUnitTypeEnum b2bUnitType);
	
	EmployeeModel getAgentForB2BUnit(B2BUnitModel b2bUnit);
	
}
