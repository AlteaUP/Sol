package com.solgroup.core.interceptors.consignment;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.consignment.SolGroupConsignmentService;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.model.ModelService;

/**
 * 
 * @author fmilazzo
 *
 */
public class SolGroupConsignmentPrepareInterceptor implements PrepareInterceptor<ConsignmentModel> {

    private static final Logger LOG = Logger.getLogger(SolGroupConsignmentPrepareInterceptor.class);

    private ModelService modelService;
    private SolGroupConsignmentService solGroupConsignmentService;
    private BusinessProcessService businessProcessService;

    @Override
    public void onPrepare(final ConsignmentModel newConsignmentModel, final InterceptorContext ctx) throws InterceptorException {
        ConsignmentModel oldConsignmentModel = null;
        String oldTrackingID = null;
        String oldStatusCode = null;

        if (!getModelService().isNew(newConsignmentModel)) {
            oldConsignmentModel = getSolGroupConsignmentService().getConsignmentForCode(newConsignmentModel.getCode(),
                    newConsignmentModel.getOrder().getCode(), newConsignmentModel.getOrder().getSite());

            if (StringUtils.isNotEmpty(oldConsignmentModel.getTrackingID())) {
                oldTrackingID = oldConsignmentModel.getTrackingID();
            }

            if (StringUtils.isNotEmpty(oldConsignmentModel.getStatus().getCode())) {
                oldStatusCode = oldConsignmentModel.getStatus().getCode();
            }

            final String newStatusCode = newConsignmentModel.getStatus().getCode();

            // start business process to send delivery email
            if (StringUtils.equals(newStatusCode, ConsignmentStatus.SHIPPED.getCode())) {
                if (!StringUtils.equals(oldStatusCode, newStatusCode)) {
                    createSendDeliveryEmailProcess(newConsignmentModel);
                }
            }

            // start business process to send delivery confirmation email
            if (StringUtils.equals(newStatusCode, ConsignmentStatus.DELIVERED.getCode())) {
                if (!StringUtils.equals(oldStatusCode, newStatusCode)) {
                    createSendDeliveryConfirmationEmailProcess(newConsignmentModel);
                }
            }

        }

        if (getModelService().isModified(newConsignmentModel)) {
            final String newTrackingID = newConsignmentModel.getTrackingID();

            // start business process to send trackingID email
            if (StringUtils.isNotEmpty(newConsignmentModel.getTrackingID())) {

                if (!StringUtils.equals(oldTrackingID, newTrackingID)) {
                    createSendTrackingIDProcess(newConsignmentModel);
                }
            }
        }
    }

    private void createSendTrackingIDProcess(final ConsignmentModel consignmentModel) {
        final ConsignmentProcessModel consignmentProcessModel = (ConsignmentProcessModel) getBusinessProcessService().createProcess(
                "sendTrackingIdEmailProcess-" + consignmentModel.getTrackingID() + "-" + System.currentTimeMillis(),
                "sendTrackingIdEmailProcess");

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Created business process for send tracking ID email. Process code : [%s] ...",
                    consignmentProcessModel.getCode()));
        }

        consignmentProcessModel.setConsignment(consignmentModel);
        getModelService().save(consignmentProcessModel);
        getBusinessProcessService().startProcess(consignmentProcessModel);
    }

    private void createSendDeliveryEmailProcess(final ConsignmentModel consignmentModel) {
        final ConsignmentProcessModel consignmentProcessModel = getBusinessProcessService().createProcess(
                "sendDeliveryEmailProcess-" + consignmentModel.getCode() + "-" + System.currentTimeMillis(), "sendDeliveryEmailProcess");

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Created business process for send delivery email. Process code : [%s] ...",
                    consignmentProcessModel.getCode()));
        }

        consignmentProcessModel.setConsignment(consignmentModel);
        getModelService().save(consignmentProcessModel);
        getBusinessProcessService().startProcess(consignmentProcessModel);
    }

    private void createSendDeliveryConfirmationEmailProcess(final ConsignmentModel consignmentModel) {
        final ConsignmentProcessModel consignmentProcessModel = (ConsignmentProcessModel) getBusinessProcessService().createProcess(
                "sendDeliveryConfirmationEmailProcess-" + consignmentModel.getCode() + "-" + System.currentTimeMillis(),
                "sendDeliveryConfirmationEmailProcess");

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Created business process for send delivery confirmation email. Process code : [%s] ...",
                    consignmentProcessModel.getCode()));
        }

        consignmentProcessModel.setConsignment(consignmentModel);
        getModelService().save(consignmentProcessModel);
        getBusinessProcessService().startProcess(consignmentProcessModel);
    }

    protected ModelService getModelService() {
        return modelService;
    }

    @Required
    public void setModelService(final ModelService modelService) {
        this.modelService = modelService;
    }

    protected SolGroupConsignmentService getSolGroupConsignmentService() {
        return solGroupConsignmentService;
    }

    @Required
    public void setSolGroupConsignmentService(final SolGroupConsignmentService solGroupConsignmentService) {
        this.solGroupConsignmentService = solGroupConsignmentService;
    }

    protected BusinessProcessService getBusinessProcessService() {
        return businessProcessService;
    }

    @Required
    public void setBusinessProcessService(final BusinessProcessService businessProcessService) {
        this.businessProcessService = businessProcessService;
    }

}
