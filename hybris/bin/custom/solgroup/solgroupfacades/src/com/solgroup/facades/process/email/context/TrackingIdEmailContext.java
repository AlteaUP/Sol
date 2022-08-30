package com.solgroup.facades.process.email.context;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.order.data.ConsignmentData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

/**
 * 
 * @author fmilazzo
 *
 */
public class TrackingIdEmailContext extends AbstractEmailContext<ConsignmentProcessModel> {

    private static final Logger LOG = Logger.getLogger(TrackingIdEmailContext.class);

    private Converter<ConsignmentModel, ConsignmentData> consignmentConverter;
    private Converter<OrderModel, OrderData> orderConverter;
    private ConsignmentData consignmentData;
    private OrderData orderData;
    
    public static final String CONSIGNMENT_DATA = "consignmentData";
    public static final String ORDER_DATA = "orderData";
    
    @Override
    public void init(final ConsignmentProcessModel consignmentProcessModel, final EmailPageModel emailPageModel) {
        super.init(consignmentProcessModel, emailPageModel);
        consignmentData = getConsignmentConverter().convert(consignmentProcessModel.getConsignment());
        orderData = getOrderConverter().convert((OrderModel) consignmentProcessModel.getConsignment().getOrder());
        
        put(CONSIGNMENT_DATA, consignmentData);
        put(ORDER_DATA, orderData);  

        if (LOG.isDebugEnabled()) {
         LOG.debug("Initialized with order code " + orderData.getCode());
         }
    }

    @Override
    protected BaseSiteModel getSite(final ConsignmentProcessModel consignmentProcessModel) {
        return consignmentProcessModel.getConsignment().getOrder().getSite();
    }

    @Override
    protected CustomerModel getCustomer(final ConsignmentProcessModel consignmentProcessModel) {
        return (CustomerModel) consignmentProcessModel.getConsignment().getOrder().getUser();
    }

    @Override
    protected LanguageModel getEmailLanguage(final ConsignmentProcessModel consignmentProcessModel) {
        if (consignmentProcessModel.getConsignment().getOrder() instanceof OrderModel)
            return ((OrderModel) consignmentProcessModel.getConsignment().getOrder()).getLanguage();

        return null;
    }

    protected Converter<ConsignmentModel, ConsignmentData> getConsignmentConverter() {
        return consignmentConverter;
    }

    @Required
    public void setConsignmentConverter(final Converter<ConsignmentModel, ConsignmentData> consignmentConverter) {
        this.consignmentConverter = consignmentConverter;
    }

     protected Converter<OrderModel, OrderData> getOrderConverter() {
     return orderConverter;
     }
    
     @Required
     public void setOrderConverter(final Converter<OrderModel, OrderData> orderConverter) {
     this.orderConverter = orderConverter;
     }

    protected ConsignmentData getConsignmentData() {
        return consignmentData;
    }

     protected OrderData getOrderData() {
     return orderData;
     }

}
