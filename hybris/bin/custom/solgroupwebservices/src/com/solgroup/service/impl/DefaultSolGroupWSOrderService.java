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
package com.solgroup.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.constants.SolgroupwebservicesConstants;
import com.solgroup.core.enums.ActionToLegacyEnum;
import com.solgroup.core.enums.SubOrderStatus;
import com.solgroup.core.model.SubOrderEntryModel;
import com.solgroup.core.model.SubOrderModel;
import com.solgroup.core.service.address.SolGroupAddressService;
import com.solgroup.core.ws.services.client.sol2po.OrdersExportOB;
import com.solgroup.core.ws.services.client.sol2po.OrdersInsert;
import com.solgroup.core.ws.services.client.sol2po.OrdersInsert.Header;
import com.solgroup.core.ws.services.client.sol2po.OrdersInsert.Order;
import com.solgroup.core.ws.services.client.sol2po.OrdersInsert.Order.EntryList.Entry;
import com.solgroup.core.ws.services.client.sol2po.OrdersInsert.Order.ShippingAddress;
import com.solgroup.service.SolGroupWSOrderService;
import com.solgroup.service.utils.SolgroupWebServiceUtils;


import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.price.DiscountModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.DiscountValue;

public class DefaultSolGroupWSOrderService implements SolGroupWSOrderService {
	private static final Logger LOG = Logger.getLogger(DefaultSolGroupWSOrderService.class);

	private ModelService modelService;
	private ConfigurationService configurationService;
	private BusinessProcessService businessProcessService;
	private SolGroupAddressService solGroupAddressService;

	@Override
	public void sendOrderInsert(SubOrderModel subOrder) throws Exception {

		// Set Ws Model
		OrdersInsert.Order subOrder2send = setParamOrder(subOrder);
		String WsUrl = getConfigurationService().getConfiguration().getString(SolgroupwebservicesConstants.PROPERTY_NAME_PO_EXPORT_ORDER_SERVICE_URL);
		OrdersExportOB orderExportClient = (OrdersExportOB) SolgroupWebServiceUtils.createSoapClient(WsUrl, OrdersExportOB.class);
		Client client = ClientProxy.getClient(orderExportClient);
		SolgroupWebServiceUtils.configureClient(client);

		SolgroupWebServiceUtils.logXmlData(subOrder2send, LOG);

		Header header = new Header();
		header.setTargetSystem(subOrder.getLegacySystem().getCode());
		// execute request
		LOG.info("Send SubOrder to Legacy - " + subOrder.getLegacySystem().getCode());
		orderExportClient.ordersInsert(header, subOrder2send);

	}

	private XMLGregorianCalendar dateToXMLGregorianCalendar(Date date) throws Exception {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(date);
		XMLGregorianCalendar date2xml = null;

		try {
			date2xml = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
			LOG.error("Error in date convertion dateToXMLGregorianCalendar :: " + e.getMessage());
			throw new Exception("Error in date convertion!");
		}

		return date2xml;
	}



	private Order setParamOrder(SubOrderModel subOrderSource) throws Exception {
		OrdersInsert wPk = new OrdersInsert();
		int offsetDays = Integer.parseInt(getConfigurationService().getConfiguration().getString(SolgroupwebservicesConstants.PROPERTY_NAME_DELIVERY_OFFSET_DATE));
		// Set Order param
		OrdersInsert.Order order = new OrdersInsert.Order();

		order.setCode(subOrderSource.getOrder().getCode());
		order.setCurrency(subOrderSource.getOrder().getCurrency().getIsocode());
		order.setB2BUnithybrisCode(subOrderSource.getOrder().getUnit().getErpCode());
		order.setB2BUnitLegacyCode(subOrderSource.getOrder().getUnit().getErpCode());
		order.setCompanyLegacyCode(subOrderSource.getOrder().getUnit().getLegalEntity().getErpCode());
		// TODO remove it?
		order.setDeliveryCost(0);
		order.setNotes(subOrderSource.getOrder().getNote());
		order.setCreationDate(dateToXMLGregorianCalendar(subOrderSource.getOrder().getCreationtime()));

		// Set Shipping Address Object
		ShippingAddress shippingAddress = new ShippingAddress();
		if (subOrderSource.getOrder().getDeliveryAddress() != null) {
			shippingAddress.setErpCode(Objects.toString(getSolGroupAddressService().retrieveErpCode(subOrderSource.getOrder().getDeliveryAddress()), ""));
			shippingAddress.setHybrisCode(subOrderSource.getOrder().getDeliveryAddress().getHybrisCode());
			shippingAddress
					.setStreet(Objects.toString(subOrderSource.getOrder().getDeliveryAddress().getStreetname(), ""));
			shippingAddress.setStreetNumber(
					Objects.toString(subOrderSource.getOrder().getDeliveryAddress().getStreetnumber(), ""));
			shippingAddress.setTown(Objects.toString(subOrderSource.getOrder().getDeliveryAddress().getTown(), ""));
			shippingAddress.setPostalCode(
					Objects.toString(subOrderSource.getOrder().getDeliveryAddress().getPostalcode(), ""));
			shippingAddress.setCountry(
					Objects.toString(subOrderSource.getOrder().getDeliveryAddress().getCountry().getIsocode(), ""));
			shippingAddress.setEmail(Objects.toString(subOrderSource.getOrder().getDeliveryAddress().getEmail(), ""));
			shippingAddress.setPhone(Objects.toString(subOrderSource.getOrder().getDeliveryAddress().getPhone1(), ""));
			shippingAddress.setFax(Objects.toString(subOrderSource.getOrder().getDeliveryAddress().getFax(), ""));
		}
		order.setShippingAddress(shippingAddress);
		wPk.setOrder(order);

		final List<SubOrderEntryModel> entryModels = (List<SubOrderEntryModel>) subOrderSource.getSubOrderEntry();

		OrdersInsert.Order.EntryList entryList = new OrdersInsert.Order.EntryList();

		Double percentageDiscount = null;
		if(CollectionUtils.isNotEmpty(subOrderSource.getOrder().getGlobalDiscountValues())) {
			for(DiscountValue discount : subOrderSource.getOrder().getGlobalDiscountValues()) {
				if(!discount.isAbsolute()) {
					percentageDiscount = discount.getValue();
					break;
				}
			}
		}
		
		for (final SubOrderEntryModel subOrderEntryModel : entryModels) {
			Entry entry = new Entry();
			entry.setProductCode(subOrderEntryModel.getOrderEntry().getProduct().getErpCode());
			entry.setOrderEntryNumber(subOrderEntryModel.getOrderEntryNumber());
			entry.setQuantity(subOrderEntryModel.getOrderEntry().getQuantity().intValue());
			entry.setBasePrice(subOrderEntryModel.getOrderEntry().getBasePrice());
			if(percentageDiscount!=null) {
				entry.setPercentageDiscount(percentageDiscount.doubleValue());
				double discount = subOrderEntryModel.getOrderEntry().getTotalPrice() * percentageDiscount.doubleValue() / 100;
				BigDecimal discountedTotalEntryValue = BigDecimal.valueOf(subOrderEntryModel.getOrderEntry().getTotalPrice() - discount).setScale(subOrderSource.getOrder().getCurrency().getDigits().intValue(), RoundingMode.HALF_EVEN);
				entry.setTotalPrice(discountedTotalEntryValue.doubleValue());
			}
			else {
				entry.setTotalPrice(subOrderEntryModel.getOrderEntry().getTotalPrice());
			}
			
			
			
			entry.setPurchaseOrderNumber(subOrderEntryModel.getOrderEntry().getPurchaseOrderNumber());
			entry.setCig(subOrderEntryModel.getOrderEntry().getCgi());
			entry.setCup(subOrderEntryModel.getOrderEntry().getCup());
			if( subOrderEntryModel.getOrderEntry().getDataOrdine()!=null ) {
				entry.setPurchaseOrderDate(dateToXMLGregorianCalendar(subOrderEntryModel.getOrderEntry().getDataOrdine()));
			}

			if (subOrderEntryModel.getOrderEntry().getProduct().getRefill()) {
				subOrderEntryModel.getOrderEntry().setNamedDeliveryDate(LocalDate.now().plusDays(offsetDays).toDate());
				modelService.save(subOrderEntryModel.getOrderEntry());
				entry.setRequestedDeliveryDate(dateToXMLGregorianCalendar(subOrderEntryModel.getOrderEntry().getNamedDeliveryDate()));
			}



			entry.setRowAction("0");

			// TODO fix PercentageDiscount logic
			if (subOrderEntryModel.getOrderEntry().getDiscountValues().size() > 0
					&& subOrderEntryModel.getOrderEntry().getDiscountValues().get(0).getCurrencyIsoCode().isEmpty()) {

				entry.setPercentageDiscount(subOrderEntryModel.getOrderEntry().getDiscountValues().get(0).getValue());
			}

			entryList.getEntry().add(entry);
		}

		order.setEntryList(entryList);

		return order;

	}

	@Override
	public void checkSubOrderConfirmAction(SubOrderModel subOrder, com.solgroup.core.ws.services.orderIntegrationLegacy.xml.Order wsOrder) throws Exception {
		
		if(subOrder.getActionToLegacy().equals(ActionToLegacyEnum.SENT_CREATION_TO_LEGACY)) {
			
			if(wsOrder.getStatus().equals(SolgroupwebservicesConstants.CONFIRM_ACTION_OK)) {
				setSubOrderActionToLegacy(subOrder, ActionToLegacyEnum.CREATED_TO_LEGACY);
				setSubOrderStatus(subOrder, SubOrderStatus.OK, "");
				
				final String orderProcessCode = wsOrder.getCode() + "_" + subOrder.getLegacySystem().getCode() + "_" + SolgroupwebservicesConstants.SUBORDER_EXPORT_PROCESS;
				// wakeup process
				final String eventName = orderProcessCode + "_orderUpdate";
				LOG.info("Raise event " + eventName);
				getBusinessProcessService().triggerEvent(eventName);

			}
			else {
				setSubOrderStatus(subOrder, SubOrderStatus.ERROR, wsOrder.getErrorDescription());
			}
		
		}
		
		else {
			LOG.error("subOrder " + subOrder.getLegacySystem().getCode() + "_" + subOrder.getOrder().getCode() + " is not in " + ActionToLegacyEnum.SENT_CREATION_TO_LEGACY.getCode() + ". It can not receive a confirmation message");
		}
	}
	
	@Override
	public void setSubOrderStatus(SubOrderModel subOrder, SubOrderStatus subOrderStatus, String errorDescription) {
		subOrder.setStatus(subOrderStatus);
		LOG.info(String.format("Set subOrder status : legacySystem [%s], order [%s], new status [%s]", subOrder.getLegacySystem().getCode(), subOrder.getOrder().getCode(), subOrderStatus.getCode()));
		if(StringUtils.isNotEmpty(errorDescription)) {
			subOrder.setErrorDescription(errorDescription);
		}

//		// set status for parent order
//		OrderModel order = subOrder.getOrder();
//		if(subOrderStatus.equals(SubOrderStatus.OK)) {
//			order.setStatus(OrderStatus.IN_PROGRESS);
//		}
//		else {
//			order.setStatus(OrderStatus.B2B_PROCESSING_ERROR);
//		}
		
//		getModelService().save(order);
//		getModelService().refresh(order);
		getModelService().save(subOrder);
//		getModelService().refresh(subOrder);
		
		OrderModel order = subOrder.getOrder();
		if(!order.getStatus().equals(OrderStatus.IN_PROGRESS)) {
			order.setStatus(OrderStatus.IN_PROGRESS);
			LOG.info(String.format("Set order status : order [%s], status [%s]", order.getCode(), order.getStatus().getCode()));
			getModelService().save(order);
		}

	}
	
	@Override
	public void setSubOrderActionToLegacy(SubOrderModel subOrder, ActionToLegacyEnum actionToLegacy) {
		subOrder.setActionToLegacy(actionToLegacy);
		if(actionToLegacy.equals(ActionToLegacyEnum.CREATED_TO_LEGACY)) {
			OrderModel order = subOrder.getOrder();
			order.setSentTolegacy(true);
			getModelService().save(order);
			LOG.info(String.format("Set order [%s] as sent to legacy", order.getCode()));
//			getModelService().refresh(order);
		}
		getModelService().save(subOrder);
		getModelService().refresh(subOrder);
	}

	/*
	@Override
	public void checkOrderStatus(SubOrderModel subOrderModel, OrderModel orderModel, String status) throws Exception {

		if (status.equalsIgnoreCase(CommonWSServiceConstants.RequestStatus.HTTP_ERROR_CODE) && subOrderModel
				.getStatus().getCode().equalsIgnoreCase(SubOrderStatus.ERROR.getCode())) {

			if (checkSubOrdersIsOK(orderModel, subOrderModel.getPk())) {
				LOG.info("SET Order status to: SENT TO LEGACY");
				orderModel.setStatus(OrderStatus.IN_PROGRESS);
				orderModel.setSentTolegacy(true);
			} else {
				LOG.info("SET Order status to: PARTIALLY_SENT_TO_LEGACY");
				orderModel.setStatus(OrderStatus.B2B_PROCESSING_ERROR);
			}

			subOrderModel.setStatus(SubOrderStatus.ERROR);
			subOrderModel.setActionToLegacy(ActionToLegacyEnum.READY_TO_SEND_CREATION_TO_LEGACY);
		} else {

			if (checkSubOrdersIsOK(orderModel, subOrderModel.getPk())) {
				LOG.info("SET Order status to: SENT TO LEGACY");
				orderModel.setStatus(OrderStatus.IN_PROGRESS);
				orderModel.setSentTolegacy(true);
			} else {
				LOG.info("SET Order status to: PARTIALLY_SENT_TO_LEGACY");
				orderModel.setStatus(OrderStatus.B2B_PROCESSING_ERROR);
			}

			subOrderModel.setStatus(SubOrderStatus.OK);
			subOrderModel.setActionToLegacy(ActionToLegacyEnum.SENT_CREATION_TO_LEGACY);
		}

		getModelService().save(orderModel);
		getModelService().refresh(orderModel);
		getModelService().save(subOrderModel);
		getModelService().refresh(subOrderModel);
	}
	*/

	/*
	private Boolean checkSubOrdersIsOK(OrderModel orderModel, PK subOrderPK) {
		for (final SubOrderModel subOrder : orderModel.getSubOrders()) {
			if (!subOrder.getPk().equals(subOrderPK)
					&& subOrder.getStatus().getCode().equalsIgnoreCase(SubOrderStatus.ERROR.getCode())) {
				return false;
			}
		}

		return true;
	}
	*/



//	protected <T> Collection<T> nullSafe(final Collection<T> c) {
//		return (c == null) ? Collections.<T>emptyList() : c;
//	}

	protected ModelService getModelService() {
		return modelService;
	}

	@Required
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	protected ConfigurationService getConfigurationService() {
		return configurationService;
	}

	@Required
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	protected BusinessProcessService getBusinessProcessService() {
		return businessProcessService;
	}

	@Required
	public void setBusinessProcessService(BusinessProcessService businessProcessService) {
		this.businessProcessService = businessProcessService;
	}

	protected SolGroupAddressService getSolGroupAddressService() {
		return solGroupAddressService;
	}

	@Required
	public void setSolGroupAddressService(SolGroupAddressService solGroupAddressService) {
		this.solGroupAddressService = solGroupAddressService;
	}
	
	
	







}