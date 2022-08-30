package com.solgroup.core.service.email.impl;

import java.util.List;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;

import com.solgroup.core.enums.EmailAttachmentDispositionEnum;

import de.hybris.platform.acceleratorservices.email.impl.DefaultEmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;

public class DefaultSolGroupEmailService extends DefaultEmailService {
	
	private Logger LOG = Logger.getLogger(DefaultSolGroupEmailService.class);

	@Override
	protected boolean processAttachmentsSuccessful(final HtmlEmail email, final List<EmailAttachmentModel> attachments)
	{
		if (attachments != null && !attachments.isEmpty())
		{
			for (final EmailAttachmentModel attachment : attachments)
			{
				try
				{
					final DataSource dataSource = new ByteArrayDataSource(getMediaService().getDataFromMedia(attachment),
							attachment.getMime());
					
					if(attachment.getAttachmentType()==null || attachment.getAttachmentType().equals(EmailAttachmentDispositionEnum.ATTACHMENT)) {
						email.attach(dataSource, attachment.getRealFileName(), attachment.getAltText());
					}
					else if (attachment.getAttachmentType().equals(EmailAttachmentDispositionEnum.INLINE)) {
						String cid = attachment.getRealFileName();
						if(StringUtils.isNotEmpty(attachment.getAttachmentCid())) {
							cid = attachment.getAttachmentCid();
						}
						email.embed(dataSource, attachment.getRealFileName(), cid);
					}
				}
				catch (final EmailException ex)
				{
					LOG.error("Failed to load attachment data into data source [" + attachment + "]", ex);
					return false;
				}
			}
		}
		return true;
	}


}
