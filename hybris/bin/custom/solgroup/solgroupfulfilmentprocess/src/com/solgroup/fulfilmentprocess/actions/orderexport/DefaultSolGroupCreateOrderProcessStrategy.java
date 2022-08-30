package com.solgroup.fulfilmentprocess.actions.orderexport;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.process.approval.model.B2BApprovalProcessModel;
import de.hybris.platform.b2b.strategies.BusinessProcessStrategy;
import de.hybris.platform.b2b.strategies.impl.DefaultB2BApprovalBusinessProcessStrategy;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.HashMap;
import java.util.Map;

public class DefaultSolGroupCreateOrderProcessStrategy implements BusinessProcessStrategy {
    protected static final Logger LOG = Logger.getLogger(DefaultSolGroupCreateOrderProcessStrategy.class);
    private BusinessProcessService businessProcessService;
    private KeyGenerator processCodeGenerator;
    private ModelService modelService;

    public DefaultSolGroupCreateOrderProcessStrategy() {
    }

    public void createB2BBusinessProcess(OrderModel order) {
        //Map<String, Object> contextParams = new HashMap();
        //contextParams.put("EVENT_AFTER_WORKFLOW_PARAM", "APPROVAL_WORKFLOW_COMPLETE_EVENT");
        //B2BCustomerModel orderUser = (B2BCustomerModel)order.getUser();
        String orderProcessCode = getProcessCodeGenerator().generate().toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("BusinessProcess is going to be created using the following keyGenerator " + ReflectionToStringBuilder
                    .toString(this.getProcessCodeGenerator()));
        }

        String processName = order.getSite().getStores().get(0).getSubmitOrderProcessCode();

        OrderProcessModel orderProcess = (OrderProcessModel) this.getBusinessProcessService()
                .createProcess(orderProcessCode, processName);
        orderProcess.setOrder(order);
        this.getModelService().save(orderProcess);
        this.getBusinessProcessService().startProcess(orderProcess);
    }

    @Required
    public void setProcessCodeGenerator(KeyGenerator processCodeGenerator) {
        this.processCodeGenerator = processCodeGenerator;
    }

    protected KeyGenerator getProcessCodeGenerator() {
        return this.processCodeGenerator;
    }

    @Required
    public void setBusinessProcessService(BusinessProcessService businessProcessService) {
        this.businessProcessService = businessProcessService;
    }

    protected BusinessProcessService getBusinessProcessService() {
        return this.businessProcessService;
    }

    protected ModelService getModelService() {
        return this.modelService;
    }

    @Required
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }
}
