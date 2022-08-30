package com.solgroup.core.asm.strategies;

import static de.hybris.platform.assistedserviceservices.constants.AssistedserviceservicesConstants.SORT_BY_NAME_ASC;
import static de.hybris.platform.assistedserviceservices.constants.AssistedserviceservicesConstants.SORT_BY_NAME_DESC;
import static de.hybris.platform.assistedserviceservices.constants.AssistedserviceservicesConstants.SORT_BY_UID_ASC;
import static de.hybris.platform.assistedserviceservices.constants.AssistedserviceservicesConstants.SORT_BY_UID_DESC;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.b2bunit.SolGroupB2BUnitService;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commerceservices.customer.strategies.CustomerListSearchStrategy;
import de.hybris.platform.commerceservices.search.flexiblesearch.PagedFlexibleSearchService;
import de.hybris.platform.commerceservices.search.flexiblesearch.data.SortQueryData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import jersey.repackaged.com.google.common.collect.Sets;

/**
 * @author fmilazzo
 *
 */
public class SolGroupB2BCustomerListSearchStrategy implements CustomerListSearchStrategy {

    private PagedFlexibleSearchService pagedFlexibleSearchService;
    private UserService userService;
    private SessionService sessionService;
    private SolGroupB2BUnitService solGroupB2BUnitService;

    private final String PARAM_NO_VENDOR_CODE = "NO_VENDOR_CODE";
    private final String PARAM_KEY_ID = "ID";
    private final String PARAM_KEY_B2BUNIT = "B2BUNIT";
    private final String PARAM_KEY_BRANCH = "BRANCH_SET";
    private final String PARAM_KEY_VENDOR_CODE = "VENDOR_CODE";

    @Override
    public <T extends CustomerModel> SearchPageData<T> getPagedCustomers(String customerListUid, String userId,
            PageableData pageableData, Map<String, Object> parameterMap) {

        validateParameterNotNullStandardMessage("customerListUid", customerListUid);
        validateParameterNotNullStandardMessage("pageableData", pageableData);
        validateParameterNotNullStandardMessage("userId", userId);

        String id = null;
        B2BUnitModel b2bUnit = null;
        Set<B2BUnitModel> branch = null;
        String vendorCode = null;

        UserModel loggedUserModel = getUserService().getUserForUID(userId);

        boolean isAgent = false;
        boolean isCustomer = false;

        if (loggedUserModel instanceof B2BCustomerModel) {
            b2bUnit = ((B2BCustomerModel) loggedUserModel).getDefaultB2BUnit();
            // branch = getSessionService().getAttribute(SolgroupCoreConstants.SESSION_PARAM_B2BUNITS_BRANCH_SET);
            branch = getSolGroupB2BUnitService().getBranch(b2bUnit);
            parameterMap.put(PARAM_KEY_B2BUNIT, b2bUnit);
            parameterMap.put(PARAM_KEY_BRANCH, branch);

            isCustomer = true;
        } else if (loggedUserModel instanceof EmployeeModel) {
            EmployeeModel agent = (EmployeeModel) loggedUserModel;
            vendorCode = agent.getVendorCode();
            parameterMap.put(PARAM_KEY_VENDOR_CODE, vendorCode);
            isAgent = true;
        }

        // Checks
        if (isAgent && StringUtils.isEmpty(vendorCode)) {
            vendorCode = PARAM_NO_VENDOR_CODE;
            parameterMap.put(PARAM_KEY_VENDOR_CODE, vendorCode);
        }

        if (isCustomer && CollectionUtils.isEmpty(branch)) {
            branch = Sets.newHashSet();
            branch.add(b2bUnit);
            parameterMap.put(PARAM_KEY_BRANCH, branch);
        }

        if (!parameterMap.isEmpty() && parameterMap.containsKey(PARAM_KEY_ID)
                && parameterMap.get(PARAM_KEY_ID) != null) {
            id = (String) parameterMap.get(PARAM_KEY_ID);
            // FIX SOL528
            parameterMap.put(PARAM_KEY_ID, id.toLowerCase());
        }

        final StringBuilder builder = getB2BUnitSearchQuery(id, b2bUnit, branch, vendorCode);

        final List<SortQueryData> sortQueries = Arrays.asList(
                createSortQueryData(SORT_BY_NAME_ASC, builder.toString() + " ORDER BY customerName ASC"),
                createSortQueryData(SORT_BY_NAME_DESC, builder.toString() + " ORDER BY customerName DESC"),
                createSortQueryData(SORT_BY_UID_ASC, builder.toString() + " ORDER BY b2bUnitName ASC"),
                createSortQueryData(SORT_BY_UID_DESC, builder.toString() + " ORDER BY b2bUnitName DESC"));

        return getPagedFlexibleSearchService().search(sortQueries, SORT_BY_UID_ASC, parameterMap, pageableData);
    }

    private StringBuilder getB2BUnitSearchQuery(final String id, final B2BUnitModel b2bUnit,
            final Set<B2BUnitModel> branch, final String vendorCode) {
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT ");
        // FIX SOL526
        builder.append("MIN({p:pk}) AS customerPk, MIN({b:pk}) AS b2bUnitPk, ");
        builder.append("{p:name} AS customerName, {b:name} AS b2bUnitName ");
        builder.append("FROM {B2BCustomer AS p JOIN B2BUnit AS b ON {p:defaultB2BUnit} = {b:pk}} ");
        builder.append("WHERE NOT {p:uid}='anonymous' ");
        if (!StringUtils.isBlank(id)) {
            builder.append("AND ( ");
            builder.append("LOWER({b:erpcode}) LIKE CONCAT('%', CONCAT(?ID, '%')) ");
            // FIX SOL528
            builder.append("OR LOWER({b:name}) LIKE CONCAT('%', CONCAT(?ID, '%')) ");
            builder.append("OR LOWER({p:name}) LIKE CONCAT('%', CONCAT(?ID, '%')) ");
            builder.append("OR LOWER({p:uid}) LIKE CONCAT('%', CONCAT(?ID, '%'))) ");
        }
        if (CollectionUtils.isNotEmpty(branch)) {
            builder.append("AND {b:pk} IN (?BRANCH_SET) ");
        }
        if (b2bUnit != null) {
            builder.append("AND NOT {b:pk} IN (?B2BUNIT) ");
        }
        if (StringUtils.isNotEmpty(vendorCode)) {
            builder.append("AND {b:vendorCode} = (?VENDOR_CODE) ");
        }
        // FIX SOL530
        builder.append("AND {p:active} = 1 ");
        builder.append("GROUP BY {b:pk}, {p:pk}, {p:name}, {b:name} ");
        return builder;
    }

    protected SortQueryData createSortQueryData(final String sortCode, final String query) {
        final SortQueryData result = new SortQueryData();
        result.setSortCode(sortCode);
        result.setQuery(query);
        return result;
    }

    public PagedFlexibleSearchService getPagedFlexibleSearchService() {
        return pagedFlexibleSearchService;
    }

    @Required
    public void setPagedFlexibleSearchService(PagedFlexibleSearchService pagedFlexibleSearchService) {
        this.pagedFlexibleSearchService = pagedFlexibleSearchService;
    }

    protected UserService getUserService() {
        return userService;
    }

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    protected SessionService getSessionService() {
        return sessionService;
    }

    @Required
    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    protected SolGroupB2BUnitService getSolGroupB2BUnitService() {
        return solGroupB2BUnitService;
    }

    @Required
    public void setSolGroupB2BUnitService(SolGroupB2BUnitService solGroupB2BUnitService) {
        this.solGroupB2BUnitService = solGroupB2BUnitService;
    }

}
