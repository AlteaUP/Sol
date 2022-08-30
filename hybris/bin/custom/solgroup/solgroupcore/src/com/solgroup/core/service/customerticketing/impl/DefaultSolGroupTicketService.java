package com.solgroup.core.service.customerticketing.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.dao.customerticketing.SolGroupTicketDao;
import com.solgroup.core.service.b2bunit.impl.DefaultSolGroupB2BUnitService;
import com.solgroup.core.service.customerticketing.SolGroupTicketService;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.impl.DefaultTicketService;

/**
 * @author fmilazzo
 *
 */
public class DefaultSolGroupTicketService extends DefaultTicketService implements SolGroupTicketService {
    private SolGroupTicketDao solGroupTicketDao;
    private DefaultSolGroupB2BUnitService solGroupB2BUnitService;

    @Override
    public SearchPageData<CsTicketModel> getTicketsForCustomerOrderByModifiedTime(UserModel user,
            BaseSiteModel baseSite, PageableData pageableData) {
        return this.solGroupTicketDao.findTicketsByCustomerOrderByModifiedTime(user, baseSite, pageableData);
    }

    @Override
    public SearchPageData<CsTicketModel> getQuotesForB2BUnitOrderByModifiedTime(UserModel user, BaseSiteModel baseSite,
            PageableData pageableData) {

        Set<B2BUnitModel> b2bUnitBranch = super.getSessionService()
                .getAttribute(SolgroupCoreConstants.SESSION_PARAM_B2BUNITS_BRANCH_SET);

        return this.solGroupTicketDao.findQuotesByB2BUnitOrderByModifiedTime(user, baseSite, pageableData,
                b2bUnitBranch);
    }

    public SolGroupTicketDao getSolGroupTicketDao() {
        return solGroupTicketDao;
    }

    @Required
    public void setSolGroupTicketDao(SolGroupTicketDao solGroupTicketDao) {
        this.solGroupTicketDao = solGroupTicketDao;
    }

    public DefaultSolGroupB2BUnitService getSolGroupB2BUnitService() {
        return solGroupB2BUnitService;
    }

    @Required
    public void setSolGroupB2BUnitService(DefaultSolGroupB2BUnitService solGroupB2BUnitService) {
        this.solGroupB2BUnitService = solGroupB2BUnitService;
    }

}
