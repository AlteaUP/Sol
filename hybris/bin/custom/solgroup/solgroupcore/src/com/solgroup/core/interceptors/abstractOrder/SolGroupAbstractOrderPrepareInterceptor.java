/**
 *
 */
package com.solgroup.core.interceptors.abstractOrder;


import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author salvo
 *
 */
public class SolGroupAbstractOrderPrepareInterceptor implements PrepareInterceptor<AbstractOrderModel>
{

	private ModelService modelService;
	

	private final Logger LOG = Logger.getLogger(SolGroupAbstractOrderPrepareInterceptor.class);


	@Override
	public void onPrepare(final AbstractOrderModel abstractOrder, final InterceptorContext context) throws InterceptorException
	{
		//Manage only new abstractOrder
		if (context.isNew(abstractOrder) || context.isModified(abstractOrder, AbstractOrderModel.USER))
		{
			
			UserModel user = abstractOrder.getUser();
			if(user instanceof B2BCustomerModel) {
				B2BCustomerModel b2bCustomer = (B2BCustomerModel)user;
				if(b2bCustomer.getDefaultB2BUnit() == null) {
					throw new InterceptorException("B2bCustomer " + b2bCustomer.getUid() + " can not create an abstarctOrder. No b2bUnit is assigned !!!");
				}
				B2BUnitModel b2bUnit = b2bCustomer.getDefaultB2BUnit();
				abstractOrder.setUnit(b2bUnit);
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
