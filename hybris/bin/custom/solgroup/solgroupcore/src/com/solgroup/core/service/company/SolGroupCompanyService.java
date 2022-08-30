package com.solgroup.core.service.company;

import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.core.model.user.EmployeeModel;

public interface SolGroupCompanyService {
	public <T extends CompanyModel> EmployeeModel findAssignedAgents(T b2bUnit);
}
