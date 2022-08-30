package com.solgroup.core.actions.emails;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.enums.EmailAttachmentDispositionEnum;

import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.acceleratorservices.process.email.actions.GenerateEmailAction;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.task.RetryLaterException;

public class SolGroupGenerateEmailAction extends GenerateEmailAction {

    private static final Logger LOG = Logger.getLogger(SolGroupGenerateEmailAction.class);

    private MediaService mediaService;
    private CatalogVersionService catalogVersionService;

    @Override
    public Transition executeAction(final BusinessProcessModel businessProcessModel) throws RetryLaterException {

        final Transition transitionResult = super.executeAction(businessProcessModel);
        if (transitionResult.equals(Transition.OK)) {
            final CatalogVersionModel contentCatalogVersion = obtainContentCatalog(businessProcessModel);
            final MediaModel mediaLogo = getMediaService().getMedia(contentCatalogVersion, "/images/theme/logo_sol.png");
            attachMediaInline(businessProcessModel, contentCatalogVersion, mediaLogo, "logo_sol.png");
        }

        return Transition.OK;

    }

    protected void attachMediaInline(final BusinessProcessModel businessProcess, final CatalogVersionModel contentCatalogVersion,
            final MediaModel media, final String attachmentCid) {

        // Create email attachment
        final EmailAttachmentModel emailAttachment = getModelService().create(EmailAttachmentModel.class);
        emailAttachment.setCode(media.getCode() + "_email_attachment_inline_" + System.currentTimeMillis());
        if (StringUtils.isNotEmpty(media.getRealFileName())) {
            emailAttachment.setRealFileName(media.getRealFileName());
        } else {
            emailAttachment.setRealFileName(media.getCode());
        }
        emailAttachment.setCatalogVersion(contentCatalogVersion);
        emailAttachment.setAttachmentType(EmailAttachmentDispositionEnum.INLINE);
        emailAttachment.setAttachmentCid(attachmentCid);
        getModelService().save(emailAttachment);
        getMediaService().setStreamForMedia(emailAttachment, mediaService.getStreamFromMedia(media));
        getModelService().refresh(emailAttachment);
        emailAttachment.setMime(media.getMime());
        getModelService().save(emailAttachment);
        getModelService().refresh(emailAttachment);

        final List<EmailAttachmentModel> emailAttachmentList = new ArrayList<EmailAttachmentModel>();
        emailAttachmentList.add(emailAttachment);

        final List<EmailMessageModel> emailMessages = businessProcess.getEmails();
        for (final EmailMessageModel emailMessage : emailMessages) {
            if (CollectionUtils.isNotEmpty(emailMessage.getAttachments())) {
                emailAttachmentList.addAll(emailMessage.getAttachments());
            }
            emailMessage.setAttachments(emailAttachmentList);
            getModelService().save(emailMessage);
            getModelService().refresh(emailMessage);
        }

    }

    protected CatalogVersionModel obtainContentCatalog(final BusinessProcessModel businessProcess) {
        return getContextResolutionStrategy().getContentCatalogVersion(businessProcess);

    }

    protected MediaService getMediaService() {
        return mediaService;
    }

    @Required
    public void setMediaService(final MediaService mediaService) {
        this.mediaService = mediaService;
    }

    protected CatalogVersionService getCatalogVersionService() {
        return catalogVersionService;
    }

    @Required
    public void setCatalogVersionService(final CatalogVersionService catalogVersionService) {
        this.catalogVersionService = catalogVersionService;
    }

}
