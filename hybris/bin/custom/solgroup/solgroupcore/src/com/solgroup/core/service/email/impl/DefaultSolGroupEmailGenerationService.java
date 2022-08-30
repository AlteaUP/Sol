package com.solgroup.core.service.email.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import com.solgroup.core.service.company.SolGroupCompanyService;

import de.hybris.platform.acceleratorservices.email.impl.DefaultEmailGenerationService;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageTemplateModel;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commons.model.renderer.RendererTemplateModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.util.ServicesUtil;

/**
 * 
 * @author fmilazzo
 *
 */
public class DefaultSolGroupEmailGenerationService extends DefaultEmailGenerationService {

    private static final Logger LOG = Logger.getLogger(DefaultSolGroupEmailGenerationService.class);

    private SolGroupCompanyService solgroupCompanyService;

    @Override
    public EmailMessageModel generate(final BusinessProcessModel businessProcessModel, final EmailPageModel emailPageModel) {
        ServicesUtil.validateParameterNotNull(emailPageModel, "EmailPageModel cannot be null");
        Assert.isInstanceOf(EmailPageTemplateModel.class, emailPageModel.getMasterTemplate(),
                "MasterTemplate associated with EmailPageModel should be EmailPageTemplate");

        final EmailPageTemplateModel emailPageTemplateModel = (EmailPageTemplateModel) emailPageModel.getMasterTemplate();
        final RendererTemplateModel bodyRenderTemplate = emailPageTemplateModel.getHtmlTemplate();
        Assert.notNull(bodyRenderTemplate, "HtmlTemplate associated with MasterTemplate of EmailPageModel cannot be null");
        final RendererTemplateModel subjectRenderTemplate = emailPageTemplateModel.getSubject();
        Assert.notNull(subjectRenderTemplate, "Subject associated with MasterTemplate of EmailPageModel cannot be null");

        final EmailMessageModel emailMessageModel;
        // This call creates the context to be used for rendering of subject and body templates.
        final AbstractEmailContext<BusinessProcessModel> emailContext = getEmailContextFactory().create(businessProcessModel,
                emailPageModel, bodyRenderTemplate);

        if (emailContext == null) {
            LOG.error("Failed to create email context for businessProcess [" + businessProcessModel + "]");
            throw new IllegalStateException("Failed to create email context for businessProcess [" + businessProcessModel + "]");
        } else {
            if (!validate(emailContext)) {
                LOG.error("Email context for businessProcess [" + businessProcessModel + "] is not valid: "
                        + ReflectionToStringBuilder.toString(emailContext));
                throw new IllegalStateException("Email context for businessProcess [" + businessProcessModel + "] is not valid: "
                        + ReflectionToStringBuilder.toString(emailContext));
            }

            final StringWriter subject = new StringWriter();
            getRendererService().render(subjectRenderTemplate, emailContext, subject);

            final StringWriter body = new StringWriter();
            getRendererService().render(bodyRenderTemplate, emailContext, body);

            final UserModel userModel = businessProcessModel.getProperty("user");
            final UserModel vendor = getSolgroupCompanyService().findAssignedAgents(getB2BUnit(userModel));

            final List<EmailAddressModel> toEmails = new ArrayList<EmailAddressModel>();
            for (final AddressModel addressModel : vendor.getAddresses()) {
                final EmailAddressModel toAddress = getEmailService().getOrCreateEmailAddressForEmail(addressModel.getEmail(),
                        addressModel.getEmail());
                toEmails.add(toAddress);
            }

            emailMessageModel = createEmailMessage(subject.toString(), body.toString(), emailContext, toEmails);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Email Subject: " + emailMessageModel.getSubject());
                LOG.debug("Email Body: " + emailMessageModel.getBody());
            }

        }

        return emailMessageModel;
    }

    protected EmailMessageModel createEmailMessage(final String emailSubject, final String emailBody,
            final AbstractEmailContext<BusinessProcessModel> emailContext, final List<EmailAddressModel> toEmails) {
        final EmailAddressModel fromAddress = getEmailService().getOrCreateEmailAddressForEmail(emailContext.getFromEmail(),
                emailContext.getFromDisplayName());
        return getEmailService().createEmailMessage(toEmails, new ArrayList<EmailAddressModel>(), new ArrayList<EmailAddressModel>(),
                fromAddress, emailContext.getFromEmail(), emailSubject, emailBody, null);
    }

    private B2BUnitModel getB2BUnit(final UserModel userModel) {
        if (userModel instanceof B2BCustomerModel) {
            final B2BCustomerModel b2bCustomer = (B2BCustomerModel) userModel;
            return b2bCustomer.getDefaultB2BUnit();
        }
        return null;
    }

    protected SolGroupCompanyService getSolgroupCompanyService() {
        return solgroupCompanyService;
    }

    @Required
    public void setSolgroupCompanyService(final SolGroupCompanyService solgroupCompanyService) {
        this.solgroupCompanyService = solgroupCompanyService;
    }

}