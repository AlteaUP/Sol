package com.solgroup.core.dao.quotes.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;
import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.dao.quotes.SolGroupQuoteDao;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commerceservices.order.dao.impl.DefaultCommerceQuoteDao;
import de.hybris.platform.commerceservices.search.flexiblesearch.data.SortQueryData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.QuoteState;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.store.BaseStoreModel;

public class DefaultSolGroupQuoteDao extends DefaultCommerceQuoteDao implements SolGroupQuoteDao {
	
	
	private SessionService sessionService;
	
	
	private static final String FIND_QUOTES_BY_CUSTOMER_STORE_CODE_MAX_VERSION_QUERY = "SELECT {q1:" + QuoteModel.PK + "} FROM {"
			+ QuoteModel._TYPECODE + " as q1} WHERE {q1:" + QuoteModel.STATE + "} IN (?quoteStates) AND {q1:" + QuoteModel.UNIT
			+ "} IN (?" + QuoteModel.UNIT + ") AND {q1:" + QuoteModel.STORE + "} = ?" + QuoteModel.STORE + " AND {q1:"
			+ QuoteModel.VERSION + "} = ({{ SELECT MAX({" + QuoteModel.VERSION + "}) FROM {" + QuoteModel._TYPECODE + "} WHERE {"
			+ QuoteModel.CODE + "} = {q1:" + QuoteModel.CODE + "} AND {" + QuoteModel.STATE + "} IN (?quoteStates) AND {" + QuoteModel.STORE + "} = ?" + QuoteModel.STORE + "}}) ";


	private static final String FIND_QUOTE_BY_CUSTOMER_STORE_CODE_MAX_VERSION_QUERY = "SELECT {q1:" + QuoteModel.PK + "} FROM {"
			+ QuoteModel._TYPECODE + " as q1} WHERE {q1:" + QuoteModel.STATE + "} IN (?quoteStates) AND {q1:" + QuoteModel.UNIT
			+ "} IN (?" + QuoteModel.UNIT + ") AND {q1:" + QuoteModel.STORE + "} = ?" + QuoteModel.STORE + " AND {q1:" + QuoteModel.CODE
			+ "} = ?" + QuoteModel.CODE + "  ORDER BY {q1:" + QuoteModel.VERSION + "} DESC";
	
	private static final String QUOTES_TOTAL_FOR_USER_AND_STORE = "SELECT COUNT(DISTINCT {q1:" + QuoteModel.CODE + "}) FROM {"
			+ QuoteModel._TYPECODE + " as q1} WHERE {q1:" + QuoteModel.UNIT + "} IN (?" + QuoteModel.UNIT + ") AND {q1:"
			+ QuoteModel.STORE + "} = ?" + QuoteModel.STORE + " AND {q1:" + QuoteModel.STATE + "} IN (?quoteStates)";


	private static final String ORDER_BY_QUOTE_CODE_DESC = " ORDER BY {q1:" + QuoteModel.CODE + "} DESC";
	private static final String ORDER_BY_QUOTE_NAME_DESC = " ORDER BY {q1:" + QuoteModel.NAME + "} DESC";
	private static final String ORDER_BY_QUOTE_DATE_DESC = " ORDER BY {q1:" + QuoteModel.MODIFIEDTIME + "} DESC";
	private static final String ORDER_BY_QUOTE_STATE = " ORDER BY {q1:" + QuoteModel.STATE + "} DESC";
	
	
	@Override
	public SearchPageData<QuoteModel> findQuotesByCustomerAndStore(final CustomerModel customerModel, final BaseStoreModel store,
			final PageableData pageableData, final Set<QuoteState> quoteStates)
	{
		validateUserAndStoreAndStates(store, customerModel, quoteStates);

		final Map<String, Object> queryParams = populateBasicQueryParams(store, customerModel, quoteStates);
		final List<SortQueryData> sortQueries;
		sortQueries = Arrays.asList(
				createSortQueryData("byDate",
						createQuery(FIND_QUOTES_BY_CUSTOMER_STORE_CODE_MAX_VERSION_QUERY, ORDER_BY_QUOTE_DATE_DESC)),
				createSortQueryData("byCode",
						createQuery(FIND_QUOTES_BY_CUSTOMER_STORE_CODE_MAX_VERSION_QUERY, ORDER_BY_QUOTE_CODE_DESC)),
				createSortQueryData("byName",
						createQuery(FIND_QUOTES_BY_CUSTOMER_STORE_CODE_MAX_VERSION_QUERY, ORDER_BY_QUOTE_NAME_DESC)),
				createSortQueryData("byState",
						createQuery(FIND_QUOTES_BY_CUSTOMER_STORE_CODE_MAX_VERSION_QUERY, ORDER_BY_QUOTE_STATE)));

		return getPagedFlexibleSearchService().search(sortQueries, "byCode", queryParams, pageableData);
	}

	@Override
	public QuoteModel findUniqueQuoteByCodeAndCustomerAndStore(final CustomerModel customerModel, final BaseStoreModel store,
			final String quoteCode, final Set<QuoteState> quoteStates)
	{
		validateParameterNotNull(quoteCode, "Quote Code cannot be null");
		validateUserAndStoreAndStates(store, customerModel, quoteStates);

		final Map<String, Object> queryParams = populateBasicQueryParams(store, customerModel, quoteStates);
		queryParams.put(QuoteModel.CODE, quoteCode);

		final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(
				FIND_QUOTE_BY_CUSTOMER_STORE_CODE_MAX_VERSION_QUERY);
		flexibleSearchQuery.getQueryParameters().putAll(queryParams);
		flexibleSearchQuery.setCount(1);

		return getFlexibleSearchService().searchUnique(flexibleSearchQuery);
	}
	
	@Override
	public Integer getQuotesCountForCustomerAndStore(final CustomerModel customerModel, final BaseStoreModel store,
			final Set<QuoteState> quoteStates)
	{
		validateUserAndStoreAndStates(store, customerModel, quoteStates);

		final Map<String, Object> queryParams = populateBasicQueryParams(store, customerModel, quoteStates);

		final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(QUOTES_TOTAL_FOR_USER_AND_STORE);
		flexibleSearchQuery.addQueryParameters(queryParams);
		flexibleSearchQuery.setResultClassList(Collections.singletonList(Integer.class));

		final SearchResult<Integer> searchResult = getFlexibleSearchService().search(flexibleSearchQuery);

		return searchResult.getResult().get(0);
	}

	
	
	
	protected void validateUserAndStoreAndStates(final BaseStoreModel store, final CustomerModel customerModel,
			final Set<QuoteState> quoteStates)
	{
		validateParameterNotNull(customerModel, "Customer must not be null");
		validateParameterNotNull(store, "Store must not be null");
		validateQuoteStateList(quoteStates, "Quote states cannot be null or empty");
		Preconditions.checkArgument(customerModel instanceof B2BCustomerModel, "Customer must be an instance of B2BCustomer");
		B2BCustomerModel b2bCustomerModel = (B2BCustomerModel)customerModel;
		validateParameterNotNull(b2bCustomerModel.getDefaultB2BUnit(), "Customer B2bUnit can not be null");
	}
	
	
	protected Map<String, Object> populateBasicQueryParams(final BaseStoreModel store, final CustomerModel customerModel,
			final Set<QuoteState> quoteStates)
	{

		
		final Map<String, Object> queryParams = new HashMap<>();
		queryParams.put(QuoteModel.UNIT, getSessionService().getAttribute(SolgroupCoreConstants.SESSION_PARAM_B2BUNITS_BRANCH_SET));
		queryParams.put(QuoteModel.STORE, store);
		queryParams.put("quoteStates", quoteStates);
		return queryParams;
	}




	protected SessionService getSessionService() {
		return sessionService;
	}


	@Required
	public void setSessionService(SessionService sessionService) {
		this.sessionService = sessionService;
	}


	
	
	
	
	


}
