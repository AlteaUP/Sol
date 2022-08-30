package com.solgroup.core.service.b2bcustomer.impl;



import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.common.daos.CommonDao;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BCustomerService;
import de.hybris.platform.b2b.services.impl.DefaultB2BCustomerService;
import de.hybris.platform.servicelayer.model.ModelService;

public class DefaultSolGroupB2BCustomerService extends DefaultB2BCustomerService implements SolGroupB2BCustomerService<B2BCustomerModel,B2BUnitModel> {
	
	private ModelService modelService;
	private CommonDao commonDao;
	
	@Override
	public void toggleB2BCustomer(B2BCustomerModel b2bCustomer, Boolean active, Boolean saveCustomer) {
		if(b2bCustomer!=null && active!=null) {
			b2bCustomer.setActive(active);
			b2bCustomer.setLoginDisabled(!active);
			if(BooleanUtils.isTrue(saveCustomer)) {
				getModelService().save(b2bCustomer);
			}
		}
	}
	
	@Override
	public B2BCustomerModel findB2BCustomerByEmail(String email) {
		return getCommonDao().findItemByUniqueCode(B2BCustomerModel._TYPECODE, B2BCustomerModel.EMAIL, email, B2BCustomerModel.class);
	}
	
	
	protected ModelService getModelService() {
		return modelService;
	}

	@Required
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	protected CommonDao getCommonDao() {
		return commonDao;
	}

	@Required
	public void setCommonDao(CommonDao commonDao) {
		this.commonDao = commonDao;
	}
	
	

	
}
