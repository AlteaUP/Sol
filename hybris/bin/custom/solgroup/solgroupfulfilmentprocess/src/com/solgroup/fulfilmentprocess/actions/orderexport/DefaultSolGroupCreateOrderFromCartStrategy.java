package com.solgroup.fulfilmentprocess.actions.orderexport;

import de.hybris.platform.b2b.strategies.BusinessProcessStrategy;
import de.hybris.platform.b2b.strategies.impl.DefaultB2BCreateOrderFromCartStrategy;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.InvalidCartException;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

public class DefaultSolGroupCreateOrderFromCartStrategy extends DefaultB2BCreateOrderFromCartStrategy {
    private List<BusinessProcessStrategy> businessProcessStrategies;

    public DefaultSolGroupCreateOrderFromCartStrategy() {
    }

    public OrderModel createOrderFromCart(CartModel cart) throws InvalidCartException {
        OrderModel orderFromCart = super.createOrderFromCart(cart);
        return orderFromCart;
    }

    public void createB2BBusinessProcess(OrderModel order) {
        BusinessProcessStrategy businessProcessStrategy = this.getBusinessProcessStrategies().get(0);
        businessProcessStrategy.createB2BBusinessProcess(order);
    }

    @Required public void setBusinessProcessStrategies(List<BusinessProcessStrategy> businessProcessStrategies) {
        this.businessProcessStrategies = businessProcessStrategies;
    }

    protected List<BusinessProcessStrategy> getBusinessProcessStrategies() {
        return this.businessProcessStrategies;
    }

}

