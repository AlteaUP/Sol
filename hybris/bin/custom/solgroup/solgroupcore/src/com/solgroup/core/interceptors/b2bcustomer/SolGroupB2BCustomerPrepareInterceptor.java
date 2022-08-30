/**
 *
 */
package com.solgroup.core.interceptors.b2bcustomer;


import de.hybris.platform.b2b.enums.B2BGroupEnum;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.servicelayer.interceptor.InitDefaultsInterceptor;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.assertj.core.util.Sets;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.enums.B2BUnitTypeEnum;


/**
 * @author salvo
 *
 */
public class SolGroupB2BCustomerPrepareInterceptor implements PrepareInterceptor<B2BCustomerModel>
{

	private ModelService modelService;
	private UserService userService;

	private final Logger LOG = Logger.getLogger(SolGroupB2BCustomerPrepareInterceptor.class);


	@Override
	public void onPrepare(final B2BCustomerModel b2bCustomer, final InterceptorContext context) throws InterceptorException
	{
		
		// Manage groups
		Set<PrincipalGroupModel> currentGroups = b2bCustomer.getGroups();
		Set<PrincipalGroupModel> newGroups = Sets.newHashSet();
		if(currentGroups!=null) {
			for(PrincipalGroupModel principalGroup : currentGroups) {
				if(
						!(principalGroup instanceof CompanyModel)
						&& !principalGroup.getUid().equals(SolgroupCoreConstants.CUSTOMER_GROUP_ID)
						&& !principalGroup.getUid().equals(B2BGroupEnum.B2BCUSTOMERGROUP.getCode())
						&& !principalGroup.getUid().equals(SolgroupCoreConstants.ORGANIZATION_GROUP_ID)
						) {
					newGroups.add(principalGroup);
				}
			}
		}

		// Add default b2bUnit to groups
		newGroups.add(b2bCustomer.getDefaultB2BUnit());
		
		// Manage b2bCustomer related to organization (1st or 2nd level) : they belong to asmGroup (to access ASM) and CUSTOMERGROUP (to login but not purchase)
		if(b2bCustomer.getDefaultB2BUnit()!=null && 
				b2bCustomer.getDefaultB2BUnit().getB2bUnitType()!=null && 
				(
						b2bCustomer.getDefaultB2BUnit().getB2bUnitType().equals(B2BUnitTypeEnum.ORGANIZATION_1LEVEL) ||
						b2bCustomer.getDefaultB2BUnit().getB2bUnitType().equals(B2BUnitTypeEnum.ORGANIZATION_2LEVEL)
						)) {
			newGroups.add(getUserService().getUserGroupForUID(SolgroupCoreConstants.CUSTOMER_GROUP_ID));
			newGroups.add(getUserService().getUserGroupForUID(SolgroupCoreConstants.ORGANIZATION_GROUP_ID));
		}
		
		// Manage b2bCustomer related to b2bUnit : they belong only to purchase role (b2bCustomerGroup)
		else if(b2bCustomer.getDefaultB2BUnit()!=null && b2bCustomer.getDefaultB2BUnit().getB2bUnitType()!=null && b2bCustomer.getDefaultB2BUnit().getB2bUnitType().equals(B2BUnitTypeEnum.POINT_OF_SALE)){
			newGroups.add(getUserService().getUserGroupForUID(B2BGroupEnum.B2BCUSTOMERGROUP.getCode()));
		}

		// Set b2bCustomer groups
		b2bCustomer.setGroups(newGroups);
		
		
		//Manage not new b2bCustomers
		if (!getModelService().isNew(b2bCustomer))
		{
			// If identityConfirmation process was not completed, set b2bCustomer as no active
			if(BooleanUtils.isFalse(b2bCustomer.getIdentityConfirmationCompleted())) {
				b2bCustomer.setActive(Boolean.FALSE);
				b2bCustomer.setLoginDisabled(Boolean.TRUE);
			}
			
		}
		// Manage new b2bCustomers
		else {
			// Set b2bCustomer as not active
			b2bCustomer.setActive(Boolean.FALSE);
			b2bCustomer.setLoginDisabled(Boolean.TRUE);
			b2bCustomer.setIdentityConfirmationSent(Boolean.FALSE);
			b2bCustomer.setIdentityConfirmationCompleted(Boolean.FALSE);
		}
		
		// Set cmsSite (if not already setted)
		if (b2bCustomer.getCmsSite() == null && b2bCustomer.getDefaultB2BUnit().getCmsSite() != null)
		{
			b2bCustomer.setCmsSite(b2bCustomer.getDefaultB2BUnit().getCmsSite());
		}
		
		// Set session currency (if not already setted)
		if (b2bCustomer.getSessionCurrency()==null && b2bCustomer.getDefaultB2BUnit().getSessionCurrency() != null)
		{
			b2bCustomer.setSessionCurrency(b2bCustomer.getDefaultB2BUnit().getSessionCurrency());
		}

		// Default condition : if sessionCurency on b2bCustomer is null, take it from siteSessionCurrency
		if (b2bCustomer.getSessionCurrency() == null && b2bCustomer.getCmsSite() != null)
		{
			final CurrencyModel currency = b2bCustomer.getCmsSite().getStores().get(0).getDefaultCurrency();
			b2bCustomer.setSessionCurrency(currency);
		}

		
	}


	protected ModelService getModelService()
	{
		return modelService;
	}


	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}


	protected UserService getUserService()
	{
		return userService;
	}


	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}






}
