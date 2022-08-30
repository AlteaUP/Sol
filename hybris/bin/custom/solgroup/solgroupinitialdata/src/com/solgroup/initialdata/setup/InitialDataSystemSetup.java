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
package com.solgroup.initialdata.setup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.solgroup.initialdata.constants.SolgroupInitialDataConstants;
import com.solgroup.services.dataimport.impl.SolGroupCoreDataImportService;
import com.solgroup.services.dataimport.impl.SolGroupSampleDataImportService;

import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.commerceservices.setup.data.ImportData;
import de.hybris.platform.commerceservices.setup.events.CoreDataImportedEvent;
import de.hybris.platform.commerceservices.setup.events.SampleDataImportedEvent;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetup.Process;
import de.hybris.platform.core.initialization.SystemSetup.Type;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;
import de.hybris.platform.core.initialization.SystemSetupParameterMethod;
import de.hybris.platform.servicelayer.config.ConfigurationService;

/**
 * This class provides hooks into the system's initialization and update processes.
 *
 * @see "https://wiki.hybris.com/display/release4/Hooks+for+Initialization+and+Update+Process"
 */
@SystemSetup(extension = SolgroupInitialDataConstants.EXTENSIONNAME)
public class InitialDataSystemSetup extends AbstractSystemSetup {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(InitialDataSystemSetup.class);

    private static final String IMPORT_CORE_DATA = "importCoreData";
    private static final String IMPORT_SAMPLE_DATA = "importSampleData";
    private static final String ACTIVATE_SOLR_CRON_JOBS = "activateSolrCronJobs";
    private static final String NOTIFY_EVENT = "notifyEvent";
    private static final String IMPORT_SAMPLE_DATA_PRODUCTS = "importSampleDataProducts";
    private static final String IMPORT_SAMPLE_DATA_PRODUCTS_LEGACY = "importSampleDataProductsLegacy";
    private static final String IMPORT_SAMPLE_DATA_CUSTOMERS = "importSampleDataCustomers";
    private static final String IMPORT_SAMPLE_DATA_PRICES = "importSampleDataPrices";
    private static final String IMPORT_SAMPLE_DATA_STOCKS = "importSampleDataStocks";
    private static final String IMPORT_SAMPLE_DATA_CONTENT_CATALOG = "importSampleDataContentCatalog";
    private static final String IMPORT_SAMPLE_DATA_COMMONS = "importSampleDataCommons";
    private static final String IMPORT_SAMPLE_DATA_AGENTS = "importSampleDataAgents";

    private static final String IMPORT_SOLGROUP_IT = "importSolgroupIT";
    private static final String IMPORT_SOLGROUP_FR = "importSolgroupFR";
    private static final String IMPORT_SOLGROUP_AT = "importSolgroupAT";

    private static final String SOLGROUP_IT_LEGACY_SYSTEMS = "solgroup_IT_legacySystem";
    private static final String SOLGROUP_FR_LEGACY_SYSTEMS = "solgroup_FR_legacySystem";
    private static final String SOLGROUP_AT_LEGACY_SYSTEMS = "solgroup_AT_legacySystem";

    public static final String SOLGROUP_IT = "solgroupIT";
    public static final String SOLGROUP_FR = "solgroupFR";
    public static final String SOLGROUP_AT = "solgroupAT";
    
    private static final String IMPORTCOCKPIT_SETUP = "importCockpitSetup";

    private SolGroupCoreDataImportService solgroupCoreDataImportService;
    private SolGroupSampleDataImportService solgroupSampleDataImportService;
    private ConfigurationService configurationService;

    public SystemSetupParameter createStringSystemSetupParameter(final String label, final String key,
            List<String> values) {
        final SystemSetupParameter syncProductsParam = new SystemSetupParameter(key);
        syncProductsParam.setLabel(label);
        for (int i = 0; i < values.size(); i++) {
            if (i == 0) {
                syncProductsParam.addValue(values.get(i), true);
            } else {
                syncProductsParam.addValue(values.get(i), false);
            }
        }

        return syncProductsParam;
    }

    public String getStringSystemSetupParameter(final SystemSetupContext context, final String key) {
        final String parameterValue = context.getParameter(context.getExtensionName() + "_" + key);
        if (parameterValue != null && !parameterValue.isEmpty()) {
            return parameterValue;
        } else {
            return getDefaultValueForStringSystemSetupParameter(key);
        }
    }

    public String getDefaultValueForStringSystemSetupParameter(final String key) {
        final List<SystemSetupParameter> initializationOptions = getInitializationOptions();
        if (initializationOptions != null) {
            for (final SystemSetupParameter option : initializationOptions) {
                if (key.equals(option.getKey())) {
                    final String[] defaults = option.getDefaults();
                    if (defaults != null && defaults.length > 0) {
                        return defaults[0];
                    }
                }
            }
        }
        return "";
    }

    /**
     * Generates the Dropdown and Multi-select boxes for the project data import
     */
    @Override
    @SystemSetupParameterMethod
    public List<SystemSetupParameter> getInitializationOptions() {
        final List<SystemSetupParameter> params = new ArrayList<SystemSetupParameter>();

        params.add(createBooleanSystemSetupParameter(IMPORT_SOLGROUP_IT, "Import SOLGroup IT", true));
        params.add(createBooleanSystemSetupParameter(IMPORT_SOLGROUP_FR, "Import SOLGroup FR", false));
        params.add(createBooleanSystemSetupParameter(IMPORT_SOLGROUP_AT, "Import SOLGroup AT", false));

        List<String> solgroupIT_legacySystems = Lists.newArrayList();
        List<String> solgroupFR_legacySystems = Lists.newArrayList();
        List<String> solgroupAT_legacySystems = Lists.newArrayList();
        solgroupIT_legacySystems.add("RAMSES_IT,RAINBOW");
        solgroupIT_legacySystems.add("RAMSES_IT");
        solgroupIT_legacySystems.add("RAINBOW");
        solgroupFR_legacySystems.add("RAMSES_FR,RAINBOW");
        solgroupFR_legacySystems.add("RAMSES_FR");
        solgroupFR_legacySystems.add("RAINBOW");
        solgroupAT_legacySystems.add("RAMSES_AT,RAINBOW");
        solgroupAT_legacySystems.add("RAMSES_AT");
        solgroupAT_legacySystems.add("RAINBOW");
        params.add(createStringSystemSetupParameter("SOLGroup IT legacy systems", SOLGROUP_IT_LEGACY_SYSTEMS,
                solgroupIT_legacySystems));
        params.add(createStringSystemSetupParameter("SOLGroup FR legacy systems", SOLGROUP_FR_LEGACY_SYSTEMS,
                solgroupFR_legacySystems));
        params.add(createStringSystemSetupParameter("SOLGroup AT legacy systems", SOLGROUP_AT_LEGACY_SYSTEMS,
                solgroupAT_legacySystems));

        params.add(createBooleanSystemSetupParameter(IMPORT_CORE_DATA, "1.1 Import Core Data", true));
        params.add(createBooleanSystemSetupParameter(IMPORT_SAMPLE_DATA, "1.2 Import Sample Data", true));

        String env = getConfigurationService().getConfiguration().getString("solgroupEnv");
        boolean loadSampleProducts = getConfigurationService().getConfiguration()
                .getBoolean("solgroupEnv.initialize." + env + ".loadSample.products");
        boolean loadSampleCustomers = getConfigurationService().getConfiguration()
                .getBoolean("solgroupEnv.initialize." + env + ".loadSample.customers");
        boolean loadSamplePrices = getConfigurationService().getConfiguration()
                .getBoolean("solgroupEnv.initialize." + env + ".loadSample.prices");
        boolean loadSampleStocks = getConfigurationService().getConfiguration()
                .getBoolean("solgroupEnv.initialize." + env + ".loadSample.stocks");
        boolean loadSampleAgents = getConfigurationService().getConfiguration()
                .getBoolean("solgroupEnv.initialize." + env + ".loadSample.agents");

        params.add(
                createBooleanSystemSetupParameter(IMPORT_SAMPLE_DATA_COMMONS, "1.2.1 Import Common Sample Data", true));
        params.add(createBooleanSystemSetupParameter(IMPORT_SAMPLE_DATA_PRODUCTS,
                "1.2.2 Import Sample Data for products", loadSampleProducts));
        params.add(createBooleanSystemSetupParameter(IMPORT_SAMPLE_DATA_PRODUCTS_LEGACY,
                "1.2.3 Import Sample Data for products from legacy systems", loadSampleProducts));
        params.add(createBooleanSystemSetupParameter(IMPORT_SAMPLE_DATA_CONTENT_CATALOG,
                "1.2.4 Import Sample Data for content catalog", true));
        params.add(createBooleanSystemSetupParameter(IMPORT_SAMPLE_DATA_CUSTOMERS,
                "1.2.5 Import Sample Data for customers from legacy systems", loadSampleCustomers));
        params.add(createBooleanSystemSetupParameter(IMPORT_SAMPLE_DATA_PRICES,
                "1.2.6 Import Sample Data for prices from legacy systems", loadSamplePrices));
        params.add(createBooleanSystemSetupParameter(IMPORT_SAMPLE_DATA_STOCKS,
                "1.2.7 Import Sample Data for stocks from legacy systems", loadSampleStocks));
        params.add(createBooleanSystemSetupParameter(IMPORT_SAMPLE_DATA_AGENTS, "1.2.8 Import Sample Data agents",
                loadSampleAgents));

        params.add(createBooleanSystemSetupParameter(ACTIVATE_SOLR_CRON_JOBS, "2.0 Activate Solr Cron Jobs", true));
        params.add(createBooleanSystemSetupParameter(NOTIFY_EVENT, "3.0 Notify event", true));
        
        // importcockpit setup
        params.add(createBooleanSystemSetupParameter(IMPORTCOCKPIT_SETUP, "4.0 Import Cockpit setup", false));

        return params;
    }

    /**
     * Implement this method to create initial objects. This method will be called by system creator during
     * initialization and system update. Be sure that this method can be called repeatedly.
     *
     * @param context
     *            the context provides the selected parameters and values
     */
    @SystemSetup(type = Type.ESSENTIAL, process = Process.ALL)
    public void createEssentialData(final SystemSetupContext context) {
    }

    /**
     * Implement this method to create data that is used in your project. This method will be called during the system
     * initialization. <br>
     * Add import data for each site you have configured
     *
     * <pre>
     * final List<ImportData> importData = new ArrayList<ImportData>();
     *
     * final ImportData sampleImportData = new ImportData();
     * sampleImportData.setProductCatalogName(SAMPLE_PRODUCT_CATALOG_NAME);
     * sampleImportData.setContentCatalogNames(Arrays.asList(SAMPLE_CONTENT_CATALOG_NAME));
     * sampleImportData.setStoreNames(Arrays.asList(SAMPLE_STORE_NAME));
     * importData.add(sampleImportData);
     *
     * getCoreDataImportService().execute(this, context, importData);
     * getEventService().publishEvent(new CoreDataImportedEvent(context, importData));
     *
     * getSampleDataImportService().execute(this, context, importData);
     * getEventService().publishEvent(new SampleDataImportedEvent(context, importData));
     * </pre>
     *
     * @param context
     *            the context provides the selected parameters and values
     */
    @SystemSetup(type = Type.PROJECT, process = Process.ALL)
    public void createProjectData(final SystemSetupContext context) {

        Boolean notifyEvent = getBooleanSystemSetupParameter(context, NOTIFY_EVENT);

        Map<String, List<String>> storesToImport = Maps.newHashMap();

        // Import solGroup IT
        if (getBooleanSystemSetupParameter(context, IMPORT_SOLGROUP_IT)) {
            storesToImport.put(SOLGROUP_IT, new ArrayList<String>());
            String legacySystems[] = getStringSystemSetupParameter(context, SOLGROUP_IT_LEGACY_SYSTEMS).split(",");
            for (int i = 0; i < legacySystems.length; i++) {
                storesToImport.get(SOLGROUP_IT).add(legacySystems[i]);
            }
        }

        // Import solGroup FR
        if (getBooleanSystemSetupParameter(context, IMPORT_SOLGROUP_FR)) {
            storesToImport.put(SOLGROUP_FR, new ArrayList<String>());
            String legacySystems[] = getStringSystemSetupParameter(context, SOLGROUP_FR_LEGACY_SYSTEMS).split(",");
            for (int i = 0; i < legacySystems.length; i++) {
                storesToImport.get(SOLGROUP_FR).add(legacySystems[i]);
            }
        }

        // Import solGroup AT
        if (getBooleanSystemSetupParameter(context, IMPORT_SOLGROUP_AT)) {
            storesToImport.put(SOLGROUP_AT, new ArrayList<String>());
            String legacySystems[] = getStringSystemSetupParameter(context, SOLGROUP_AT_LEGACY_SYSTEMS).split(",");
            for (int i = 0; i < legacySystems.length; i++) {
                storesToImport.get(SOLGROUP_AT).add(legacySystems[i]);
            }
        }

        boolean importSampleDataCommons = getBooleanSystemSetupParameter(context, IMPORT_SAMPLE_DATA_COMMONS);
        boolean importSampleDataProducts = getBooleanSystemSetupParameter(context, IMPORT_SAMPLE_DATA_PRODUCTS);
        boolean importSampleDataProductsFromLegacySystem = getBooleanSystemSetupParameter(context,
                IMPORT_SAMPLE_DATA_PRODUCTS_LEGACY);
        boolean importSampleDataCustomers = getBooleanSystemSetupParameter(context, IMPORT_SAMPLE_DATA_CUSTOMERS);
        boolean importSampleDataPrices = getBooleanSystemSetupParameter(context, IMPORT_SAMPLE_DATA_PRICES);
        boolean importSampleDataStocks = getBooleanSystemSetupParameter(context, IMPORT_SAMPLE_DATA_STOCKS);
        boolean importSampleDataContentCatalog = getBooleanSystemSetupParameter(context,
                IMPORT_SAMPLE_DATA_CONTENT_CATALOG);
        boolean importSampleDataAgents = getBooleanSystemSetupParameter(context, IMPORT_SAMPLE_DATA_AGENTS);
        boolean importCockpitInitialData = getBooleanSystemSetupParameter(context, IMPORTCOCKPIT_SETUP);
        
        if (CollectionUtils.isNotEmpty(storesToImport.keySet())) {
            for (final String storeName : storesToImport.keySet()) {
                logInfo(context, "Begin import for " + storeName);

                final List<ImportData> importData = new ArrayList<ImportData>();

                final ImportData hybrisImportData = new ImportData();

                hybrisImportData.setProductCatalogName(storeName);
                hybrisImportData.setContentCatalogNames(Arrays.asList(storeName));
                hybrisImportData.setStoreNames(Arrays.asList(storeName));
                hybrisImportData.setImportSampleDataCommons(importSampleDataCommons);
                hybrisImportData.setImportSampleDataProducts(importSampleDataProducts);
                hybrisImportData.setImportSampleDataProductsLegacySystem(importSampleDataProductsFromLegacySystem);
                hybrisImportData.setImportSampleDataCustomers(importSampleDataCustomers);
                hybrisImportData.setImportSampleDataPrices(importSampleDataPrices);
                hybrisImportData.setImportSampleDataStocks(importSampleDataStocks);
                hybrisImportData.setImportSampleDataAgents(importSampleDataAgents);
                hybrisImportData.setLegacySystems(storesToImport.get(storeName));
                hybrisImportData.setImportSampleDataContentCatalog(importSampleDataContentCatalog);
                importData.add(hybrisImportData);

                getSolgroupCoreDataImportService().execute(this, context, importData);
                if (BooleanUtils.isTrue(notifyEvent)) {
                    getEventService().publishEvent(new CoreDataImportedEvent(context, importData));
                }

                getSolgroupSampleDataImportService().execute(this, context, importData);
                if (BooleanUtils.isTrue(notifyEvent)) {
                    getEventService().publishEvent(new SampleDataImportedEvent(context, importData));
                }
                
                // importcockpit setup  
                if (importCockpitInitialData) {
                    getSetupImpexService().importImpexFile(String.format("/solgroupinitialdata/import/importcockpit/%s/import_jobs.impex", storeName), true);
                }

                logInfo(context, "End import for " + storeName);
            }
        }
    }

    protected SolGroupCoreDataImportService getSolgroupCoreDataImportService() {
        return solgroupCoreDataImportService;
    }

    @Required
    public void setSolgroupCoreDataImportService(final SolGroupCoreDataImportService solgroupCoreDataImportService) {
        this.solgroupCoreDataImportService = solgroupCoreDataImportService;
    }

    protected SolGroupSampleDataImportService getSolgroupSampleDataImportService() {
        return solgroupSampleDataImportService;
    }

    @Required
    public void setSolgroupSampleDataImportService(
            final SolGroupSampleDataImportService solgroupSampleDataImportService) {
        this.solgroupSampleDataImportService = solgroupSampleDataImportService;
    }

    protected ConfigurationService getConfigurationService() {
        return configurationService;
    }

    @Required
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
