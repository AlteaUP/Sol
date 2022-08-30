package com.solgroup.core.service.customerticketing.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.b2bunit.impl.DefaultSolGroupB2BUnitService;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ticket.enums.CsTicketCategory;
import de.hybris.platform.ticket.events.model.CsCustomerEventModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.impl.DefaultTicketBusinessService;
import de.hybris.platform.ticket.strategies.TicketEventEmailStrategy;
import de.hybris.platform.ticket.strategies.TicketEventStrategy;
import de.hybris.platform.ticketsystem.data.CsTicketParameter;

public class DefaultSolGroupTicketBusinessService extends DefaultTicketBusinessService {
    //private TicketEventStrategy ticketEventStrategy;
    private DefaultSolGroupB2BUnitService solGroupB2BUnitService;
    //private TicketEventEmailStrategy ticketEventEmailStrategy;
    
    private static final Logger log = Logger.getLogger(DefaultSolGroupTicketBusinessService.class);

    @Override
    public CsTicketModel createTicket(CsTicketParameter ticketParameter) {
    	
    	UserModel currentUser = getUserService().getCurrentUser();
    	ticketParameter.setCustomer(currentUser);
    	
    	
    	B2BUnitModel b2bUnit = getB2BUnit(currentUser);
    	if(b2bUnit!=null) {
        	// Set b2bUnit for ticket
    		ticketParameter.setB2bUnit(b2bUnit);
        	
        	// Set agent for ticket
            EmployeeModel agent = getSolGroupB2BUnitService().getAgentForB2BUnit(b2bUnit);
            if(agent!=null) {
            	ticketParameter.setAssignedAgent(agent);
            }
            else {
            	log.warn(String.format("No agent found for b2bUnit [%s]", b2bUnit.getUid()));
            }
        }
        else {
        	log.warn(String.format("No b2bUnit found for customer [%s]", currentUser.getUid()));
        }
    	
    	return super.createTicket(ticketParameter);
        
    	
    	
    	/*
        CsTicketModel ticket1 = super.getTicketParameterConverter().convert(ticketParameter);
        // Set customer for ticket
        if(ticket1.getCustomer()!=null) {
        	ticket1.setCustomer(getUserService().getCurrentUser());
        }
        
        B2BUnitModel b2bUnit = getB2BUnit(ticket1.getCustomer());
        
        
        if(b2bUnit!=null) {
        	// Set b2bUnit for ticket
        	ticket1.setB2bUnit(b2bUnit);
        	
        	// Set agent for ticket
            EmployeeModel agent = getSolGroupB2BUnitService().getAgentForB2BUnit(ticket1.getB2bUnit());
            if(agent!=null) {
            	ticket1.setAssignedAgent(agent);
            }
            else {
            	log.warn(String.format("No agent found for b2bUnit [%s]", ticket1.getB2bUnit().getUid()));
            }
        }
        else {
        	log.warn(String.format("No b2bUnit found for customer [%s]", ticket1.getCustomer().getUid()));
        }
        
        
//        if (ticketParameter.getCategory().equals(CsTicketCategory.QUOTE)) {
//            ticket1.setB2bUnit(getB2BUnit(ticket1.getCustomer()));
//        }

        CsCustomerEventModel creationEvent1 = getTicketEventStrategy().createCreationEventForTicket(ticket1,
                ticketParameter.getReason(), ticketParameter.getInterventionType(), ticketParameter.getCreationNotes());

        return this.createTicketInternal(ticket1, creationEvent1);
        */
    }

    private B2BUnitModel getB2BUnit(UserModel userModel) {
        if (userModel instanceof B2BCustomerModel) {
            B2BCustomerModel b2bCustomer = (B2BCustomerModel) userModel;
            return b2bCustomer.getDefaultB2BUnit();
        }
        return null;
    }

//    @Override
//    protected CsTicketModel createTicketInternal(CsTicketModel ticket, CsCustomerEventModel creationEvent) {
//        BaseSiteModel currentBaseSite = this.getBaseSiteService().getCurrentBaseSite();
//        if (currentBaseSite != null) {
//            ticket.setBaseSite(currentBaseSite);
//        }
//
//        this.ticketEventStrategy.ensureTicketEventSetupForCreationEvent(ticket, creationEvent);
//        this.getModelService().saveAll(new Object[] { ticket, creationEvent });
//        this.getModelService().refresh(ticket);
//        getTicketEventEmailStrategy().sendEmailsForEvent(ticket, creationEvent);
//        //this.ticketEventEmailStrategy.sendEmailsForEvent(ticket, creationEvent);
//        return ticket;
//    }
//
//    protected TicketEventStrategy getTicketEventStrategy() {
//        return ticketEventStrategy;
//    }
//
//    @Override
//    @Required
//    public void setTicketEventStrategy(TicketEventStrategy ticketEventStrategy) {
//        this.ticketEventStrategy = ticketEventStrategy;
//    }

    protected DefaultSolGroupB2BUnitService getSolGroupB2BUnitService() {
        return solGroupB2BUnitService;
    }

    @Required
    public void setSolGroupB2BUnitService(DefaultSolGroupB2BUnitService solGroupB2BUnitService) {
        this.solGroupB2BUnitService = solGroupB2BUnitService;
    }

    
    
    
    

}
