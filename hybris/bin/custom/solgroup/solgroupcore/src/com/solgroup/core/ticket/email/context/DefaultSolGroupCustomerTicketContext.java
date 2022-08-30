package com.solgroup.core.ticket.email.context;

import org.apache.commons.lang.StringUtils;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ticket.email.context.AbstractTicketContext;
import de.hybris.platform.ticket.events.model.CsTicketEventModel;
import de.hybris.platform.ticket.model.CsTicketModel;

public class DefaultSolGroupCustomerTicketContext extends AbstractTicketContext {

	public DefaultSolGroupCustomerTicketContext(CsTicketModel ticket, CsTicketEventModel event) {
		super(ticket, event);
	}
	
	public String getName() {
		return this.getTicket().getCustomer().getName();
	}

	public String getTo() {
		
		// No user associated with ticket --> return null
		UserModel ticketUser = this.getTicket().getCustomer();
		if(ticketUser == null) {
			return null;
		}

		// User associated with ticket is a b2bCustomer --> return his email
		if(ticketUser instanceof B2BCustomerModel) {
			B2BCustomerModel ticketB2BCustomer = (B2BCustomerModel)ticketUser;
			if(StringUtils.isNotEmpty(ticketB2BCustomer.getEmail())) {
				return ticketB2BCustomer.getEmail();
			}
			else {
				return null;
			}
		}
		
		// Default case
		return this.getTicket().getCustomer() != null
				&& this.getTicket().getCustomer().getDefaultPaymentAddress() != null
						? this.getTicket().getCustomer().getDefaultPaymentAddress().getEmail()
						: null;
	}


}
