package com.solgroup.core.job;

import com.solgroup.core.model.NewProductsNotificationProcessModel;
import com.solgroup.core.model.job.NewProductsNotificationCronJobModel;
import com.solgroup.core.service.product.SolGroupProductService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.variants.model.GenericVariantProductModel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewProductsNotificationJobPerformable extends AbstractJobPerformable<NewProductsNotificationCronJobModel> {

    private static final Logger LOG = Logger.getLogger(NewProductsNotificationJobPerformable.class);
    private static final String PROCESS_NAME = "newProductsNotificationEmailProcess";
    private static final String CATALOG_ID = "solgroupITProductCatalog";
    private static final String CATALOG_VERSION = "Staged";

    private BusinessProcessService businessProcessService;
    private CatalogVersionService catalogVersionService;
    private CMSSiteService cmsSiteService;
    private SolGroupProductService productService;

    @Override
    public PerformResult perform(NewProductsNotificationCronJobModel jobModel) {

        boolean allCompletedWithSuccess = true;

        List<ProductModel> newProductsToCheck = getAllNewProductsToCheck(jobModel.getDaysToCheck());
        List<GenericVariantProductModel> newVariantsToCheck = getAllNewVariantsToCheck(jobModel.getDaysToCheck());

        if(jobModel.getEmployeesToNotify() != null && ((newProductsToCheck != null && !newProductsToCheck.isEmpty())
                || (newVariantsToCheck != null && !newVariantsToCheck.isEmpty()))){

            cmsSiteService.setCurrentSite(jobModel.getSite());
            //Per ogni employee a livello di NewProductsNotificationCronJobModel creo un processo per l'invio della mail
            for (EmployeeModel employee : jobModel.getEmployeesToNotify()) {
                try {
                    String processCode = "NewProductsNotification_" + new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());
                    final NewProductsNotificationProcessModel newProductsNotificationProcessModel = getBusinessProcessService()
                            .<NewProductsNotificationProcessModel> createProcess(processCode, PROCESS_NAME);

                    if (newProductsNotificationProcessModel != null) {

                        newProductsNotificationProcessModel.setSite(jobModel.getSite());
                        newProductsNotificationProcessModel.setStore(jobModel.getSite().getStores().get(0));
                        newProductsNotificationProcessModel.setEmployee(employee);
                        newProductsNotificationProcessModel.setLanguageModel(jobModel.getSessionLanguage());

                        //Setto i nuovi prodotti padre nel model
                        if(newProductsToCheck != null) {
                            newProductsNotificationProcessModel.setNewProducts(newProductsToCheck);
                        }else{
                            newProductsNotificationProcessModel.setNewProducts(new ArrayList<>());
                        }

                        //Setto i nuovi prodotti figlio (varianti) nel model
                        if(newVariantsToCheck != null){
                            newProductsNotificationProcessModel.setNewVariants(newVariantsToCheck);
                        }else{
                            newProductsNotificationProcessModel.setNewVariants(new ArrayList<>());
                        }

                        modelService.save(newProductsNotificationProcessModel);
                        getBusinessProcessService().startProcess(newProductsNotificationProcessModel);

                    } else {
                        LOG.debug("Could not create process [" + PROCESS_NAME + "]");
                    }
                } catch (Exception exc) {
                    allCompletedWithSuccess = false;
                    LOG.debug("Could not create process [" + PROCESS_NAME + "]");
                    LOG.error(exc.getMessage(), exc);
                }
            }

        }

        if (allCompletedWithSuccess) {
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        } else {
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
        }
    }


    List<ProductModel> getAllNewProductsToCheck(Integer daysToCheck){
        return getProductService().getAllBaseProductForDateRangeAndApprovalStatusAndCatalogVersion(daysToCheck, ArticleApprovalStatus.CHECK, catalogVersionService.getCatalogVersion(CATALOG_ID, CATALOG_VERSION));
    }

    List<GenericVariantProductModel> getAllNewVariantsToCheck(Integer daysToCheck){
        return getProductService().getAllProductVariantsForDateRangeAndApprovalStatusAndCatalogVersion(daysToCheck, ArticleApprovalStatus.CHECK, catalogVersionService.getCatalogVersion(CATALOG_ID, CATALOG_VERSION));
    }

    public CMSSiteService getCmsSiteService() {
        return cmsSiteService;
    }

    public void setCmsSiteService(CMSSiteService cmsSiteService) {
        this.cmsSiteService = cmsSiteService;
    }

    public BusinessProcessService getBusinessProcessService() {
        return businessProcessService;
    }

    @Required
    public void setBusinessProcessService(BusinessProcessService businessProcessService) {
        this.businessProcessService = businessProcessService;
    }

    public CatalogVersionService getCatalogVersionService() {
        return catalogVersionService;
    }

    @Required
    public void setCatalogVersionService(CatalogVersionService catalogVersionService) {
        this.catalogVersionService = catalogVersionService;
    }

    public SolGroupProductService getProductService() {
        return productService;
    }

    public void setProductService(SolGroupProductService productService) {
        this.productService = productService;
    }

}
