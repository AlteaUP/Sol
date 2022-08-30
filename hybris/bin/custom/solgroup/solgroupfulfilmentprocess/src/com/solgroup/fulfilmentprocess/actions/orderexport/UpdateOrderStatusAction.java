package com.solgroup.fulfilmentprocess.actions.orderexport;

import com.google.common.collect.Maps;
import com.solgroup.core.service.order.SolGroupOrderService;
import com.solgroup.fulfilmentprocess.beans.EntryBeanData;
import com.solgroup.service.utils.SolgroupWebServiceUtils;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;
import java.util.stream.Collectors;

public class UpdateOrderStatusAction extends AbstractAction<OrderProcessModel> {
	
	private SolGroupOrderService solGroupOrderService;
	
	
	
	
    private static final Logger LOG = Logger.getLogger(UpdateOrderStatusAction.class);

    public enum Transition {
        NOTFINAL, OK;

        public static Set<String> getStringValues() {
            final Set<String> res = new HashSet<String>();

            for (final Transition transition : Transition.values()) {
                res.add(transition.toString());
            }
            return res;
        }
    }

    @Override public String execute(final OrderProcessModel process) throws RetryLaterException, Exception {
        final OrderModel orderModel = process.getOrder();

        Map<String, EntryBeanData> entriesMap = Maps.newHashMap();

        // Populate entries map with productCode and order quantity
        for (AbstractOrderEntryModel orderEntry : orderModel.getEntries()) {
            EntryBeanData entryBean = new EntryBeanData();
            entryBean.setProductCode(orderEntry.getProduct().getErpCode());
            entryBean.setOrderQty(orderEntry.getQuantity().intValue());
            entryBean.setDeliveredQty(0);
            entryBean.setCancelledQty(0);
            entriesMap.put(orderEntry.getProduct().getErpCode(), entryBean);
        }

        // Get all consignment entries in final status
        List<ConsignmentEntryModel> consignmentEntrieslist = new ArrayList<ConsignmentEntryModel>();
        for (ConsignmentModel consignment : orderModel.getConsignments()) {
            consignmentEntrieslist.addAll(consignment.getConsignmentEntries());
        }
        //List<ConsignmentEntryModel> consignmentEntriesFinal = (List<ConsignmentEntryModel>) SolgroupWebServiceUtils.findAnyByProperty(consignmentEntrieslist, consignmentEntry -> ArrayUtils.contains(SolgroupWebServiceUtils.OrderStateFinalArray, consignmentEntry.getStatus()));
        List<ConsignmentEntryModel> consignmentEntriesFinal = (List<ConsignmentEntryModel>) SolgroupWebServiceUtils.findAnyByProperty(consignmentEntrieslist, consignmentEntry -> getSolGroupOrderService().isFinalStatus(consignmentEntry.getStatus().getCode()));
        
        int consignmentEntriesDelivered_size = ((List<ConsignmentEntryModel>) SolgroupWebServiceUtils
                .findAnyByProperty(consignmentEntriesFinal,
                        consignmentEntry -> ConsignmentStatus.DELIVERED.getCode().equalsIgnoreCase(consignmentEntry.getStatus().getCode()))).size();

        int consignmentEntriesCancelled_size = ((List<ConsignmentEntryModel>) SolgroupWebServiceUtils
                .findAnyByProperty(consignmentEntriesFinal,
                        consignmentEntry -> ConsignmentStatus.CANCELLED.getCode().equalsIgnoreCase(consignmentEntry.getStatus().getCode()))).size();

        for (ConsignmentEntryModel consignmentEntry : consignmentEntriesFinal) {
            if (consignmentEntry.getStatus().equals(ConsignmentStatus.DELIVERED)) {
                int previousQty = entriesMap.get(consignmentEntry.getOrderEntry().getProduct().getErpCode())
                        .getDeliveredQty();
                entriesMap.get(consignmentEntry.getOrderEntry().getProduct().getErpCode())
                        .setDeliveredQty(previousQty + consignmentEntry.getQuantity().intValue());
            } else if (consignmentEntry.getStatus().equals(ConsignmentStatus.CANCELLED)) {
                int previousQty = entriesMap.get(consignmentEntry.getOrderEntry().getProduct().getErpCode())
                        .getCancelledQty();
                entriesMap.get(consignmentEntry.getOrderEntry().getProduct().getErpCode())
                        .setCancelledQty(previousQty + consignmentEntry.getQuantity().intValue());
            }
        }

        // EVALUATE order status
        OrderStatus orderStatus = OrderStatus.IN_PROGRESS;

        // Only DELIVERED or DELIVERED and CANCELLED  +
        if (consignmentEntriesDelivered_size > 0) {
            boolean totallyDelivered = true;
            for (EntryBeanData entryBean : entriesMap.values()) {
                if (entryBean.getOrderQty() > entryBean.getDeliveredQty()) {
                    totallyDelivered = false;
                    break;
                }
            }
            if (totallyDelivered) {
                LOG.info("All Consingnments are delivered");
                orderStatus = OrderStatus.DELIVERED;
            } else {
                LOG.info("At least one Consingnments is not delivered");
                orderStatus = OrderStatus.PARTIALLY_DELIVERED;
            }
        }

        // Only CANCELLED
        else if (consignmentEntriesCancelled_size > 0 && consignmentEntriesDelivered_size == 0) {
            boolean totallyCancelled = true;
            for (EntryBeanData entryBean : entriesMap.values()) {
                if (entryBean.getOrderQty() > entryBean.getCancelledQty()) {
                    totallyCancelled = false;
                    break;
                }
            }
            if (totallyCancelled) {
                LOG.info("All Consingnments are cancelled");
                orderStatus = OrderStatus.CANCELLED;
            } else {
                LOG.info("Not All Consingnments are cancelled, setted to IN_PROGRESS");
                orderStatus = OrderStatus.IN_PROGRESS;
            }
        }
        orderModel.setStatus(orderStatus);
        saveOrderModel(orderModel);

        boolean orderInFinalStatus = true;
        for (EntryBeanData entryBean : entriesMap.values()) {
            if (!entryBean.isQtyCompleted()) {
                orderInFinalStatus = false;
                break;
            }
        }

        if (orderInFinalStatus) {
            LOG.info("Every Consingnments is in a final satus");
            return Transition.OK.toString();
        } else {
            LOG.info(
                    "The quantity of Order Entries is not equals to consignment entries Quantity! I should wait at least one more consignment to close the order");
            return Transition.NOTFINAL.toString();
        }
    }

    private void saveOrderModel(OrderModel orderModel) throws Exception {

        try {
            // Metto a DB l'entry nuova inserita
            getModelService().save(orderModel);
            getModelService().refresh(orderModel);
        } catch (Exception e) {

            LOG.error(String.format("Error saving consignmentProcess [%s] !!!", e.getMessage()));
            throw new Exception(String.format("Error saving consignmentProcess [%s] !!!", e.getMessage()));
        }
    }

    @Override public Set<String> getTransitions() {
        return Transition.getStringValues();
    }

	protected SolGroupOrderService getSolGroupOrderService() {
		return solGroupOrderService;
	}

	@Required
	public void setSolGroupOrderService(SolGroupOrderService solGroupOrderService) {
		this.solGroupOrderService = solGroupOrderService;
	}
    
    
    
}
