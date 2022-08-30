/**
 *
 */
package com.solgroup.services.dataimport.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.services.dataimport.daos.SolGroupImportUnitsDao;

import de.hybris.platform.commerceservices.dataimport.impl.CoreDataImportService;
import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.commerceservices.setup.data.ImportData;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.validation.services.ValidationService;


/**
 * @author salvo
 *
 */
public class SolGroupCoreDataImportService extends CoreDataImportService
{

	private SolGroupImportUnitsDao solGroupImportUnitsDao;
	private ModelService modelService;
	
	@Override
	protected void importCommonData(final String extensionName)
	{
		// Standard Hybris impex
		super.importCommonData(extensionName);
		
		// SolGroup impex
		importUnits(extensionName);
		getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/material.impex", extensionName), true);
		getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/commonCatalogs.impex", extensionName), true);
		getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/legacySystems.impex", extensionName), true);
		getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/commonJobs.impex", extensionName), true);
		getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/groupsAndUsers.impex", extensionName), true);
		getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/customerAttributes.impex", extensionName), true);
		getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/syncCatalog.impex", extensionName), true);
		getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/solrEssentialData.impex", extensionName), true);
		getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/backofficesolrsearch-projectdata-productSolr.impex", extensionName), true);
		getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/visibilityCategory.impex", extensionName), true);

		
	}
	
	@Override
	protected void importStore(final String extensionName, final String storeName, final String productCatalogName)
	{
		
		// Standard Hybris impex
		super.importStore(extensionName, storeName, productCatalogName);
		
		// SolGroup impex
		getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/stores/%s/mediaFolder.impex", extensionName, storeName), true);
		getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/stores/%s/site2Legacy.impex", extensionName, storeName), true);
		getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/stores/%s/cronJobs.impex", extensionName, storeName), true);
		
	}
	
	@Override
	protected void importSolrIndex(final String extensionName, final String storeName)
	{
		super.importSolrIndex(extensionName, storeName);
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/coredata/stores/%s/solrOptimization.impex", extensionName, storeName), false);
	}
	
	

	private void importUnits(final String extensionName)
	{
		// Delete all previous units
		final List<UnitModel> unitsToRemove = getSolGroupImportUnitsDao().findAllUnits();
		if (CollectionUtils.isNotEmpty(unitsToRemove))
		{
			for (final UnitModel unit : unitsToRemove)
			{
				getModelService().remove(unit);
			}
		}

		final String unitsFile = String.format("/%s/import/coredata/common/units.impex", extensionName);
		getSetupImpexService().importImpexFile(unitsFile, false);
	}
	

	
	

	protected SolGroupImportUnitsDao getSolGroupImportUnitsDao()
	{
		return solGroupImportUnitsDao;
	}

	@Required
	public void setSolGroupImportUnitsDao(final SolGroupImportUnitsDao solGroupImportUnitsDao)
	{
		this.solGroupImportUnitsDao = solGroupImportUnitsDao;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}



}
