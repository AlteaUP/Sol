/**
 *
 */
package com.solgroup.core.interceptors.b2bunit;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author salvo
 *
 */
public class SolGroupB2BUnitSessionCurrencyPrepareInterceptor implements PrepareInterceptor<B2BUnitModel>
{

	private ModelService modelService;


	@Override
	public void onPrepare(final B2BUnitModel b2bUnit, final InterceptorContext context) throws InterceptorException
	{
		//Work only on updated b2bUnits
		if (getModelService().isNew(b2bUnit))
		{
			return;
		}

		// Check if session currency was changed
		final CurrencyModel previousSessionCurrency = b2bUnit.getItemModelContext().getOriginalValue(B2BUnitModel.SESSIONCURRENCY);
		final CurrencyModel currentSessionCurrency = b2bUnit.getSessionCurrency();
		if ( (previousSessionCurrency==null && currentSessionCurrency!=null) || 
			 (previousSessionCurrency != null && currentSessionCurrency != null && !previousSessionCurrency.equals(currentSessionCurrency)))
		{

			final Set<PrincipalModel> members = b2bUnit.getMembers();
			if (CollectionUtils.isNotEmpty(members))
			{
				for (final PrincipalModel member : members)
				{
					if (member instanceof B2BUnitModel)
					{
						((B2BUnitModel) member).setSessionCurrency(currentSessionCurrency);
						getModelService().save(member);
					}
					else if (member instanceof B2BCustomerModel)
					{
						((B2BCustomerModel) member).setSessionCurrency(currentSessionCurrency);
						getModelService().save(member);
					}
				}
			}
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


}
