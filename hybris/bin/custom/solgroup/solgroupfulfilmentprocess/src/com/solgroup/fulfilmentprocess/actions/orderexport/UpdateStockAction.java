package com.solgroup.fulfilmentprocess.actions.orderexport;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.model.SubOrderModel;
import com.solgroup.core.model.SubOrderProcessModel;
import com.solgroup.service.SolGroupWSStockService;

import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;

public class UpdateStockAction extends AbstractSimpleDecisionAction<SubOrderProcessModel> {
	private static final Logger LOG = Logger.getLogger(UpdateStockAction.class);

	private SolGroupWSStockService solGroupWSStockService;

	@Override
	public Transition executeAction(final SubOrderProcessModel process) {
		final SubOrderModel subOrder = process.getSubOrder();

		Set<ProductModel> products = new HashSet<ProductModel>();

		subOrder.getSubOrderEntry().forEach(subOrderEntry -> products.add(subOrderEntry.getOrderEntry().getProduct()));

		if (LOG.isInfoEnabled())
			LOG.info("cardinality of products list is " + products.size());

		getSolGroupWSStockService().sendStockRequest(subOrder.getLegacySystem(),
				((CMSSiteModel)subOrder.getOrder().getSite()).getMainCountry(), products);

		return Transition.OK;
	}

	private SolGroupWSStockService getSolGroupWSStockService() {
		return solGroupWSStockService;
	}

	@Required
	public void setSolGroupWSStockService(SolGroupWSStockService solGroupWSStockService) {
		this.solGroupWSStockService = solGroupWSStockService;
	}

}
