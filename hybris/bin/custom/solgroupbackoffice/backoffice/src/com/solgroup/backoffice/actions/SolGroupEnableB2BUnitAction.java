/**
 *
 */
package com.solgroup.backoffice.actions;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

import javax.annotation.Resource;

import org.apache.commons.lang.BooleanUtils;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationUtils;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.solgroup.core.service.b2bunit.impl.DefaultSolGroupB2BUnitService;


/**
 * @author salvo
 *
 */
public class SolGroupEnableB2BUnitAction implements CockpitAction<B2BUnitModel, Object>
{


	@Resource
	private UserService userService;

	@Resource
	private DefaultSolGroupB2BUnitService solGroupB2BUnitService;
	
	@Resource
	private ModelService modelService;



	@Override
	public boolean canPerform(final ActionContext<B2BUnitModel> ctx)
	{
		final Object data = ctx.getData();

		if ((data != null) && ((data instanceof B2BUnitModel)))
		{
			final B2BUnitModel b2bUnitModel = (B2BUnitModel) data;
			final UserModel currentUser = userService.getCurrentUser();
			final boolean isUserMemberOfAdminGroup = userService.isMemberOfGroup(currentUser, userService.getAdminUserGroup());
			final boolean isUserMemberOfB2BAdminGroup = userService.isMemberOfGroup(currentUser,
					userService.getUserGroupForUID("b2badmingroup"));
			final boolean isActive = b2bUnitModel.getActive().booleanValue();

			return ((isUserMemberOfAdminGroup) || (isUserMemberOfB2BAdminGroup)) && (!isActive);
		}


		return false;
	}


	@Override
	public ActionResult<Object> perform(final ActionContext<B2BUnitModel> ctx)
	{
		final Object data = ctx.getData();

		if ((data != null) && ((data instanceof B2BUnitModel)))
		{
			final B2BUnitModel b2bUnitModel = (B2BUnitModel) data;
			//	      if (!canActivate(b2bUnitModel))
			//	      {
			//	        NotificationUtils.notifyUser(ctx.getLabel("hmc.action.b2bunitenable.cannotactivate"), NotificationEvent.Type.SUCCESS);
			//	        return new ActionResult("success", b2bUnitModel);
			//	      }

			solGroupB2BUnitService.enableUnit(b2bUnitModel);
			modelService.refresh(b2bUnitModel);
			if(BooleanUtils.isTrue(b2bUnitModel.getActive())) {
				NotificationUtils.notifyUser(ctx.getLabel("com.solgroup.backoffice.action.b2bunitenable.success"),
						NotificationEvent.Type.SUCCESS);
				return new ActionResult("success", b2bUnitModel);

			}
			else {
				NotificationUtils.notifyUser(ctx.getLabel("com.solgroup.backoffice.action.b2bunitenable.error"),
						NotificationEvent.Type.FAILURE);
				return new ActionResult("error", b2bUnitModel);
			}
		}


		return new ActionResult("error");
	}

	public String getConfirmationMessage(ActionContext<B2BUnitModel> ctx)
	{
	    return ctx.getLabel("com.solgroup.backoffice.action.b2bunitenable.confirm");
	}
	  

	public boolean needsConfirmation(ActionContext<B2BUnitModel> ctx)
	{
		return true;
	}

}
