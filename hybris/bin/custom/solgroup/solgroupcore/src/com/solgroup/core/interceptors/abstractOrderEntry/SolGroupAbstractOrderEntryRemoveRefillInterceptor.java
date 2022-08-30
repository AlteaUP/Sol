package com.solgroup.core.interceptors.abstractOrderEntry;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;
import de.hybris.platform.servicelayer.model.ModelService;

public class SolGroupAbstractOrderEntryRemoveRefillInterceptor implements RemoveInterceptor<AbstractOrderEntryModel> {
    
    private final Logger LOG = Logger.getLogger(SolGroupAbstractOrderEntryRemoveRefillInterceptor.class);

    private ModelService modelService;

    @Override
    public void onRemove(AbstractOrderEntryModel entry, InterceptorContext context) throws InterceptorException {
        final AbstractOrderModel order = entry.getOrder();
        final ProductModel product = entry.getProduct();
        if (product != null && BooleanUtils.isTrue(product.getRefill())) {
            if (order != null && order.getRefillOrderEntryNumber() != null
                    && order.getRefillOrderEntryNumber().intValue() > 0) {
                order.setRefillOrderEntryNumber(order.getRefillOrderEntryNumber() - NumberUtils.INTEGER_ONE);
                getModelService().save(order);
                LOG.info("REFILL products into cart: " + order.getRefillOrderEntryNumber());
            }
        }
    }

    protected ModelService getModelService() {
        return modelService;
    }

    @Required
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

}
