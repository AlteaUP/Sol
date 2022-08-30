package com.solgroup.core.service.company.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.b2bunit.impl.DefaultSolGroupB2BUnitService;
import com.solgroup.core.service.company.SolGroupCompanyService;

import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;

public class DefaultSolgroupCompanyService implements SolGroupCompanyService {

	private final Logger LOG = Logger.getLogger(DefaultSolGroupB2BUnitService.class);

	private static final String VENDOR_CODE = "vendorCode";
	private GenericDao<EmployeeModel> employeeGenericDao;

	
	
	
	@Override
    public <T extends CompanyModel> EmployeeModel findAssignedAgents(final T b2bUnit) {

		if (Strings.isBlank(b2bUnit.getVendorCode()) && !(b2bUnit instanceof CompanyModel)) {
			if (LOG.isInfoEnabled()) {
                LOG.info("B2BUnit don't have a vendor code. We will use the vendor of parent.");
            }
			return findAssignedAgents(b2bUnit.getResponsibleCompany());
		} else {
			if (Strings.isBlank(b2bUnit.getVendorCode())) {
				LOG.warn("The company '" + b2bUnit.getErpCode() + " - " + b2bUnit.getName()
						+ "' don't have a default vendor.");
				return null;
			}
			List<EmployeeModel> employees;
			
			final Map<String, String> parameters = new HashMap<>();
			parameters.put(VENDOR_CODE, b2bUnit.getVendorCode());

			employees = getEmployeeGenericDao().find(parameters);
            if (employees.isEmpty()) {
                LOG.warn("B2BUnit have an unknow vendor code. The list of vendors will be empty!");
                return null;
            }
			return employees.get(0);
		}
	}
	
	protected GenericDao<EmployeeModel> getEmployeeGenericDao() {
		return employeeGenericDao;
	}

	@Required
	public void setEmployeeGenericDao(final GenericDao<EmployeeModel> employeeGenericDao) {
		this.employeeGenericDao = employeeGenericDao;
	}
	
}
