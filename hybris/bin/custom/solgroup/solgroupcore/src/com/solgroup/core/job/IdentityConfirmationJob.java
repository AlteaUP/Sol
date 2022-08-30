package com.solgroup.core.job;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.dao.b2bcustomer.SolGroupB2BCustomerDao;
import com.solgroup.core.model.IdentityConfirmationProcessModel;
import com.solgroup.core.model.job.IdentityConfirmationCronJobModel;
import com.solgroup.core.service.b2bcustomer.SolGroupCustomerAccountService;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

public class IdentityConfirmationJob extends AbstractJobPerformable<IdentityConfirmationCronJobModel> {
    private static final Logger LOG = Logger.getLogger(IdentityConfirmationJob.class);
    private static final String processName = "identityConfirmationEmailProcess";

    private SolGroupB2BCustomerDao solGroupB2BCustomerDao;
    private BusinessProcessService businessProcessService;
    private CatalogVersionService catalogVersionService;
    private CMSSiteService cmsSiteService;
    private SolGroupCustomerAccountService solGroupCustomerAccountService;

    @Override
    public PerformResult perform(IdentityConfirmationCronJobModel cronJob) {

        List<B2BCustomerModel> customers = getSolGroupB2BCustomerDao().findIdentityConfirmationSentB2BCustomer(false,
                true, cronJob.getSite());
        boolean allCompletedWithSuccess = true;

        cmsSiteService.setCurrentSite(cronJob.getSite());

        for (B2BCustomerModel customer : customers) {
            try {
                String processCode = "IdentityConfirmation_" + customer.getContactEmail() + "_"
                        + new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());
                final IdentityConfirmationProcessModel identitiConfirmationProcess = getBusinessProcessService()
                        .<IdentityConfirmationProcessModel> createProcess(processCode, processName);

                if (identitiConfirmationProcess != null) {
                    getSolGroupCustomerAccountService().identityConfirmation_createToken(customer);

                    identitiConfirmationProcess.setCustomer(customer);
                    identitiConfirmationProcess.setSite(cronJob.getSite());
                    identitiConfirmationProcess.setStore(cronJob.getSite().getStores().get(0));

                    LanguageModel customerLanguage = customer.getSessionLanguage();
                    if (customerLanguage == null) {
                        customerLanguage = cronJob.getSite().getDefaultLanguage();
                    }
                    identitiConfirmationProcess.setLanguage(customerLanguage);
                    identitiConfirmationProcess.setCurrency(customer.getDefaultB2BUnit().getSessionCurrency() != null
                            ? customer.getDefaultB2BUnit().getSessionCurrency()
                            : cronJob.getSite().getStores().get(0).getDefaultCurrency());
                    identitiConfirmationProcess.setToken(customer.getToken());
                    modelService.save(identitiConfirmationProcess);
                    getBusinessProcessService().startProcess(identitiConfirmationProcess);

                } else {
                    LOG.debug("Could not create credentials email process [" + processName + "]");
                }
            } catch (Exception exc) {
                allCompletedWithSuccess = false;
                LOG.error(exc.getMessage(), exc);
            }
        }

        if (allCompletedWithSuccess) {
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        } else {
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
        }

    }

    protected BusinessProcessService getBusinessProcessService() {
        return businessProcessService;
    }

    @Required
    public void setBusinessProcessService(BusinessProcessService businessProcessService) {
        this.businessProcessService = businessProcessService;
    }

    protected CatalogVersionService getCatalogVersionService() {
        return catalogVersionService;
    }

    @Required
    public void setCatalogVersionService(CatalogVersionService catalogVersionService) {
        this.catalogVersionService = catalogVersionService;
    }

    protected CMSSiteService getCmsSiteService() {
        return cmsSiteService;
    }

    @Required
    public void setCmsSiteService(CMSSiteService cmsSiteService) {
        this.cmsSiteService = cmsSiteService;
    }

    protected SolGroupB2BCustomerDao getSolGroupB2BCustomerDao() {
        return solGroupB2BCustomerDao;
    }

    @Required
    public void setSolGroupB2BCustomerDao(SolGroupB2BCustomerDao solGroupB2BCustomerDao) {
        this.solGroupB2BCustomerDao = solGroupB2BCustomerDao;
    }

    protected SolGroupCustomerAccountService getSolGroupCustomerAccountService() {
        return solGroupCustomerAccountService;
    }

    @Required
    public void setSolGroupCustomerAccountService(SolGroupCustomerAccountService solGroupCustomerAccountService) {
        this.solGroupCustomerAccountService = solGroupCustomerAccountService;
    }

}
