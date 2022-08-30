package com.solgroup.core.dao.customerticketing.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.solgroup.core.dao.customerticketing.SolGroupTicketDao;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.search.flexiblesearch.data.SortQueryData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.ticket.dao.impl.DefaultTicketDao;
import de.hybris.platform.ticket.enums.CsTicketCategory;
import de.hybris.platform.ticket.model.CsTicketModel;

/**
 * @author fmilazzo
 *
 */
public class DefaultSolGroupTicketDao extends DefaultTicketDao implements SolGroupTicketDao {

    @Override
    public SearchPageData<CsTicketModel> findTicketsByCustomerOrderByModifiedTime(UserModel user,
            BaseSiteModel baseSite, PageableData pageableData) {
        ServicesUtil.validateParameterNotNull(user, "Customer must not be null");
        ServicesUtil.validateParameterNotNull(baseSite, "Store must not be null");
        HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("user", user);
        queryParams.put("baseSite", baseSite);
        queryParams.put("ticketCategory", CsTicketCategory.SUPPORT.getCode());
        List<SortQueryData> sortQueries = Arrays.asList(new SortQueryData[] {
                super.createSortQueryData("byDate",
                        "SELECT {pk} FROM {CsTicket as ct JOIN CsTicketCategory as tc ON {ct.category} = {tc.PK}} WHERE {ct.customer} = ?user AND {ct.baseSite} = ?baseSite AND {tc.code} = ?ticketCategory ORDER BY {ct.modifiedtime} DESC"),
                super.createSortQueryData("byTicketId",
                        "SELECT {pk} FROM {CsTicket as ct JOIN CsTicketCategory as tc ON {ct.category} = {tc.PK}} WHERE {ct.customer} = ?user AND {ct.baseSite} = ?baseSite AND {tc.code} = ?ticketCategory ORDER BY {ct.ticketID} DESC") });
        return super.getPagedFlexibleSearchService().search(sortQueries, "byDate", queryParams, pageableData);
    }

    @Override
    public SearchPageData<CsTicketModel> findQuotesByB2BUnitOrderByModifiedTime(UserModel user, BaseSiteModel baseSite,
            PageableData pageableData, Set<B2BUnitModel> b2bUnitBranch) {
        ServicesUtil.validateParameterNotNull(user, "User must not be null");
        ServicesUtil.validateParameterNotNull(baseSite, "Store must not be null");
        ServicesUtil.validateParameterNotNull(b2bUnitBranch, "b2bUnitBranch must not be null");
        HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("baseSite", baseSite);
        queryParams.put("b2bUnitBrach", b2bUnitBranch);
        queryParams.put("ticketCategory", CsTicketCategory.QUOTE.getCode());
        List<SortQueryData> sortQueries = Arrays.asList(new SortQueryData[] {
                super.createSortQueryData("byDate",
                        "SELECT {pk} FROM {CsTicket as ct JOIN CsTicketCategory as tc ON {ct.category} = {tc.PK}} WHERE {ct.b2bUnit} IN (?b2bUnitBrach) AND {ct.baseSite} = ?baseSite AND {tc.code} = ?ticketCategory ORDER BY {ct.modifiedtime} DESC"),
                super.createSortQueryData("byTicketId",
                        "SELECT {pk} FROM {CsTicket as ct JOIN CsTicketCategory as tc ON {ct.category} = {tc.PK}} WHERE {ct.b2bUnit} IN (?b2bUnitBrach) AND {ct.baseSite} = ?baseSite AND {tc.code} = ?ticketCategory ORDER BY {ct.ticketID} DESC") });
        return super.getPagedFlexibleSearchService().search(sortQueries, "byDate", queryParams, pageableData);
    }

}