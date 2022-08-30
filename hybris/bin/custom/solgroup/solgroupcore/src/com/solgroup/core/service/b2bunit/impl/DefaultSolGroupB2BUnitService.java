package com.solgroup.core.service.b2bunit.impl;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;
import com.solgroup.core.dao.b2bunit.SolGroupB2BUnitDao;
import com.solgroup.core.enums.B2BUnitTypeEnum;
import com.solgroup.core.service.b2bcustomer.impl.SolGroupB2BCustomerService;
import com.solgroup.core.service.b2bunit.SolGroupB2BUnitService;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.impl.DefaultB2BUnitService;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.commerceservices.model.OrgUnitModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import jersey.repackaged.com.google.common.collect.Sets;

public class DefaultSolGroupB2BUnitService extends DefaultB2BUnitService implements SolGroupB2BUnitService {
	
	private SolGroupB2BCustomerService solGroupB2BCustomerService;
	private SolGroupB2BUnitDao solGroupB2BUnitDao;

	private static final Logger LOG = Logger.getLogger(DefaultSolGroupB2BUnitService.class);

	
	@Override
	public void disableUnit(B2BUnitModel unit) {
		ServicesUtil.validateParameterNotNullStandardMessage("unit", unit);
		this.toggleUnit(unit, Boolean.FALSE);
	}
	
	@Override
	public void enableUnit(B2BUnitModel unit) {
		ServicesUtil.validateParameterNotNullStandardMessage("unit", unit);
		this.toggleUnit(unit, Boolean.TRUE);
	}

	
	@Override
	public void toggleUnit(B2BUnitModel b2bUnit, Boolean enable) {
		
		// Disable b2bUit
		if(!enable && b2bUnit.getActive()) {
			toggleCustomers(b2bUnit, enable);
			b2bUnit.setActive(enable);
			getModelService().save(b2bUnit);
		}
		
		// Enable b2bUnit
		else if(enable) {
			b2bUnit.setActive(enable);
			getModelService().save(b2bUnit);
			toggleCustomers(b2bUnit, enable);
		}
		
		
	}
	
	
	public void toggleCustomers(B2BUnitModel b2bUnit, Boolean enable) {
		Set<B2BCustomerModel> b2bCustomers = Sets.newHashSet();
		b2bCustomers = super.getB2BCustomers(b2bUnit);
		if(CollectionUtils.isNotEmpty(b2bCustomers)) {
			for(B2BCustomerModel b2bCustomer : b2bCustomers) {
				getSolGroupB2BCustomerService().toggleB2BCustomer(b2bCustomer, enable, Boolean.FALSE);
			}
			getModelService().saveAll(b2bCustomers);
		}
	}
	

	@Override
	public AddressModel getBillingAddress(B2BUnitModel b2bUnit) {
		
		if(b2bUnit.getBillingAddress()!=null) {
			return b2bUnit.getBillingAddress();
		}
			
		for(AddressModel address : b2bUnit.getAddresses()){
			if(address.getBillingAddress()){
				return address;
			}
		}
		
		
		CompanyModel responsibleCompany = b2bUnit.getResponsibleCompany();
		if(responsibleCompany.getBillingAddress()!=null) {
			return responsibleCompany.getBillingAddress();
		}
		for(AddressModel address : responsibleCompany.getAddresses()){
			if(address.getBillingAddress()){
				return address;
			}
		}
		
		
		return b2bUnit.getShippingAddress();
		
	}
	
	@Override
	public List<String> findVisibilityCategories(B2BUnitModel b2bUnit) {
		LOG.info("Find visibility categories for B2BUnit " + b2bUnit.getUid());
		List<String> visibilityCategories = getSolGroupB2BUnitDao().findVisibilityCategories(b2bUnit);
		if(visibilityCategories==null) {
			return Lists.newArrayList();
		}
		else {
			return visibilityCategories;
		}
	}
	
	@Override
	public String getUpgCode(B2BUnitModel unit){
		String upgCode = "";
		if(unit.getUserPriceGroup() != null){
		
			upgCode = unit.getUserPriceGroup().getCode();
			
			if(StringUtils.isEmpty(upgCode)){
				CompanyModel orgUnit_2lvl = unit.getResponsibleCompany();
				
				if(orgUnit_2lvl instanceof OrgUnitModel){
					upgCode = orgUnit_2lvl.getUserPriceGroup().getCode();
					if(upgCode.isEmpty()){
						CompanyModel orgUnit_1lvl = orgUnit_2lvl.getResponsibleCompany();
						if(orgUnit_1lvl instanceof OrgUnitModel){
							upgCode = orgUnit_1lvl.getUserPriceGroup().getCode();
						}
					}
				}
			}
		}
		
		return upgCode;
	}
	
	@Override
	public String getHybrisCode(String legalEntityErpCode, String erpCode, CMSSiteModel cmsSite, B2BUnitTypeEnum b2bUnitType) {
		return cmsSite.getUid() + "_" + legalEntityErpCode + "_" + erpCode + "__" + b2bUnitType.getCode();
	}

	@Override
	public EmployeeModel getAgentForB2BUnit(B2BUnitModel b2bUnit) {
		return getSolGroupB2BUnitDao().findAgentForB2BUnit(b2bUnit);
	}

	
	protected SolGroupB2BCustomerService getSolGroupB2BCustomerService() {
		return solGroupB2BCustomerService;
	}

	@Required
	public void setSolGroupB2BCustomerService(SolGroupB2BCustomerService solGroupB2BCustomerService) {
		this.solGroupB2BCustomerService = solGroupB2BCustomerService;
	}

	protected SolGroupB2BUnitDao getSolGroupB2BUnitDao() {
		return solGroupB2BUnitDao;
	}

	@Required
	public void setSolGroupB2BUnitDao(SolGroupB2BUnitDao solGroupB2BUnitDao) {
		this.solGroupB2BUnitDao = solGroupB2BUnitDao;
	}

	


	

	

}
