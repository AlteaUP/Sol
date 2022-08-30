package com.solgroup.core.service.customerticketing;

import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketBusinessService;
import de.hybris.platform.ticketsystem.data.CsTicketParameter;

public interface SolGroupTicketBusinessService extends TicketBusinessService {

    @Override
    CsTicketModel createTicket(CsTicketParameter arg0);

}
