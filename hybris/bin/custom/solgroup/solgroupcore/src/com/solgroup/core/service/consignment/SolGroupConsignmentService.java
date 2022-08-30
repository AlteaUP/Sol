package com.solgroup.core.service.consignment;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;

public interface SolGroupConsignmentService {

	ConsignmentModel getConsignmentForCode(final String consignmentCode, final String orderCode);

	ConsignmentEntryModel getConsignmentEntryByProductCode(ConsignmentModel consignment,String productCode);
	
	boolean isFinalStatus(String consignmentStatus);

    ConsignmentModel getConsignmentForCode(final String consignmentCode, final String orderCode, final BaseSiteModel baseSite);
	
}
