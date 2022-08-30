package com.solgroup.customersupportbackoffice.widgets.sessioncontext;

import com.hybris.cockpitng.annotations.SocketEvent;

import de.hybris.platform.customersupportbackoffice.widgets.sessioncontext.SessionContextController;
import de.hybris.platform.customersupportbackoffice.widgets.sessioncontext.model.SessionContextModel;
import de.hybris.platform.customersupportbackoffice.widgets.sessioncontext.util.SessionContextUtil;

public class SolGroupSessionContextController extends SessionContextController {

	
	
	@SocketEvent(socketId="selectedItem")
	public void itemSelected(Object msg) {
		super.itemSelected(msg);
	    stopWatch.stop();
	    sessionTimerDiv.setVisible(false);
	}
	

	 protected void handleUIComponents()
	  {
	    SessionContextModel currentSessionContext = SessionContextUtil.getCurrentSessionContext(getWidgetInstanceManager()
	      .getModel());
	    
	    endSessionBtn.setVisible(false);
	    callContextBtn.setVisible(false);
	    customerPlaceholder.setVisible((currentSessionContext == null) || (currentSessionContext.getCurrentCustomer() == null));
	    ticketPlaceholder.setVisible((currentSessionContext == null) || (currentSessionContext.getCurrentTicket() == null));
	    orderPlaceholder.setVisible((currentSessionContext == null) || (currentSessionContext.getCurrentOrder() == null));
	    sessionTimerDiv.setVisible(false);
	  }
	  


}
