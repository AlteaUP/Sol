package com.solgroup.core.actions.emails;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.email.impl.DefaultSolGroupEmailGenerationService;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.acceleratorservices.process.email.actions.GenerateEmailAction;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.task.RetryLaterException;

/**
 * 
 * @author fmilazzo
 *
 */
public class SolGroupGenerateVendorAlertEmailAction extends GenerateEmailAction {

    private static final Logger LOG = Logger.getLogger(SolGroupGenerateVendorAlertEmailAction.class);

    // private EmailGenerationService emailGenerationService;
    private DefaultSolGroupEmailGenerationService defaultSolGroupEmailGenerationService;

    @Override
    public Transition executeAction(final BusinessProcessModel businessProcessModel) throws RetryLaterException {
        getContextResolutionStrategy().initializeContext(businessProcessModel);

        final CatalogVersionModel contentCatalogVersion = getContextResolutionStrategy().getContentCatalogVersion(businessProcessModel);
        if (contentCatalogVersion == null) {
            LOG.warn("Could not resolve the content catalog version, cannot generate email content");
            return Transition.NOK;
        }

        final EmailPageModel emailPageModel = getCmsEmailPageService().getEmailPageForFrontendTemplate(getFrontendTemplateName(),
                contentCatalogVersion);
        if (emailPageModel == null) {
            LOG.warn("Could not retrieve email page model for " + getFrontendTemplateName() + " and "
                    + contentCatalogVersion.getCatalog().getName() + ":" + contentCatalogVersion.getVersion()
                    + ", cannot generate email content");
            return Transition.NOK;
        }

        final EmailMessageModel emailMessageModel = getDefaultSolGroupEmailGenerationService().generate(businessProcessModel,
                emailPageModel);
        if (emailMessageModel == null) {
            LOG.warn("Failed to generate email message");
            return Transition.NOK;
        }

        final List<EmailMessageModel> emails = new ArrayList<>();
        emails.addAll(businessProcessModel.getEmails());
        emails.add(emailMessageModel);
        businessProcessModel.setEmails(emails);

        getModelService().save(businessProcessModel);

        LOG.info("Email message generated");
        return Transition.OK;
    }

    protected DefaultSolGroupEmailGenerationService getDefaultSolGroupEmailGenerationService() {
        return defaultSolGroupEmailGenerationService;
    }

    @Required
    public void setDefaultSolGroupEmailGenerationService(
            final DefaultSolGroupEmailGenerationService defaultSolGroupEmailGenerationService) {
        this.defaultSolGroupEmailGenerationService = defaultSolGroupEmailGenerationService;
    }

}
