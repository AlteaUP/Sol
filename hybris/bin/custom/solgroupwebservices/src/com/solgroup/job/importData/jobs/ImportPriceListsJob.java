/**
 *
 */
package com.solgroup.job.importData.jobs;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.common.daos.CommonDao;
import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.dao.b2bunit.SolGroupB2BUnitDao;
import com.solgroup.core.enums.B2BUnitTypeEnum;
import com.solgroup.core.model.LegacySystemModel;
import com.solgroup.core.model.job.BatchImportCronJobModel;
import com.solgroup.core.service.b2bunit.SolGroupB2BUnitService;
import com.solgroup.core.service.product.SolGroupProductService;
import com.solgroup.core.ws.services.customerpricelist.xml.CustomerPriceListsImportHybris;
import com.solgroup.core.ws.services.customerpricelist.xml.CustomerPriceListsImportHybris.CustomerPriceLists;
import com.solgroup.core.ws.services.customerpricelist.xml.CustomerPriceListsImportHybris.CustomerPriceLists.CustomerPriceList;
import com.solgroup.core.ws.services.customerpricelist.xml.CustomerPriceListsImportHybris.CustomerPriceLists.CustomerPriceList.PriceRowList;
import com.solgroup.core.ws.services.customerpricelist.xml.CustomerPriceListsImportHybris.CustomerPriceLists.CustomerPriceList.PriceRowList.PriceRow;
import com.solgroup.job.importData.abstractJob.AbstractImportJob;
import com.solgroup.service.impl.DefaultSolgroupwebservicesService;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.i18n.daos.CurrencyDao;
import de.hybris.platform.servicelayer.user.UserService;

/**
 * @author Roberto
 *
 */
public class ImportPriceListsJob extends AbstractImportJob {

	private UserService userService;
	private SolGroupB2BUnitDao solGroupB2BUnitDao;
	private CurrencyDao currencyDao;
	private SolGroupProductService solGroupProductService;
	private SolGroupB2BUnitService solGroupB2BUnitService;
	

	//private final Logger log = Logger.getLogger(ImportPriceListsJob.class);
	private static final Logger log = LoggerFactory.getLogger(ImportPriceListsJob.class);
	
	private static final String IMPEX_HEADER = "#% impex.setLocale(Locale.ENGLISH);\n" +
			"$productCatalog=$solPrefixProductCatalog\n"
			+ "$catalogVersion=catalogversion(catalog(id[default=$productCatalog]),version[default='Staged'])[unique=true,default='$productCatalog:Staged']\n"
			+ "$prices=Europe1prices[translator=de.hybris.platform.europe1.jalo.impex.Europe1PricesTranslator]\n"
			+ "$taxGroup=Europe1PriceFactory_PTG(code)[default=us-sales-tax-full]\n" + "\n"
			+ "# Set product approval status to Approved only for those products that have prices.\n"
			+ "$approved=approvalstatus(code)[default='approved']";

	private static final String PRICE_GROUP_HEADER = "\n############################################ INSERT USERPRICEGROUP ##################################\n\n"
			+ "INSERT_UPDATE UserPriceGroup;code[unique=true];name[lang=en]";
	private static final String PRICE_ROW_HEADER = "\n############################################ INSERT PRICEROW ##################################\n\n"
			+ "INSERT_UPDATE PriceRow; product(code, $catalogVersion)[unique=true];ug(code)[unique=true];currency(isocode)[unique=true];price;minqtd[default=1];unit(code)[default=pieces];net[default=true];customPrice[default=false];startTime[dateformat='yyyy-MM-dd''T''HH:mm:ss',allownull=true];endTime[dateformat='yyyy-MM-dd''T''HH:mm:ss',allownull=true];$catalogVersion";
	private static final String PRINCIPAL_HEADER = "\n############################################ UPDATE PRINCIPAL ##################################\n\n"
			+ "UPDATE Company;uid[unique=true];userPriceGroup(code,itemtype(code));sessionCurrency(isocode)";
	private static final String IMPEX_SEPARATOR = ";";
	private static final String STRING_RETURN = "\n";

	@Override
	public PerformResult perform(final CronJobModel cronJob) {
		CronJobStatus cronJobStatus;
		CronJobResult cronJobResult;

		if (cronJob instanceof BatchImportCronJobModel) {

			log.info("Start importPriceList job. User is " + getUserService().getCurrentUser().getUid());
			
			// Define simpleDateFormat and timeZone
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			SimpleDateFormat sdfWithTimeZone = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			GregorianCalendar gregorianCalendar = new GregorianCalendar();
			TimeZone currentTimeZone = gregorianCalendar.getTimeZone();
			sdf.setTimeZone(currentTimeZone);
			sdfWithTimeZone.setTimeZone(currentTimeZone);


			final BatchImportCronJobModel batchImportCronJob = (BatchImportCronJobModel) cronJob;
			final MediaModel xmlMedia = batchImportCronJob.getImportMedia();
			final InputStream mediaStream = getMediaService().getStreamFromMedia(xmlMedia);
			final CustomerPriceListsImportHybris importData = (CustomerPriceListsImportHybris) getCommonWsService()
					.parseXmlStream(CustomerPriceListsImportHybris.class, mediaStream);

			// Retrieve source system
			final CMSSiteModel cmsSite = batchImportCronJob.getSite();
			LegacySystemModel sourceSystem = batchImportCronJob.getLegacySystem();
			log.info("Source system = " + sourceSystem.getCode());

			// Retrieve catalog version (Staged)
			final CatalogVersionModel catalogVersion = cmsSite.getImportProductCatalog();

			// Iterate over all baseProducts sent in message
			final CustomerPriceLists customerPriceLists = importData.getCustomerPriceLists();

			StringBuilder importPriceGroups = new StringBuilder();
			StringBuilder importPriceRows = new StringBuilder();
			StringBuilder importPrincipal = new StringBuilder();

			// Initialize StringBuilder for Price Group
			importPriceGroups.append(PRICE_GROUP_HEADER);
			importPriceGroups.append(STRING_RETURN);

			// Initialize StringBuilder for Price Row
			importPriceRows.append(PRICE_ROW_HEADER);
			importPriceRows.append(STRING_RETURN);

			// Initialize StringBuilder for Principal
			importPrincipal.append(PRINCIPAL_HEADER);
			importPrincipal.append(STRING_RETURN);

			String solrCatalogPrefix = "$solPrefix=" + cmsSite.getUid() + "\n";

			for (final CustomerPriceList customerPriceList : customerPriceLists.getCustomerPriceList()) {

				boolean isDefault = false;

				String legacyCode = customerPriceList.getLegacyCode();
				String companyLegacyCode = customerPriceList.getCompanyLegacyCode();

				if (legacyCode.isEmpty()) {
					isDefault = true;
				}

				String upgCode = "";
				String upgDescr = "";
				String b2bUnitCode = "";

				if (!isDefault) {
					//CompanyModel company = solGroupB2BUnitDao.findCompany(companyLegacyCode, cmsSite);
					//b2bUnitCode = solGroupB2BUnitDao.findB2BUnit(company, legacyCode, cmsSite).getUid();
					b2bUnitCode = getSolGroupB2BUnitService().getHybrisCode(companyLegacyCode, legacyCode, cmsSite, B2BUnitTypeEnum.POINT_OF_SALE);
					B2BUnitModel b2bUnitModel = getCommonDao().findItemByUniqueCode(B2BUnitModel._TYPECODE, B2BUnitModel.UID, b2bUnitCode, B2BUnitModel.class);
					if(b2bUnitModel==null) {
						b2bUnitCode = getSolGroupB2BUnitService().getHybrisCode(companyLegacyCode, legacyCode, cmsSite, B2BUnitTypeEnum.ORGANIZATION_2LEVEL);
						b2bUnitModel = getCommonDao().findItemByUniqueCode(B2BUnitModel._TYPECODE, B2BUnitModel.UID, b2bUnitCode, B2BUnitModel.class);
						if(b2bUnitModel==null) {
							b2bUnitCode = getSolGroupB2BUnitService().getHybrisCode(companyLegacyCode, legacyCode, cmsSite, B2BUnitTypeEnum.ORGANIZATION_1LEVEL);
							b2bUnitModel = getCommonDao().findItemByUniqueCode(B2BUnitModel._TYPECODE, B2BUnitModel.UID, b2bUnitCode, B2BUnitModel.class);
						}
					}
					
					if(b2bUnitModel == null) {
						log.error("No b2bUnit found for erpCode " + legacyCode + " and company " + companyLegacyCode);
						continue;
					}
					
					
					upgCode = "CPL_" + cmsSite.getUid() + "_" + b2bUnitCode;
					upgCode = upgCode.replaceAll(" ", "_");
					upgDescr = "CPL " + cmsSite.getUid() + " " + b2bUnitCode;
				}

				String currency = customerPriceList.getCurrency();
				
				if(StringUtils.isNotEmpty(upgCode)) {
					importPriceGroups.append(IMPEX_SEPARATOR + upgCode + IMPEX_SEPARATOR + upgDescr + STRING_RETURN);
				}
				if(StringUtils.isNotEmpty(b2bUnitCode)) {
					importPrincipal.append(IMPEX_SEPARATOR + b2bUnitCode);
					importPrincipal.append(IMPEX_SEPARATOR + upgCode + ":UserPriceGroup");
					importPrincipal.append(IMPEX_SEPARATOR + currency + IMPEX_SEPARATOR);
					importPrincipal.append(STRING_RETURN);
				}

				// Create price row
				final PriceRowList priceRowList = customerPriceList.getPriceRowList();

				
				for (final PriceRow priceRow : priceRowList.getPriceRow()) {

					importPriceRows.append(IMPEX_SEPARATOR);
					//importPriceRows.append(priceRow.getProductCode() + ":" + catalogVersion.getCatalog().getId() + ":" + catalogVersion.getVersion() + IMPEX_SEPARATOR);
					importPriceRows.append(getSolGroupProductService().getHybrisProductCode(priceRow.getProductCode(), cmsSite));
					importPriceRows.append(IMPEX_SEPARATOR);
					importPriceRows.append(upgCode + IMPEX_SEPARATOR);
					importPriceRows.append(currency + IMPEX_SEPARATOR);

					List<CurrencyModel> currencies = getCurrencyDao().findCurrenciesByCode(currency);
					CurrencyModel currencyModel = null;
					if (CollectionUtils.isNotEmpty(currencies)) {
						currencyModel = currencies.get(0);
					}

					String digits = "00";
					if (currencyModel != null && currencyModel.getDigits() != null
							&& currencyModel.getDigits().intValue() > 2) {
						for (int i = 2; i < currencyModel.getDigits().intValue(); i++) {
							digits = digits + "#	";
						}
					}

					DecimalFormatSymbols symbols = new DecimalFormatSymbols();
					symbols.setDecimalSeparator('.');
					DecimalFormat df = new DecimalFormat("#." + digits, symbols);
					importPriceRows.append(df.format(priceRow.getPrice()) + IMPEX_SEPARATOR);

					importPriceRows.append(priceRow.getScale() + IMPEX_SEPARATOR);
					importPriceRows.append(priceRow.getUm() + IMPEX_SEPARATOR);
					importPriceRows.append(IMPEX_SEPARATOR);
					if (!isDefault) {
						importPriceRows.append("true" + IMPEX_SEPARATOR);
					} else {
						importPriceRows.append("false" + IMPEX_SEPARATOR);
					}
					

					String startDate_toImpex = "";
					String endDate_toImpex = "";

					if(priceRow.getStartDate()!=null) {
						startDate_toImpex = priceRow.getStartDate().toString();
					}
					
					if(priceRow.getEndDate()!=null) {
						endDate_toImpex = priceRow.getEndDate().toString();
					}
					
//					if (priceRow.getStartDate() != null && priceRow.getEndDate() != null) {
//						
//						try {
//							String endDateStr_fromLegacy = priceRow.getEndDate().toString();
//							Date endDate = sdf.parse(endDateStr_fromLegacy);
//							endDate_toImpex = sdfWithTimeZone.format(endDate);
//						}catch(Exception exc) {
//							log.error(exc.getMessage(),exc);
//						}
//						
//						System.err.println(startDate_toImpex);
//						System.err.println(endDate_toImpex);
//
//						
//					}

					importPriceRows.append(startDate_toImpex);
					importPriceRows.append(IMPEX_SEPARATOR);
					importPriceRows.append(endDate_toImpex);
					importPriceRows.append(IMPEX_SEPARATOR);
					importPriceRows.append(STRING_RETURN);

					
//					importPriceRows.append(IMPEX_SEPARATOR);
//
//					importPriceRows.append(
//							catalogVersion.getCatalog().getId() + ":" + catalogVersion.getVersion() + STRING_RETURN);

				}

			} // end for to iterate over all price lists

			StringBuilder importImpex = new StringBuilder();

			importImpex.append(solrCatalogPrefix + IMPEX_HEADER);

			importImpex.append(importPriceGroups);
			importImpex.append(importPrincipal);

			importImpex.append(importPriceRows);

			
//			if(log.isDebugEnabled()) {
//				log.debug(importImpex.toString());
//			}

			log.info(importImpex.toString());
			
			try {
				
				performImpexImport(importImpex.toString(), cmsSite, importData.getHeader().getSourceSystem(), importData.getHeader().getMessageID(), batchImportCronJob.getJob().getCode());
				
				cronJobStatus = CronJobStatus.FINISHED;
				cronJobResult = CronJobResult.SUCCESS;

				
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				cronJobStatus = CronJobStatus.FINISHED;
				cronJobResult = CronJobResult.ERROR;
			}
			


		} else {
			log.error("CronJob " + cronJob.getCode() + " is not an instance of BatchImportCronJobModel");
			cronJobStatus = CronJobStatus.FINISHED;
			cronJobResult = CronJobResult.ERROR;
		}

		return new PerformResult(cronJobResult, cronJobStatus);
	}



	protected CurrencyDao getCurrencyDao() {
		return currencyDao;
	}

	@Required
	public void setCurrencyDao(CurrencyDao currencyDao) {
		this.currencyDao = currencyDao;
	}

	protected UserService getUserService() {
		return userService;
	}
	@Required
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	protected SolGroupB2BUnitDao getSolGroupB2BUnitDao() {
		return solGroupB2BUnitDao;
	}
	@Required
	public void setSolGroupB2BUnitDao(SolGroupB2BUnitDao solGroupB2BUnitDao) {
		this.solGroupB2BUnitDao = solGroupB2BUnitDao;
	}

	protected SolGroupProductService getSolGroupProductService() {
		return solGroupProductService;
	}

	@Required
	public void setSolGroupProductService(SolGroupProductService solGroupProductService) {
		this.solGroupProductService = solGroupProductService;
	}



	public SolGroupB2BUnitService getSolGroupB2BUnitService() {
		return solGroupB2BUnitService;
	}



	public void setSolGroupB2BUnitService(SolGroupB2BUnitService solGroupB2BUnitService) {
		this.solGroupB2BUnitService = solGroupB2BUnitService;
	}
	
	

    
}
