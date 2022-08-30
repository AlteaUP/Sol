package com.solgroup.core.strategies.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.model.LegacySystemModel;
import com.solgroup.core.strategies.SolgroupBaseCommerceSelectionProductStrategy;
import com.solgroup.core.ws.service.CommonWsService;

import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.store.BaseStoreModel;

public class DefaultSolgroupBaseCommerceSelectionProductStrategy implements SolgroupBaseCommerceSelectionProductStrategy {

	private Logger LOG = Logger.getLogger(DefaultSolgroupBaseCommerceSelectionProductStrategy.class);

	private CommonWsService commonWsService;
	
	
	@Override
	public <T extends ProductModel> WarehouseModel getDefaultWarehouseForProduct(T product) {

		BaseStoreModel baseStore = getDefaultBaseStoreForProduct(product);
		if (baseStore.getWarehouses() == null || baseStore.getWarehouses().isEmpty()) {
			LOG.error("Base store " + baseStore.getName() + " must have a warehouse!");
			return null;
		}

		return baseStore.getWarehouses().get(0);
	}

	@Override
	public <T extends ProductModel> BaseStoreModel getDefaultBaseStoreForProduct(T product) {

		validateParameterNotNullStandardMessage("product", product);

		if (product.getCatalogVersion().getCatalog().getBaseStores() == null
				|| product.getCatalogVersion().getCatalog().getBaseStores().isEmpty()) {
			LOG.error("Catalog " + product.getCatalogVersion().getCatalog().getName() + " must have a base store!");
			return null;
		}

		return product.getCatalogVersion().getCatalog().getBaseStores().iterator().next();
	}

	@Override
	public WarehouseModel getDefaultWarehouseForLegacySystemAndCountry(String legacySystemCode,
			String countryIsoCode) {
		WarehouseModel warehouse = null;
		try{
			warehouse = getCommonWsService().getCMSSite(legacySystemCode, countryIsoCode).getStores().get(0).getWarehouses().get(0);
			if (getCommonWsService().getCMSSite(legacySystemCode, countryIsoCode).getStores().size() != 1 || getCommonWsService().getCMSSite(legacySystemCode, countryIsoCode).getStores().get(0).getWarehouses().size() !=1)
				throw new NullPointerException();
		}
		catch (NullPointerException e)
		{
			LOG.error("Site don't have a store or store don't have a warehouse!!! Both relation must have cardinality one!!!");
			LOG.error(e.getStackTrace());
			throw e;
			
		}
		return warehouse;
		
	}

	
	protected CommonWsService getCommonWsService() {
		return commonWsService;
	}
	
	@Required
	public void setCommonWsService(CommonWsService commonWsService) {
		this.commonWsService = commonWsService;
	}


}