package com.solgroup.core.actions.emails;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

/**
 * 
 * @author fmilazzo
 *
 */
public class SolGroupGenerateIdentityConfirmationEmailAction extends GenerateEmailAction {

    private MediaService mediaService;
    private CatalogVersionService catalogVersionService;
    private final String FILE_LOCATION = "/documents/attachments/";
    private final String FILE_NAME = "Informativa sul trattamento dei dati personali.pdf";

    @Override
    public Transition executeAction(final BusinessProcessModel businessProcessModel) throws RetryLaterException {
        
        Transition transitionResult = super.executeAction(businessProcessModel);
        if (transitionResult.equals(Transition.OK)) {
            CatalogVersionModel contentCatalogVersion = obtainContentCatalog(businessProcessModel);
            final MediaModel attachment = getMediaService().getMedia(contentCatalogVersion, FILE_LOCATION + FILE_NAME);
            attachMedia(businessProcessModel, contentCatalogVersion, attachment, FILE_NAME);
        }
        return Transition.OK;
    }

    protected void attachMedia(BusinessProcessModel businessProcess, CatalogVersionModel contentCatalogVersion,
            MediaModel media, String attachmentCid) {

        // Create email attachment
        final EmailAttachmentModel emailAttachment = getModelService().create(EmailAttachmentModel.class);
        emailAttachment
                .setCode(media.getCode() + "_email_attachment_identity_confirmation_" + System.currentTimeMillis());
        if (StringUtils.isNotEmpty(media.getRealFileName())) {
            emailAttachment.setRealFileName(media.getRealFileName());
        } else {
            emailAttachment.setRealFileName(media.getCode());
        }
        emailAttachment.setCatalogVersion(contentCatalogVersion);
        emailAttachment.setAttachmentType(EmailAttachmentDispositionEnum.ATTACHMENT);
        emailAttachment.setAttachmentCid(attachmentCid);
        emailAttachment.setMime(media.getMime());
        getModelService().save(emailAttachment);
        getMediaService().setStreamForMedia(emailAttachment, mediaService.getStreamFromMedia(media));
        getModelService().refresh(emailAttachment);

        final List<EmailAttachmentModel> emailAttachmentList = new ArrayList<EmailAttachmentModel>();
        emailAttachmentList.add(emailAttachment);

        List<EmailMessageModel> emailMessages = businessProcess.getEmails();
        for (EmailMessageModel emailMessage : emailMessages) {
            if (CollectionUtils.isNotEmpty(emailMessage.getAttachments())) {
                emailAttachmentList.addAll(emailMessage.getAttachments());
            }
            emailMessage.setAttachments(emailAttachmentList);
            getModelService().save(emailMessage);
            getModelService().refresh(emailMessage);
        }

    }

    protected CatalogVersionModel obtainContentCatalog(BusinessProcessModel businessProcess) {
        return getContextResolutionStrategy().getContentCatalogVersion(businessProcess);
    }

    protected MediaService getMediaService() {
        return mediaService;
    }

    @Required
    public void setMediaService(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    protected CatalogVersionService getCatalogVersionService() {
        return catalogVersionService;
    }

    @Required
    public void setCatalogVersionService(CatalogVersionService catalogVersionService) {
        this.catalogVersionService = catalogVersionService;
    }

}
