package com.solgroup.core.dao.consignment;

import java.util.List;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;


public interface SolGroupConsignmentDao {

    ConsignmentModel findConsignmentByCode(final String orderCode, final String consignmentCode,
            final BaseSiteModel baseSite);
	
	List<ConsignmentEntryModel> findConsignmentEntriesByProductCode(ConsignmentModel consignment, String productCode);

}
