package com.solgroup.core.login.impl;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.assertj.core.util.Sets;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.login.SolGroupLoginService;
import com.solgroup.core.service.b2bunit.SolGroupB2BUnitService;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.session.SessionService;

public class DefaultSolGroupLoginService implements SolGroupLoginService {
	
	private SolGroupB2BUnitService solGroupB2BUnitService;
	private SessionService sessionService;
	private CommonI18NService commonI18NService;
	
	
	private static final Logger LOG = Logger.getLogger(DefaultSolGroupLoginService.class);

	@Override
	public boolean prepareUserSession(UserModel currentUser, boolean fullMode) {

		if(currentUser instanceof B2BCustomerModel) {
			
			B2BCustomerModel currentB2BCustomer = (B2BCustomerModel)currentUser;
		
		
			// Check default b2bUnit
			if(currentB2BCustomer.getDefaultB2BUnit()==null) {
				LOG.warn("B2BCustomer " + currentB2BCustomer.getUid() + " has no default b2bUnit. Login can not be performed !!!!");
				return false;
			}
		
			// Get default B2BUnit
			B2BUnitModel b2bUnit = currentB2BCustomer.getDefaultB2BUnit();
			LOG.info("B2BCustomer " + currentB2BCustomer.getUid() + " belong to B2BUnit " + b2bUnit.getUid());
		
			// Evaluate branch
			if(fullMode) {
				Set<B2BUnitModel> branch = solGroupB2BUnitService.getBranch(b2bUnit);
				if(CollectionUtils.isNotEmpty(branch)) {
					sessionService.setAttribute(SolgroupCoreConstants.SESSION_PARAM_B2BUNITS_BRANCH_SET, branch);
					LOG.info("B2BUnit branch for B2BCustomer " + currentB2BCustomer.getUid());
					for(B2BUnitModel b2bUnit_inBranch : branch) {
						LOG.info("B2BUnit " + b2bUnit_inBranch.getUid());
					}
				}
				else {
					sessionService.setAttribute(SolgroupCoreConstants.SESSION_PARAM_B2BUNITS_BRANCH_SET, Sets.newHashSet());
					LOG.info("Empty branch for B2BCustomer " + currentB2BCustomer.getUid());
				}
			}
		
			// Evaluate visibilityCategories
			List<String> visibilityCategories = solGroupB2BUnitService.findVisibilityCategories(b2bUnit);
			if(CollectionUtils.isNotEmpty(visibilityCategories)) {
				sessionService.setAttribute(SolgroupCoreConstants.SESSION_PARAM_VSIBILITY_CATEGORIES, visibilityCategories);
				LOG.info("Visibility categories for B2BCustomer " + currentB2BCustomer.getUid() + " :");
				for(String categoryCode : visibilityCategories) {
					LOG.info(categoryCode);
				}
			}
			else {
				LOG.info("No visibility categories for B2BCustomer " + currentB2BCustomer.getUid());
				sessionService.removeAttribute(SolgroupCoreConstants.SESSION_PARAM_VSIBILITY_CATEGORIES);
			}
			
			// Evaluate user price group
			String upgCode = getSolGroupB2BUnitService().getUpgCode(b2bUnit);
			sessionService.setAttribute(SolgroupCoreConstants.SESSION_PARAM_PRICE_LIST_CODE, upgCode);
		
		}
		
		// Seet current currency
		if(currentUser instanceof CustomerModel) {
			CurrencyModel sessionCurrencyModel = ((CustomerModel)currentUser).getSessionCurrency();
			if(sessionCurrencyModel != null){
				commonI18NService.setCurrentCurrency(sessionCurrencyModel);
			}
		}
		
		return true;
		
	}

	protected SolGroupB2BUnitService getSolGroupB2BUnitService() {
		return solGroupB2BUnitService;
	}

	@Required
	public void setSolGroupB2BUnitService(SolGroupB2BUnitService solGroupB2BUnitService) {
		this.solGroupB2BUnitService = solGroupB2BUnitService;
	}

	protected SessionService getSessionService() {
		return sessionService;
	}

	@Required
	public void setSessionService(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	protected CommonI18NService getCommonI18NService() {
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(CommonI18NService commonI18NService) {
		this.commonI18NService = commonI18NService;
	}
	
	
	

}
