package com.solgroup.core.ticket.email.context;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ticket.email.context.AbstractTicketContext;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.model.CsTicketModel;

public class DefaultSolGroupAgentTicketContext extends AbstractTicketContext {

	public DefaultSolGroupAgentTicketContext(CsTicketModel ticket, CsTicketEventModel event) {
		super(ticket, event);
	}
	
	

	public String getName() {
		return this.getTicket().getAssignedAgent().getName();
	}

	public String getTo() {

		String emailAddress = "";
		
		EmployeeModel agent = this.getTicket().getAssignedAgent();
		Collection<AddressModel> addresses = agent.getAddresses();
		if(CollectionUtils.isNotEmpty(addresses)) {
			for(AddressModel address : addresses) {
				if(BooleanUtils.isTrue(address.getContactAddress()) && StringUtils.isNotEmpty(address.getEmail())) {
					emailAddress = address.getEmail();
					break;
				}
			}
		}
		
		if(StringUtils.isNotEmpty(emailAddress)) {
			return emailAddress;
		}
		else {
			return null;
		}
	}
	



}
