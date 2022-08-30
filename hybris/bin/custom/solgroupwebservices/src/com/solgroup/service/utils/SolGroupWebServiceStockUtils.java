package com.solgroup.service.utils;


import java.math.BigInteger;
import java.util.List;

import com.solgroup.core.service.product.SolGroupProductService;
import com.solgroup.core.ws.services.stock.StocksResponse;
import com.solgroup.core.ws.services.stock.StocksResponse.StockProductList.StockProduct;
import com.solgroup.service.impexImport.Impex;
import com.solgroup.service.impexImport.ImpexAttribute;
import com.solgroup.service.impexImport.ImpexAttributeModifier;
import com.solgroup.service.impexImport.ImpexHeader;
import com.solgroup.service.impexImport.ImpexRow;
import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.Registry;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;

public class SolGroupWebServiceStockUtils {
	
	public static String generateImpex_importStock(StocksResponse stocksResponse, WarehouseModel warehouse, CMSSiteModel cmsSite) {
		return createImpexStrategy(stocksResponse, warehouse, cmsSite).toImpex();
		
	}
	
	
	
	private static Impex createImpexStrategy(StocksResponse stocksResponse, WarehouseModel warehouseM, CMSSiteModel cmsSite) {
		ImpexAttributeModifier unique = new ImpexAttributeModifier("unique=true",
				ImpexAttributeModifier.TypeBrackets.SQUARE);
		ImpexAttributeModifier _default = new ImpexAttributeModifier("default=default",
				ImpexAttributeModifier.TypeBrackets.SQUARE);
		ImpexAttributeModifier code = new ImpexAttributeModifier("code",
				ImpexAttributeModifier.TypeBrackets.ROUNDS);

		ImpexAttribute productCode = new ImpexAttribute("productCode");
		productCode.add(unique);

		ImpexAttribute warehouse = new ImpexAttribute("warehouse");
		warehouse.add(unique);
		warehouse.add(_default);
		warehouse.add(code);

		ImpexAttribute available = new ImpexAttribute("available");

		ImpexAttribute inStockStatus = new ImpexAttribute("inStockStatus");
		inStockStatus.add(code);

		ImpexHeader header = new ImpexHeader("StockLevel", ImpexHeader.Action.UPDATE);
		header.add(productCode);
		header.add(warehouse);
		header.add(available);
		header.add(inStockStatus);

		Impex stockLevel = new Impex();
		stockLevel.setHeader(header);

//		WarehouseModel warehouseM = getSolGroupBaseCommerceService().getDefaultWarehouseForLegacySystemAndCountry(stockResponse.getHeader().getSourceSystem(), stockResponse.getCountry());
//		CMSSiteModel cmsSite = getCommonWsService().getCMSSite(stockResponse.getHeader().getSourceSystem(), stockResponse.getCountry());

		List<StockProduct> products = stocksResponse.getStockProductList().getStockProduct();
		
		
		for (StockProduct i : products) {
			ImpexRow row = new ImpexRow();

//			row.put(productCode, SolgroupWebServiceUtils.generateSolgroupProductCode(cmsSite, i.getProductCode()));
			row.put(productCode, getSolgroupProductService().getHybrisProductCode(i.getProductCode(), cmsSite));
			row.put(warehouse, warehouseM.getCode());
			row.put(available, i.getAvailableQty().toString());
			row.put(inStockStatus, evaluateInStockStatus(i.getAvailableQty(),cmsSite).getCode());

			stockLevel.add(row);
		}
		return stockLevel;
	}
	
	
	
	private static InStockStatus evaluateInStockStatus(BigInteger avalaible, CMSSiteModel cmsSite) {
		Integer availableI = avalaible.intValue();
		if (availableI > getThreshold(cmsSite))
			return InStockStatus.FORCEINSTOCK;
		else
			return InStockStatus.FORCEOUTOFSTOCK;
	}
	
	private static Integer getThreshold(CMSSiteModel cmsSite) {
		return getConfigurationService().getConfiguration().getInt(cmsSite.getUid() + ".stock.threshold", 0);
	}
	
	private static ConfigurationService getConfigurationService() {
		ConfigurationService configurationService = (ConfigurationService) Registry.getApplicationContext().getBean("configurationService");
		return configurationService;
	}
	
	private static SolGroupProductService getSolgroupProductService() {
		SolGroupProductService solGroupProductService = (SolGroupProductService) Registry.getApplicationContext().getBean("solGroupProductService");
		return solGroupProductService;
		
	}


}
