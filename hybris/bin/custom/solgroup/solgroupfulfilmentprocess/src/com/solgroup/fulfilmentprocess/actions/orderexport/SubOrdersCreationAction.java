package com.solgroup.fulfilmentprocess.actions.orderexport;

import com.solgroup.core.enums.ActionToLegacyEnum;
import com.solgroup.core.enums.SubOrderStatus;
import com.solgroup.core.model.LegacySystemModel;
import com.solgroup.core.model.SubOrderEntryModel;
import com.solgroup.core.model.SubOrderModel;
import com.solgroup.core.model.SubOrderProcessModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import org.apache.log4j.Logger;


import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class SubOrdersCreationAction extends AbstractSimpleDecisionAction<OrderProcessModel> {
    private static final Logger LOG = Logger.getLogger(SubOrdersCreationAction.class);

    private static final String ORDER_PROCESS_NAME = "suborder-export-process";
    private static final String subOrderEntryStatus = "1";

    @Resource private BusinessProcessService businessProcessService;

    @Override public Transition executeAction(final OrderProcessModel process) {

        final OrderModel order = process.getOrder();

        HashMap<LegacySystemModel, SubOrderModel> subOrderMap = new HashMap<>();
        HashMap<LegacySystemModel, Integer> entryNumbers = new HashMap<>();

        try {
            List<AbstractOrderEntryModel> orderEntries = order.getEntries();
            for (final AbstractOrderEntryModel orderEntry : orderEntries) {

                LegacySystemModel legacySystem = orderEntry.getProduct().getSourceSystem();
                if (legacySystem == null) {
                    LOG.error("No Source System setted on product: " + orderEntry.getProduct().getCode());
                }
                //String legacyName = legacySystem.getCode();

                SubOrderModel subOrder = null;
                SubOrderEntryModel entry = getModelService().create(SubOrderEntryModel.class);

                if (subOrderMap.containsKey(legacySystem)) {

                    subOrder = subOrderMap.get(legacySystem);
                    entryNumbers.put(legacySystem, entryNumbers.get(legacySystem) + 1);

                } else {

                    entryNumbers.put(legacySystem, 0);

                    subOrder = getModelService().create(SubOrderModel.class);
                    subOrder.setLegacySystem(legacySystem);
                    subOrder.setOrder(order);
                    subOrder.setStatus(SubOrderStatus.OK);
                    subOrder.setErrorDescription("");
                    subOrder.setActionToLegacy(ActionToLegacyEnum.READY_TO_SEND_CREATION_TO_LEGACY);

                    Collection<SubOrderProcessModel> subOrderProcessModels = new ArrayList<>();

                    SubOrderProcessModel subOrderProcess = null;
                    
                    final String orderProcessCode =
                            subOrder.getOrder().getCode() + "_" + subOrder.getLegacySystem().getCode() + "_"
                                    + ORDER_PROCESS_NAME;

                    // Create order process
                    subOrderProcess = businessProcessService.createProcess(orderProcessCode, ORDER_PROCESS_NAME);
                    subOrderProcess.setCmsSite((CMSSiteModel) subOrder.getOrder().getSite());
                    LOG.info("Created OrderProcess with code " + subOrderProcess.getCode());

                    // Attach order to orderProcess
                    subOrderProcess.setSubOrder(subOrder);
                    subOrderProcessModels.add(subOrderProcess);
                    subOrder.setSubOrderProcess(subOrderProcessModels);

                   
                    // Metto a DB l'entry nuova inserita
                    getModelService().save(subOrderProcess);

                    businessProcessService.startProcess(subOrderProcess);
                    LOG.info("Started subOrdeProcess with code " + subOrderProcess.getCode());

                    subOrderMap.put(legacySystem, subOrder);

                }
                entry.setOrderEntry((OrderEntryModel)orderEntry);
                entry.setOrderEntryNumber(entryNumbers.get(legacySystem));
                entry.setStatus(subOrderEntryStatus);
                entry.setErrorDescription("");
                entry.setSubOrder(subOrder);
                getModelService().save(entry);
            }
        } catch (Exception e) {
            LOG.error("Error in Splitting SubOrder: " + e.getMessage(), e);
            return Transition.NOK;
        }

        return Transition.OK;

    }

}
