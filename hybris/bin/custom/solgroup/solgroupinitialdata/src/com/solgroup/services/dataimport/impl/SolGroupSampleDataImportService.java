/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.solgroup.services.dataimport.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.constants.SolgroupCoreConstants;

import de.hybris.platform.commerceservices.dataimport.impl.SampleDataImportService;
import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.commerceservices.setup.data.ImportData;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.CronJobService;


/**
 * Implementation to handle specific Sample Data Import services to Powertools.
 */
public class SolGroupSampleDataImportService extends SampleDataImportService
{

	private CronJobService cronJobService;
	
	@Override
	protected void importAllData(final AbstractSystemSetup systemSetup, final SystemSetupContext context,
			final ImportData importData, final boolean syncCatalogs)
	{
		
		if(BooleanUtils.isTrue(importData.getImportSampleDataCommons())) {
			systemSetup.logInfo(context, String.format("Begin importing common data for [%s]", context.getExtensionName()));	
			importCommonData(context.getExtensionName());
		}
		
		
		if(BooleanUtils.isTrue(importData.getImportSampleDataProducts())) {
			systemSetup.logInfo(context,String.format("Begin importing product catalog data for [%s]", importData.getProductCatalogName()));
			importProductCatalog(context.getExtensionName(), importData.getProductCatalogName());
		}
		
		if(BooleanUtils.isTrue(importData.getImportSampleDataProductsLegacySystem())) {
			for(String legacySystem : importData.getLegacySystems()) {
				systemSetup.logInfo(context,String.format("Begin importing product catalog data for legacy [%s]", legacySystem));
				importProductCatalogFromLegacy(context.getExtensionName(), importData.getProductCatalogName(), legacySystem);
			}
		}
		
		
		if(BooleanUtils.isTrue(importData.getImportSampleDataContentCatalog())) {
			for (final String contentCatalogName : importData.getContentCatalogNames())
			{
				systemSetup.logInfo(context, String.format("Begin importing content catalog data for [%s]", contentCatalogName));
				importContentCatalog(context.getExtensionName(), contentCatalogName);
			}
		}
		
		if(BooleanUtils.isTrue(importData.getImportSampleDataCustomers())) {
			for(String legacySystem : importData.getLegacySystems()) {
				systemSetup.logInfo(context, String.format("Begin importing customer data for [%s]", legacySystem));
				importCommerceOrgData(context, importData.getStoreNames().get(0),legacySystem);
			}
			
		}

		if(BooleanUtils.isTrue(importData.getImportSampleDataPrices())) {
			for(String legacySystem : importData.getLegacySystems()) {
				systemSetup.logInfo(context, String.format("Begin importing price data for [%s]", legacySystem));
				importPrices(context, importData.getStoreNames().get(0), legacySystem);
			}
		}

		if(BooleanUtils.isTrue(importData.getImportSampleDataStocks())) {
			for(String legacySystem : importData.getLegacySystems()) {
				systemSetup.logInfo(context, String.format("Begin importing stock data for [%s]", legacySystem));
				importStocks(context, importData.getStoreNames().get(0), legacySystem);
			}
		}
		
		if(BooleanUtils.isTrue(importData.getImportSampleDataAgents())) {
			systemSetup.logInfo(context, "Begin importing agents data");
			importAgents(context.getExtensionName(), importData.getStoreNames().get(0));
		}
		

		if(BooleanUtils.isTrue(importData.getImportSampleDataProducts()) || BooleanUtils.isTrue(importData.getImportSampleDataProductsLegacySystem())) {
			synchronizeProductCatalog(systemSetup, context, importData.getProductCatalogName(), false);
		}
		
		if( BooleanUtils.isTrue(importData.getImportSampleDataContentCatalog())) {
			for (final String contentCatalog : importData.getContentCatalogNames())
			{
				synchronizeContentCatalog(systemSetup, context, contentCatalog, false);
			}
		}

		if( 
				((BooleanUtils.isTrue(importData.getImportSampleDataContentCatalog()) || BooleanUtils.isTrue(importData.getImportSampleDataProductsLegacySystem())) ) && 
				BooleanUtils.isTrue(importData.getImportSampleDataContentCatalog())) {
			
			assignDependent(importData.getProductCatalogName(), importData.getContentCatalogNames());
			if (syncCatalogs)
			{
				systemSetup
						.logInfo(context, String.format("Synchronizing product catalog for [%s]", importData.getProductCatalogName()));
				final boolean productSyncSuccess = synchronizeProductCatalog(systemSetup, context, importData.getProductCatalogName(),
						true);

				for (final String contentCatalogName : importData.getContentCatalogNames())
				{
					systemSetup.logInfo(context, String.format("Synchronizing content catalog for [%s]", contentCatalogName));
					synchronizeContentCatalog(systemSetup, context, contentCatalogName, true);
				}

				if (!productSyncSuccess)
				{
					// Rerun the product sync if required
					systemSetup.logInfo(context,
							String.format("Rerunning product catalog synchronization for [%s]", importData.getProductCatalogName()));
					if (!synchronizeProductCatalog(systemSetup, context, importData.getProductCatalogName(), true))
					{
						systemSetup.logInfo(context, String.format(
										"Rerunning product catalog synchronization for [%s], failed. Please consult logs for more details.",
										importData.getProductCatalogName()));
					}
				}
			}
		}
		

		if(BooleanUtils.isTrue(importData.getImportSampleDataCommons())) {
			for (final String storeName : importData.getStoreNames())
			{
				systemSetup.logInfo(context, String.format("Begin importing store data for [%s]", storeName));
				importStore(context.getExtensionName(), storeName, importData.getProductCatalogName());
	
				systemSetup.logInfo(context, String.format("Begin importing job data for [%s]", storeName));
				importJobs(context.getExtensionName(), storeName);
	
				systemSetup.logInfo(context, String.format("Begin importing solr index data for [%s]", storeName));
				importSolrIndex(context.getExtensionName(), storeName);
	
				if (systemSetup.getBooleanSystemSetupParameter(context, ACTIVATE_SOLR_CRON_JOBS))
				{
					systemSetup.logInfo(context, String.format("Activating solr index for [%s]", storeName));
					runSolrIndex(context.getExtensionName(), storeName);
				}
			}
		}
	}	
	
	
	
	protected void importProductCatalog(final String extensionName, final String productCatalogName)
	{

		// Load taxes
		getSetupImpexService().importImpexFile(String.format("/%s/import/sampledata/taxes/%s/taxes.impex", extensionName, productCatalogName),
				false);

		
		// Load classification units
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/classifications-units.impex", extensionName,
						productCatalogName), false);

		// Load base categories
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/1 - categories.impex", extensionName,
						productCatalogName), false);
		
		// Load application categories
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/2 - applicationCategories.impex", extensionName,
						productCatalogName), false);
		
		// Load topBarCategories
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/3 - topBarCategories.impex", extensionName,
						productCatalogName), false);
		
		// Load dimension categories
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/4 - dimension-categories.impex", extensionName,
						productCatalogName), false);
		
		// Load classification categories
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/5 - categories-classifications.impex", extensionName,
						productCatalogName), false);
		
	}

	protected void importProductCatalogFromLegacy(final String extensionName, final String productCatalogName, String legacySystem) {

			// Load bacth import products
			getSetupImpexService().importImpexFile(
					String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/%s/6 - batchImportProducts.impex", extensionName,
							productCatalogName, legacySystem), false);
			
			// Perform import product job
			CronJobModel productImportCronJob = null;
			try {
				productImportCronJob = getCronJobService().getCronJob(productCatalogName + "_importProductJob_" + legacySystem + "_0");
			} catch(Exception exc) {}
			
			if(productImportCronJob != null) {
				getCronJobService().performCronJob(productImportCronJob, true);
			}
			
			// Load product data
			getSetupImpexService().importImpexFile(
					String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/%s/7 - products.impex", extensionName,
							productCatalogName, legacySystem), false);
			
			// Load product medias
			getSetupImpexService().importImpexFile(
					String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/%s/8 - products-media.impex", extensionName,
							productCatalogName, legacySystem), false);
			
			// Load product classification attributes
			getSetupImpexService().importImpexFile(
					String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/%s/9 - productsClassification.impex", extensionName,
							productCatalogName, legacySystem), false);
			
//			// Load product visibility
//			getSetupImpexService().importImpexFile(
//					String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/%s/10 - productsVisibility.impex", extensionName,
//							productCatalogName, legacySystem), false);
			
			// Load approval products
			getSetupImpexService().importImpexFile(
					String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/%s/productsApproval.impex", extensionName,
							productCatalogName, legacySystem), false);
		
		
	}
	
	
	
	protected void importCommerceOrgData(final SystemSetupContext context, String storeName, String legacySystem)
	{
		final String extensionName = context.getExtensionName();

		// Load batch import customers
		getSetupImpexService().importImpexFile(String.format("/%s/import/sampledata/commerceorg/%s/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/%s/1 - batchImportCustomers.impex", extensionName, storeName,legacySystem),
				false);
		
		// Perform customer import job
		CronJobModel customerImportCronJob = null;
		try {
			customerImportCronJob = getCronJobService().getCronJob(storeName + "_importCustomerJob_" + legacySystem + "_0");
		}catch(Exception exc) {}
		if(customerImportCronJob != null) {
			getCronJobService().performCronJob(customerImportCronJob, true);
		}
		
		// Load customers activation
		getSetupImpexService().importImpexFile(String.format("/%s/import/sampledata/commerceorg/%s/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/%s/2 - customerActivation.impex", extensionName, storeName, legacySystem),
				false);

	}
	
	protected void importPrices(final SystemSetupContext context, String storeName, String legacySystem) {
		final String extensionName = context.getExtensionName();

		// Load batch import prices
		getSetupImpexService().importImpexFile(String.format("/%s/import/sampledata/prices/%s/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/%s/batchImportPrices.impex", extensionName, storeName, legacySystem),
				false);
		
		// Perform price import job
		CronJobModel priceImportCronJob = null;
		try {
			priceImportCronJob = getCronJobService().getCronJob(storeName + "_importPriceListsJob_" + legacySystem + "_0");
		}catch(Exception exc) {}
		if(priceImportCronJob != null) {
			getCronJobService().performCronJob(priceImportCronJob, true);
		}
	}

	protected void importStocks(final SystemSetupContext context, String storeName, String legacySystem) {
		final String extensionName = context.getExtensionName();

		// Load batch import stocks
		getSetupImpexService().importImpexFile(String.format("/%s/import/sampledata/stocks/%s/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/%s/batchImportStocks.impex", extensionName, storeName, legacySystem),
				false);
		
		// Perform stocks import job
		CronJobModel stockImportCronJob = null;
		try {
			stockImportCronJob = getCronJobService().getCronJob(storeName + "_importStockJob_" + legacySystem + "_0");
		}catch(Exception exc) {}
		if(stockImportCronJob != null) {
			getCronJobService().performCronJob(stockImportCronJob, true);
		}
	}

	
	@Override
	protected void importCommonData(final String extensionName) {
		super.importCommonData(extensionName);
		
		// Load asmCustomerList
		getSetupImpexService().importImpexFile(String.format("/%s/import/sampledata/backoffice/customersupport/asmCustomerList.impex", extensionName),
				false);
		
	}
	
	protected void importAgents(final String extensionName, final String storeName) {

		// Load agents
		getSetupImpexService().importImpexFile(String.format("/%s/import/sampledata/agents/%s/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/agents.impex", extensionName, storeName),
				false);
		
	}
	
	@Override
	protected void importSolrIndex(final String extensionName, final String storeName)
	{
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/stores/%s/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/solr.impex", extensionName, storeName), false);
		
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/stores/%s/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/solr_applicationCategories.impex", extensionName, storeName), false);
		

		getSetupSolrIndexerService().createSolrIndexerCronJobs(String.format("%sIndex", storeName));
	}

	
	@Override
	protected void importContentCatalog(final String extensionName, final String contentCatalogName)
	{
		
		super.importContentCatalog(extensionName, contentCatalogName);
		
		// Import topBar categories structure
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/contentCatalogs/%sContentCatalog/" + getConfigurationService().getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CURRENT_ENV, SolgroupCoreConstants.PROPERTY_DEFAULT_ENV) + "/topBarCategories_structure.impex",
						extensionName, contentCatalogName),
				false);

	}
	
	

	



	protected CronJobService getCronJobService() {
		return cronJobService;
	}

	@Required
	public void setCronJobService(CronJobService cronJobService) {
		this.cronJobService = cronJobService;
	}
	
	
	
	
	
}
