package com.solgroup.core.jalo;

import java.util.Collections;
import java.util.List;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.constants.SolgroupCoreConstants;

import de.hybris.platform.catalog.jalo.CatalogAwareEurope1PriceFactory;
import de.hybris.platform.europe1.jalo.PriceRow;
import de.hybris.platform.servicelayer.session.SessionService;

public class SolGroupEurope1PriceFactory extends CatalogAwareEurope1PriceFactory{
	
	private SessionService sessionService;
	
	
	
    protected List<PriceRow> filterPriceRows(List<PriceRow> priceRows) {
        if(priceRows.isEmpty()) {
           return Collections.EMPTY_LIST;
        } 
        else if (priceRows.size()==1) {
        	return priceRows;
        }
        else {
        	List<PriceRow> customerPriceRowList = Lists.newArrayList();
        	String customerPriceListId = getSessionService().getAttribute(SolgroupCoreConstants.SESSION_PARAM_PRICE_LIST_CODE);
        	if(customerPriceListId==null) {
        		customerPriceListId = "";
        	}
        			
        	for(PriceRow priceRow : priceRows) {
        		
        		if(priceRow.getCustomerPriceGroup()==null && customerPriceListId.isEmpty()) {
        			customerPriceRowList.add(priceRow);
        		}
        		
        		else if(priceRow.getCustomerPriceGroup()!=null && priceRow.getCustomerPriceGroup().getCode().equals(customerPriceListId)) {
        			customerPriceRowList.add(priceRow);
        		}
        	}
        	
        	return super.filterPriceRows(customerPriceRowList);
        
        }
        
     }


	protected SessionService getSessionService() {
		return sessionService;
	}

	@Required
	public void setSessionService(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	
	
	
}
