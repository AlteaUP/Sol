package com.solgroup.core.dao.consignment.impl;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.solgroup.core.dao.consignment.SolGroupConsignmentDao;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;

public class DefaultSolGroupConsignmentDao extends AbstractItemDao implements SolGroupConsignmentDao {
 	private static final String CONSIGNMENT_CODE = "consignmentCode";
    private static final String ORDER_CODE = "orderCode";
    private static final String BASE_SITE = "baseSite";

    @Override
    public ConsignmentModel findConsignmentByCode(final String orderCode, final String consignmentCode,
            final BaseSiteModel baseSite) {
        final FlexibleSearchQuery query = new FlexibleSearchQuery(FSQ);
        query.addQueryParameter(CONSIGNMENT_CODE, consignmentCode);
        query.addQueryParameter(ORDER_CODE, orderCode);
        query.addQueryParameter(BASE_SITE, baseSite);
        return (ConsignmentModel) getFlexibleSearchService().searchUnique(query);
    }


	@Override
	public List<ConsignmentEntryModel> findConsignmentEntriesByProductCode(ConsignmentModel consignment,
			String productCode) {
		
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select {ce." + ItemModel.PK + "} from ");
		queryBuilder.append("{" + ConsignmentEntryModel._TYPECODE + " as ce ");
		queryBuilder.append("join " + OrderEntryModel._TYPECODE + " as oe on {oe." + ItemModel.PK + "}={ce." + ConsignmentEntryModel.ORDERENTRY+"} ");
		queryBuilder.append("join " + ProductModel._TYPECODE + " as p on {p." + ItemModel.PK + "}={oe." + OrderEntryModel.PRODUCT + "}}");
		queryBuilder.append(" where ");
		queryBuilder.append("{ce." + ConsignmentEntryModel.CONSIGNMENT + "}=?consignment");
		queryBuilder.append(" AND {p." + ProductModel.CODE + "}=?productCode");
		
		Map<String,Object> params = Maps.newHashMap();
		params.put("consignment", consignment);
		params.put("productCode", productCode);
		
		final List<ConsignmentEntryModel> result = getFlexibleSearchService()
				.<ConsignmentEntryModel> search(queryBuilder.toString(), params).getResult();
		
		return result;
	}
	
	
    private static final String FSQ = "select {" + ConsignmentModel.PK + "} from {" + ConsignmentModel._TYPECODE
            + " join " + OrderModel._TYPECODE + " on {" + ConsignmentModel._TYPECODE + ":order} = {"
            + OrderModel._TYPECODE + ":pk} join " + BaseSiteModel._TYPECODE + " on {" + OrderModel._TYPECODE
            + ":site} = {" + BaseSiteModel._TYPECODE + ":pk}} where {" + ConsignmentModel._TYPECODE
            + ":code} = ?consignmentCode and {" + OrderModel._TYPECODE + ":code} = ?orderCode and {"
            + OrderModel._TYPECODE + ":site} = ?baseSite";
	

}
