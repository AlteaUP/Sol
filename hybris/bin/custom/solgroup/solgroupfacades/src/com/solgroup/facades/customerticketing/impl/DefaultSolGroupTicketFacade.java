/**
 *
 */
package com.solgroup.facades.customerticketing.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.service.customerticketing.impl.DefaultSolGroupTicketBusinessService;
import com.solgroup.core.service.customerticketing.impl.DefaultSolGroupTicketService;
import com.solgroup.facades.customerticketing.SolGroupTicketFacade;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.customerticketingfacades.customerticket.DefaultCustomerTicketingFacade;
import de.hybris.platform.customerticketingfacades.data.TicketData;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticketsystem.data.CsTicketParameter;

/**
 * @author fmilazzo
 *
 */
public class DefaultSolGroupTicketFacade extends DefaultCustomerTicketingFacade implements SolGroupTicketFacade
{
    private DefaultSolGroupTicketService solGroupTicketService;
    private DefaultSolGroupTicketBusinessService solGroupTicketBusinessService;
    private SessionService sessionService;

    @Override
    public SearchPageData<TicketData> getTickets(final PageableData pageableData) {
        final SearchPageData<CsTicketModel> searchPageData = getSolGroupTicketService()
                .getTicketsForCustomerOrderByModifiedTime(getUserService().getCurrentUser(),
                        getBaseSiteService().getCurrentBaseSite(), pageableData);
        return convertPageData(searchPageData, getTicketListConverter());
    }

    @Override
    public SearchPageData<TicketData> getQuotes(final PageableData pageableData) {
        final SearchPageData<CsTicketModel> searchPageData = getSolGroupTicketService()
                .getQuotesForB2BUnitOrderByModifiedTime(getUserService().getCurrentUser(),
                        getBaseSiteService().getCurrentBaseSite(), pageableData);
        return convertPageData(searchPageData, getTicketListConverter());
    }

    @Override
    public TicketData createQuoteRequest(final TicketData ticketData) {
        final CsTicketModel ticket;
        final CsTicketParameter ticketParameter = createCsTicketParameter(ticketData);
        ticket = getSolGroupTicketBusinessService().createTicket(ticketParameter);
        ticketData.setId(ticket.getTicketID());
        return ticketData;
    }

    @Override
    public TicketData getQuote(final String quoteId) {
        final CsTicketModel ticketModel = getTicketService().getTicketForTicketId(quoteId);
        if (ticketModel == null) {
            throw new RuntimeException("Quotation not found for the given ID " + quoteId); // NOSONAR
        }

        Set<B2BUnitModel> branchSet = getSessionService().getAttribute(SolgroupCoreConstants.SESSION_PARAM_B2BUNITS_BRANCH_SET);
        
        for(B2BUnitModel b2BUnitModel : branchSet) {
            if (b2BUnitModel.equals(ticketModel.getB2bUnit())) {
                return getTicketConverter().convert(ticketModel, new TicketData());
            }
        }
        return null;
    }

    public DefaultSolGroupTicketService getSolGroupTicketService() {
        return solGroupTicketService;
    }

    @Required
    public void setSolGroupTicketService(DefaultSolGroupTicketService solGroupTicketService) {
        this.solGroupTicketService = solGroupTicketService;
    }

    public DefaultSolGroupTicketBusinessService getSolGroupTicketBusinessService() {
        return solGroupTicketBusinessService;
    }

    @Required
    public void setSolGroupTicketBusinessService(DefaultSolGroupTicketBusinessService solGroupTicketBusinessService) {
        this.solGroupTicketBusinessService = solGroupTicketBusinessService;
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    @Required
    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

}
