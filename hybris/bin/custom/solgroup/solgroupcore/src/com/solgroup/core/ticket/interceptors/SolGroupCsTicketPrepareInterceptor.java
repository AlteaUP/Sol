package com.solgroup.core.ticket.interceptors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.company.SolGroupCompanyService;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.ticket.model.CsTicketModel;

public class SolGroupCsTicketPrepareInterceptor implements PrepareInterceptor<CsTicketModel> {

	private final Logger LOG = Logger.getLogger(SolGroupCsTicketPrepareInterceptor.class);

	private UserService userService;
	private SolGroupCompanyService solgroupCompanyService;
	private ModelService modelService;


	
	@Override
	public void onPrepare(CsTicketModel arg0, InterceptorContext arg1) throws InterceptorException {
		
		if (!getModelService().isNew(arg0))
		{
			B2BCustomerModel user = (B2BCustomerModel) this.userService.getCurrentUser();

			EmployeeModel agent = getSolgroupCompanyService().findAssignedAgents(user.getDefaultB2BUnit());

			if (agent == null)
				LOG.warn("I could not find a default agent for the B2BUnit/customer " + user.getDefaultB2BUnit().getUid()
						+ " - " + user.getUid());
			else {
			//	arg0.setAssignedTo(Lists.newArrayList(agent));
				arg0.setAssignedAgent(agent);
				

			}
		}
	}

	protected UserService getUserService() {
		return userService;
	}

	@Required
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	protected SolGroupCompanyService getSolgroupCompanyService() {
		return solgroupCompanyService;
	}

	@Required
	public void setSolgroupCompanyService(SolGroupCompanyService solgroupCompanyService) {
		this.solgroupCompanyService = solgroupCompanyService;
	}

	protected ModelService getModelService() {
		return modelService;
	}

	@Required
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}



}
