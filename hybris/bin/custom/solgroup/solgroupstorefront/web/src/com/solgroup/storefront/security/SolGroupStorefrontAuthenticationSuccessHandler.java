
/**
 *
 */
package com.solgroup.storefront.security;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.assertj.core.util.Sets;
import org.springframework.security.core.Authentication;

import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.login.SolGroupLoginService;
import com.solgroup.core.service.b2bunit.impl.DefaultSolGroupB2BUnitService;

import de.hybris.platform.acceleratorstorefrontcommons.security.StorefrontAuthenticationSuccessHandler;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.commerceservices.model.OrgUnitModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;


/**
 * @author salvo
 *
 */
public class SolGroupStorefrontAuthenticationSuccessHandler extends StorefrontAuthenticationSuccessHandler
{

	private static final Logger LOG = Logger.getLogger(SolGroupStorefrontAuthenticationSuccessHandler.class);
	
	@Resource(name="userService")
	UserService userService;
	
//	@Resource(name="sessionService")
//	SessionService sessionService;
	
//	@Resource(name="commonI18NService")
//    private CommonI18NService commonI18NService;

//	@Resource(name="solGroupB2BUnitService")
//	private DefaultSolGroupB2BUnitService solGroupB2BUnitService;
	

	@Resource(name="solGroupLoginService")
	SolGroupLoginService solGroupLoginService;
	
	@Override
	public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
			final Authentication authentication) throws IOException, ServletException
	{
		
		// Retrieve logged user
		UserModel currentUser = userService.getCurrentUser();
		LOG.info("Login performed by " + currentUser.getUid());
		
		boolean prepareUserSessionResult = solGroupLoginService.prepareUserSession(currentUser,true);
		if(!prepareUserSessionResult) {
			invalidateSession(request, response);
			return;
		}

		
		
//		if(currentUser instanceof B2BCustomerModel) {
//			
//			B2BCustomerModel currentB2BCustomer = (B2BCustomerModel)currentUser;
//		
//		
//			// Check default b2bUnit
//			if(currentB2BCustomer.getDefaultB2BUnit()==null) {
//				LOG.warn("B2BCustomer " + currentB2BCustomer.getUid() + " has no default b2bUnit. Login can not be performed !!!!");
//				invalidateSession(request, response);
//				return;
//			}
//		
//			// Get default B2BUnit
//			B2BUnitModel b2bUnit = currentB2BCustomer.getDefaultB2BUnit();
//			LOG.info("B2BCustomer " + currentB2BCustomer.getUid() + " belong to B2BUnit " + b2bUnit.getUid());
//		
//			// Evaluate branch
//			Set<B2BUnitModel> branch = solGroupB2BUnitService.getBranch(b2bUnit);
//			if(CollectionUtils.isNotEmpty(branch)) {
//				sessionService.setAttribute(SolgroupCoreConstants.B2BUNITS_BRANCH_SET, branch);
//				LOG.info("B2BUnit branch for B2BCustomer " + currentB2BCustomer.getUid());
//				for(B2BUnitModel b2bUnit_inBranch : branch) {
//					LOG.info("B2BUnit " + b2bUnit_inBranch.getUid());
//				}
//			}
//			else {
//				sessionService.setAttribute(SolgroupCoreConstants.B2BUNITS_BRANCH_SET, Sets.newHashSet());
//				LOG.info("Empty branch for B2BCustomer " + currentB2BCustomer.getUid());
//			}
//		
//			// Evaluate visibilityCategories
//			List<String> visibilityCategories = solGroupB2BUnitService.findVisibilityCategories(b2bUnit);
//			if(CollectionUtils.isNotEmpty(visibilityCategories)) {
//				sessionService.setAttribute(SolgroupCoreConstants.VSIBILITY_CATEGORIES, visibilityCategories);
//				LOG.info("Visibility categories for B2BCustomer " + currentB2BCustomer.getUid() + " :");
//				for(String categoryCode : visibilityCategories) {
//					LOG.info(categoryCode);
//				}
//			}
//			else {
//				LOG.info("No visibility categories for B2BCustomer " + currentB2BCustomer.getUid());
//				sessionService.removeAttribute(SolgroupCoreConstants.VSIBILITY_CATEGORIES);
//			}
//			
//			// Evaluate user price group
//			String upgCode = getUpgCode(b2bUnit);
//			sessionService.setAttribute("priceListCode", upgCode);
//		
//		}
//		
//		// Seet current currency
//		if(currentUser instanceof CustomerModel) {
//			CurrencyModel sessionCurrencyModel = ((CustomerModel)currentUser).getSessionCurrency();
//			if(sessionCurrencyModel != null){
//				commonI18NService.setCurrentCurrency(sessionCurrencyModel);
//			}
//		}
		

		super.onAuthenticationSuccess(request, response, authentication);
	}
	
	
	
//	private String getUpgCode(B2BUnitModel unit){
//		String upgCode = "";
//		if(unit.getUserPriceGroup() != null){
//		
//			upgCode = unit.getUserPriceGroup().getCode();
//			
//			if(StringUtils.isEmpty(upgCode)){
//				CompanyModel orgUnit_2lvl = unit.getResponsibleCompany();
//				
//				if(orgUnit_2lvl instanceof OrgUnitModel){
//					upgCode = orgUnit_2lvl.getUserPriceGroup().getCode();
//					if(upgCode.isEmpty()){
//						CompanyModel orgUnit_1lvl = orgUnit_2lvl.getResponsibleCompany();
//						if(orgUnit_1lvl instanceof OrgUnitModel){
//							upgCode = orgUnit_1lvl.getUserPriceGroup().getCode();
//						}
//					}
//				}
//			}
//		}
//		
//		return upgCode;
//	}

}
