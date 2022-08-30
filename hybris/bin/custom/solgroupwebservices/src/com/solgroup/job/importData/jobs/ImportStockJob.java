package com.solgroup.job.importData.jobs;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.model.LegacySystemModel;
import com.solgroup.core.model.job.BatchImportCronJobModel;
import com.solgroup.core.service.basecommerce.SolGroupBaseCommerceService;
import com.solgroup.core.service.product.SolGroupProductService;
import com.solgroup.core.ws.service.CommonWsService;
import com.solgroup.core.ws.services.stock.StocksResponse;
import com.solgroup.core.ws.services.stock.StocksResponse.StockProductList.StockProduct;
import com.solgroup.job.importData.abstractJob.AbstractImportJob;
import com.solgroup.service.impexImport.Impex;
import com.solgroup.service.impexImport.ImpexAttribute;
import com.solgroup.service.impexImport.ImpexAttributeModifier;
import com.solgroup.service.impexImport.ImpexHeader;
import com.solgroup.service.impexImport.ImpexRow;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.i18n.daos.CountryDao;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;

public class ImportStockJob extends AbstractImportJob {

	private Logger LOG = Logger.getLogger(this.getClass());

	private Integer threshold;
	private SolGroupBaseCommerceService solGroupBaseCommerceService;
	private SolGroupProductService solGroupProductService;

	@Override
	public PerformResult perform(CronJobModel cronJob) {

		if (cronJob instanceof BatchImportCronJobModel) {

			BatchImportCronJobModel batchImportCronJob = (BatchImportCronJobModel) cronJob;
			MediaModel xmlMedia = batchImportCronJob.getImportMedia();
			InputStream mediaStream = getMediaService().getStreamFromMedia(xmlMedia);
			
			StocksResponse stockResponse = (StocksResponse) getCommonWsService().parseXmlStream_withoutRootNode(StocksResponse.class,
					mediaStream);
			
			Impex stockLevel = createImpexStrategy(stockResponse);
			
			try {
//				createImpex(stockLevel.toImpex());
				
				performImpexImport(stockLevel.toImpex().toString(), batchImportCronJob.getSite(), stockResponse.getHeader().getSourceSystem(), stockResponse.getHeader().getMessageID(), batchImportCronJob.getJob().getCode());

			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
			}

			return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
		}
		return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
	}

	
	private Impex createImpexStrategy(StocksResponse stockResponse) {
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

		WarehouseModel warehouseM = getSolGroupBaseCommerceService()
				.getDefaultWarehouseForLegacySystemAndCountry(stockResponse.getHeader().getSourceSystem(), stockResponse.getCountry());
		CMSSiteModel cmsSite = getCommonWsService().getCMSSite(stockResponse.getHeader().getSourceSystem(), stockResponse.getCountry());

		List<StockProduct> products = stockResponse.getStockProductList().getStockProduct();
		
		
		for (StockProduct i : products) {
			ImpexRow row = new ImpexRow();

			row.put(productCode, getSolGroupProductService().getHybrisProductCode(i.getProductCode(), cmsSite));
			row.put(warehouse, warehouseM.getCode());
			row.put(available, i.getAvailableQty().toString());
			row.put(inStockStatus, evaluateInStockStatus(i.getAvailableQty()).getCode());

			stockLevel.add(row);
		}
		return stockLevel;
	}

	InStockStatus evaluateInStockStatus(BigInteger avalaible) {
		Integer availableI = avalaible.intValue();
		if (availableI > getThreshold())
			return InStockStatus.FORCEINSTOCK;
		else
			return InStockStatus.FORCEOUTOFSTOCK;
	}

	protected Integer getThreshold() {
		return threshold;
	}

	@Required
	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
	}

	protected SolGroupBaseCommerceService getSolGroupBaseCommerceService() {
		return solGroupBaseCommerceService;
	}

	@Required
	public void setSolGroupBaseCommerceService(SolGroupBaseCommerceService solGroupBaseCommerceService) {
		this.solGroupBaseCommerceService = solGroupBaseCommerceService;
	}

	protected SolGroupProductService getSolGroupProductService() {
		return solGroupProductService;
	}

	@Required
	public void setSolGroupProductService(SolGroupProductService solGroupProductService) {
		this.solGroupProductService = solGroupProductService;
	}

	
}
