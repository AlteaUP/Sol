package com.solgroup.core.interceptors.abstractOrderEntry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.commerceservices.price.CommercePriceService;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.europe1.jalo.PriceRow;
import de.hybris.platform.jalo.order.price.PriceInformation;
import de.hybris.platform.jalo.product.Unit;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.model.ModelService;

public class SolGroupAbstractOrderEntryUnitMeasurePrepareInterceptor implements PrepareInterceptor<AbstractOrderEntryModel> {

    private final Logger LOG = Logger.getLogger(SolGroupAbstractOrderEntryUnitMeasurePrepareInterceptor.class);

    private ModelService modelService;
	private CommercePriceService commercePriceService;
    

    @Override
    public void onPrepare(AbstractOrderEntryModel orderEntry, InterceptorContext context) throws InterceptorException {
        if(context.isNew(orderEntry)) {
	    	final ProductModel product = orderEntry.getProduct();
	        PriceInformation priceInfo = getCommercePriceService().getWebPriceForProduct(product);
	        if(priceInfo!=null && priceInfo.getQualifierValue("pricerow")!=null && priceInfo.getQualifierValue("pricerow") instanceof PriceRow) {
	        	PriceRow priceRow =  (PriceRow) priceInfo.getQualifierValue("pricerow");
	    		Unit unit = priceRow.getUnit();
	        	if(unit!=null) {
	        		UnitModel unitModel = modelService.get(unit.getPK());
	        		orderEntry.setUnit(unitModel);
	        	}
	        	
	        }
        }
    }

    @Required
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    protected ModelService getModelService() {
        return modelService;
    }

	protected CommercePriceService getCommercePriceService() {
		return commercePriceService;
	}

	@Required
	public void setCommercePriceService(CommercePriceService commercePriceService) {
		this.commercePriceService = commercePriceService;
	}

    
    

}
