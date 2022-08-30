package com.solgroup.core.service.customerticketing;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketService;

/**
 * @author fmilazzo
 *
 */
public interface SolGroupTicketService extends TicketService {

    @Override
    SearchPageData<CsTicketModel> getTicketsForCustomerOrderByModifiedTime(UserModel arg0, BaseSiteModel arg1,
            PageableData arg2);

    SearchPageData<CsTicketModel> getQuotesForB2BUnitOrderByModifiedTime(UserModel user, BaseSiteModel baseSite,
            PageableData pageableData);

}
