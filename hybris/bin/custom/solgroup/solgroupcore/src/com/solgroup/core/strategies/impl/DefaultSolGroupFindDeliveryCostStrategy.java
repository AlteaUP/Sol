/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.solgroup.core.strategies.impl;

import com.solgroup.storefront.util.ProductDeliveryCost;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeModel;
import de.hybris.platform.deliveryzone.model.ZoneDeliveryModeValueModel;
import de.hybris.platform.order.strategies.calculation.FindDeliveryCostStrategy;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.internal.service.AbstractBusinessService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.util.PriceValue;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * Default implementation of {@link FindDeliveryCostStrategy}.
 */
public class DefaultSolGroupFindDeliveryCostStrategy extends AbstractBusinessService implements FindDeliveryCostStrategy
{

	private static final Logger LOG = Logger.getLogger(DefaultSolGroupFindDeliveryCostStrategy.class);

	@Resource(name = "productService")
	private ProductService productService;

	//step 1 : delegate to jalo
	@Override
	public PriceValue getDeliveryCost(final AbstractOrderModel order)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("order", order);

		if(order.getPaymentInfo() !=null && order.getPaymentInfo().getRequireDeliveryCostCalculation()!=null &&
			order.getPaymentInfo().getRequireDeliveryCostCalculation()){

			List<ProductDeliveryCost> realDeliveryCosts = new ArrayList<ProductDeliveryCost>();

			Double realDeliveryCost = new Double(0);

			for(AbstractOrderEntryModel entry : order.getEntries()){
				String code = entry.getProduct().getCode();
				ProductModel product = productService.getProductForCode(code);
				List<ZoneDeliveryModeModel> deliveryModes = product.getProductDeliveryMode();
				if(deliveryModes != null && !deliveryModes.isEmpty() && product.getMaterial().getCode().equalsIgnoreCase("mat")){
					ZoneDeliveryModeModel deliveryMode = deliveryModes.get(0);
					ZoneDeliveryModeValueModel value = deliveryMode.getValues().iterator().next();
					Double unitCost = value.getValue();
					Double weight = product.getWeight();
					Long qty = entry.getQuantity();

					Double totalWeight = qty * weight;

					Double totalCost = totalWeight * unitCost;

					ProductDeliveryCost cost = new ProductDeliveryCost();
					cost.setCost(totalCost);
					cost.setDeliveryModeDescription(deliveryMode.getDescription());
					cost.setProduct(product.getCode());

					realDeliveryCosts.add(cost);

					realDeliveryCost = realDeliveryCost + cost.getCost();
				}
			}

			LOG.info("Real delivery cost: " + realDeliveryCost);
			//TODO remove default delivery cost
			return new PriceValue(order.getCurrency().getIsocode(), 10.0, order.getNet().booleanValue());

		}else{

			LOG.info("Order [" + order.getCode() + "] has no delivery cost");
			return new PriceValue(order.getCurrency().getIsocode(), 0.0, order.getNet().booleanValue());

		}

	}

}
