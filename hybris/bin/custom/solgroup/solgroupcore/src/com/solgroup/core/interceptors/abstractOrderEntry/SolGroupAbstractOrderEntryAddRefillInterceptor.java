package com.solgroup.core.interceptors.abstractOrderEntry;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.model.ModelService;

public class SolGroupAbstractOrderEntryAddRefillInterceptor implements PrepareInterceptor<AbstractOrderEntryModel> {

    private final Logger LOG = Logger.getLogger(SolGroupAbstractOrderEntryAddRefillInterceptor.class);

    private ModelService modelService;
    private ConfigurationService configurationService;

    @Override
    public void onPrepare(AbstractOrderEntryModel orderEntry, InterceptorContext context) throws InterceptorException {
        final AbstractOrderModel order = orderEntry.getOrder();
        
        if(order instanceof OrderModel) {
        	return;
        }
        
        final ProductModel product = orderEntry.getProduct();
        final String materialsString = configurationService.getConfiguration().getString("quantity.code.materials");

        if (orderEntry != null && getModelService().isNew(orderEntry)) {
            if (product != null) {
                orderEntry.setRefill(product.getRefill());
                if (BooleanUtils.isTrue(orderEntry.getRefill())) {
                    if (order.getRefillOrderEntryNumber() == null || order.getRefillOrderEntryNumber().intValue() < 0)
                        order.setRefillOrderEntryNumber(NumberUtils.INTEGER_ZERO);
                    if (order != null && order.getRefillOrderEntryNumber().intValue() >= 0) {
                        order.setRefillOrderEntryNumber(order.getRefillOrderEntryNumber() + NumberUtils.INTEGER_ONE);
                        LOG.info("REFILL products into cart: " + order.getRefillOrderEntryNumber());
                    }
                }
            }
        }

        // OrderEntry quantity setting
        if (BooleanUtils.isTrue(orderEntry.getRefill()) && materialsString.equals(getProductMaterial(product))
                && getOrderEntryQuantity(orderEntry) > 1) {
            orderEntry.setQuantity(NumberUtils.LONG_ONE);
        }
        
    }

    private String getProductMaterial(ProductModel product) {
        String productMaterial = null;
        if (product.getMaterial() != null && product.getMaterial().getCode() != null) {
            productMaterial = product.getMaterial().getCode();
        }
        return productMaterial;
    }

    private int getOrderEntryQuantity(AbstractOrderEntryModel orderEntry) {
        if (orderEntry.getQuantity() != null) {
            return orderEntry.getQuantity().intValue();
        }
        return 0;
    }

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

}
