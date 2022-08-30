package com.solgroup.facades.customerticketing;

import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.customerticketingfacades.TicketFacade;
import de.hybris.platform.customerticketingfacades.data.TicketData;

/**
 * @author fmilazzo
 *
 */
public interface SolGroupTicketFacade extends TicketFacade {

    @Override
    public SearchPageData<TicketData> getTickets(final PageableData pageableData);

    SearchPageData<TicketData> getQuotes(final PageableData pageableData);

    TicketData createQuoteRequest(TicketData ticketData);

    TicketData getQuote(String quoteId);
    
}
