package com.solgroup.core.dao.customerticketing;

import java.util.Set;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ticket.dao.TicketDao;
import de.hybris.platform.ticket.model.CsTicketModel;

/**
 * @author fmilazzo
 *
 */
public interface SolGroupTicketDao extends TicketDao {
    
    @Override
    SearchPageData<CsTicketModel> findTicketsByCustomerOrderByModifiedTime(UserModel arg0, BaseSiteModel arg1,
            PageableData arg2);

    SearchPageData<CsTicketModel> findQuotesByB2BUnitOrderByModifiedTime(UserModel user, BaseSiteModel baseSite,
            PageableData pageableData, Set<B2BUnitModel> b2bUnitBranch);

}
