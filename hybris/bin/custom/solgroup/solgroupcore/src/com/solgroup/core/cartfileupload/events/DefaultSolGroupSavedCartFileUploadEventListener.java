package com.solgroup.core.cartfileupload.events;

import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.acceleratorservices.cartfileupload.events.SavedCartFileUploadEvent;
import de.hybris.platform.acceleratorservices.cartfileupload.events.SavedCartFileUploadEventListener;
import de.hybris.platform.acceleratorservices.enums.ImportStatus;
import de.hybris.platform.commerceservices.strategies.NetGrossStrategy;
import de.hybris.platform.core.model.order.CartModel;

public class DefaultSolGroupSavedCartFileUploadEventListener extends SavedCartFileUploadEventListener {

	private NetGrossStrategy netGrossStrategy;
	
	@Override
	protected CartModel createSavedCartForProcess(final SavedCartFileUploadEvent event)
	{
		final CartModel cartModel = getModelService().create(CartModel.class);
		cartModel.setSaveTime(getTimeService().getCurrentTime());
		cartModel.setName(String.valueOf(System.currentTimeMillis()));
		cartModel.setUser(event.getCustomer());
		cartModel.setCurrency(event.getCurrency());
		cartModel.setDate(getTimeService().getCurrentTime());
		cartModel.setSite(event.getSite());
		cartModel.setSavedBy(event.getCustomer());
		cartModel.setImportStatus(ImportStatus.PROCESSING);
		cartModel.setStore(event.getBaseStore());
		cartModel.setGuid(getGuidKeyGenerator().generate().toString());
		cartModel.setNet(Boolean.valueOf(getNetGrossStrategy().isNet()));
		getModelService().save(cartModel);
		getModelService().refresh(cartModel);
		return cartModel;
	}

	protected NetGrossStrategy getNetGrossStrategy() {
		return netGrossStrategy;
	}

	@Required
	public void setNetGrossStrategy(NetGrossStrategy netGrossStrategy) {
		this.netGrossStrategy = netGrossStrategy;
	}

	
	
	
}
