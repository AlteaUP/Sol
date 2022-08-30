package com.solgroup.facades.populators.cart;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import de.hybris.platform.commercefacades.order.converters.populator.MiniCartPopulator;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.core.model.order.CartModel;

public class SolGroupMiniCartPopulator extends MiniCartPopulator {
    
    @Override
    public void populate(final CartModel source, final CartData target) {
        super.populate(source, target);
        if (source != null) {
            // Se esiste almeno una entry di tipo refill il carrello/ordine è refill
            if (source.getRefillOrderEntryNumber() != null && source.getRefillOrderEntryNumber() > 0) {
            	target.setRefill(Boolean.TRUE);
            } else {
            	target.setRefill(Boolean.FALSE);
            }
        }
        
        List<OrderEntryData> entries = Lists.newArrayList();
        if(source!=null && source.getEntries()!=null) {
        	for(int i=0; i<source.getEntries().size(); i++) {
        		entries.add(new OrderEntryData());
        	}
        }
        target.setEntries(entries);
        
        

        
    }
}
