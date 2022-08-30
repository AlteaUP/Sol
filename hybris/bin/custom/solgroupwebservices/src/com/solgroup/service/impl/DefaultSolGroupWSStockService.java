package com.solgroup.service.impl;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.model.LegacySystemModel;
import com.solgroup.core.service.basecommerce.SolGroupBaseCommerceService;
import com.solgroup.core.util.SolGroupUtilities;
import com.solgroup.core.ws.service.CommonWsService;
import com.solgroup.core.ws.services.stock.StocksImportOB;
import com.solgroup.core.ws.services.stock.StocksRequest;
import com.solgroup.core.ws.services.stock.StocksResponse;
import com.solgroup.service.SolGroupWSStockService;
import com.solgroup.service.utils.SolGroupWebServiceStockUtils;
import com.solgroup.service.utils.SolgroupWebServiceUtils;

import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import reactor.util.CollectionUtils;

public class DefaultSolGroupWSStockService implements SolGroupWSStockService {

	private static final String IMPORT_STOCK_JOB = "importStockJob";

	private final static String PO_STOCK_SERVICE_URL = "app.core.services.serviceUrl.po.importStock";
	
	private static final String RESPONSE_STATUS_OK = "OK";
	private static final String RESPONSE_STATUS_KO = "KO";

	private static final Logger LOG = Logger.getLogger(DefaultSolGroupWSStockService.class);

	private CommonWsService commonWsService;
	private ConfigurationService configurationService;
	private SolGroupUtilities solGroupUtilities;
	private SolGroupBaseCommerceService solGroupBaseCommerceService;
	
	
	@Override
	public void sendStockRequest(LegacySystemModel legacySystem, CountryModel country) {

		StocksRequest stockRequest = new StocksRequest();

		StocksRequest.Header headerRequest = new StocksRequest.Header();
		headerRequest.setTargetSystem(legacySystem.getCode());
		stockRequest.setHeader(headerRequest);
		stockRequest.setCountry(country.getIsocode());
		
		SolgroupWebServiceUtils.logXmlData(headerRequest, LOG);

		String WsUrl = getConfigurationService().getConfiguration().getString(PO_STOCK_SERVICE_URL);
		StocksImportOB stockClient = (StocksImportOB) SolgroupWebServiceUtils.createSoapClient(WsUrl, StocksImportOB.class);

		Client client = ClientProxy.getClient(stockClient);
		SolgroupWebServiceUtils.configureClient(client);

		LOG.info("Send full stocks request to Legacy - " + legacySystem.getCode());
		SolgroupWebServiceUtils.logXmlData(stockRequest, LOG);

		StocksResponse response = stockClient.stocksImportOB(stockRequest);
		
		SolgroupWebServiceUtils.logXmlData(stockRequest, LOG);
		
		if(response.getStatus()!=null && !response.getStatus().toUpperCase().equals(RESPONSE_STATUS_OK)) {
			LOG.error("Stock response contains a KO status !!! ImportStockJob will not be created." );
			return;
		}
		
		try {
			getCommonWsService().prepareImportDataJob(response, IMPORT_STOCK_JOB);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

	}

	@Override
	public void sendStockRequest(LegacySystemModel legacySystem, CountryModel country, Collection<ProductModel> products) {

		if (products.isEmpty())
		{
			LOG.warn("No products for stock request !!!");
			return;
		}
		
		// Create stock request
		StocksRequest stockRequest = new StocksRequest();
		StocksRequest.Header headerRequest = new StocksRequest.Header();
		headerRequest.setTargetSystem(legacySystem.getCode());
		stockRequest.setHeader(headerRequest);
		stockRequest.setCountry(country.getIsocode());
		stockRequest.setProductCodeList(generateBodyRequest(products));
		
		if(CollectionUtils.isEmpty(stockRequest.getProductCodeList().getProductCode())) {
			LOG.info("No stockable products. No stock request will be generated !!!");
			return;
		}

		String WsUrl = getConfigurationService().getConfiguration().getString(PO_STOCK_SERVICE_URL);
		StocksImportOB stockClient = (StocksImportOB) SolgroupWebServiceUtils.createSoapClient(WsUrl, StocksImportOB.class);
		Client client = ClientProxy.getClient(stockClient);
		SolgroupWebServiceUtils.configureClient(client);

		SolgroupWebServiceUtils.logXmlData(stockRequest, LOG);
		
		// Send request to legacy and obtain stock response
		StocksResponse stockResponse = stockClient.stocksImportOB(stockRequest);
		
		SolgroupWebServiceUtils.logXmlData(stockResponse, LOG);
		
		
		try {
			WarehouseModel warehouse = getSolGroupBaseCommerceService().getDefaultWarehouseForLegacySystemAndCountry(stockResponse.getHeader().getSourceSystem(), stockResponse.getCountry());
			CMSSiteModel cmsSite = getCommonWsService().getCMSSite(stockResponse.getHeader().getSourceSystem(), stockResponse.getCountry());

			String impexBody = SolGroupWebServiceStockUtils.generateImpex_importStock(stockResponse, warehouse, cmsSite);
			
			String messageId = "messageId";
			if(StringUtils.isNotEmpty(stockResponse.getHeader().getMessageID())) {
				messageId = stockResponse.getHeader().getMessageID();
			}
			
			String impexCronJobCode = String.format(SolgroupCoreConstants.IMPEX_IMPORT_CRONJOB_CODE_PATTERN, cmsSite.getUid(),stockResponse.getHeader().getSourceSystem(),"importStockAfterOrder_job",messageId,System.currentTimeMillis());
			String impexMediaCode = String.format(SolgroupCoreConstants.IMPEX_IMPORT_CRONJOB_CODE_PATTERN, cmsSite.getUid(),stockResponse.getHeader().getSourceSystem(),"importStockAfterOrder_media",messageId,System.currentTimeMillis()); 
			getSolGroupUtilities().importImpex(impexBody, impexMediaCode, impexCronJobCode, 1, true);
			
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}

	}

	private StocksRequest.ProductCodeList generateBodyRequest(Collection<ProductModel> products) {

		StocksRequest.ProductCodeList productList = new StocksRequest.ProductCodeList();

		for (ProductModel product : products){
			if (product.getStockable())
				productList.getProductCode().add(product.getErpCode());
		}
		
		return productList;
	}

	
	
	protected CommonWsService getCommonWsService() {
		return commonWsService;
	}

	@Required
	public void setCommonWsService(CommonWsService commonWsService) {
		this.commonWsService = commonWsService;
	}

	protected ConfigurationService getConfigurationService() {
		return configurationService;
	}
	@Required
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	protected SolGroupUtilities getSolGroupUtilities() {
		return solGroupUtilities;
	}

	@Required
	public void setSolGroupUtilities(SolGroupUtilities solGroupUtilities) {
		this.solGroupUtilities = solGroupUtilities;
	}

	protected SolGroupBaseCommerceService getSolGroupBaseCommerceService() {
		return solGroupBaseCommerceService;
	}

	@Required
	public void setSolGroupBaseCommerceService(SolGroupBaseCommerceService solGroupBaseCommerceService) {
		this.solGroupBaseCommerceService = solGroupBaseCommerceService;
	}
	
	
	

}
