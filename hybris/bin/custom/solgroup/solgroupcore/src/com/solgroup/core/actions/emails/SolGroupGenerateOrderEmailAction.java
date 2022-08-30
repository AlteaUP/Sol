package com.solgroup.core.actions.emails;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.commercefacades.product.ImageFormatMapping;
import de.hybris.platform.core.model.media.MediaContainerModel;
import de.hybris.platform.core.model.media.MediaFormatModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.media.MediaContainerService;
import de.hybris.platform.task.RetryLaterException;

public class SolGroupGenerateOrderEmailAction extends SolGroupGenerateEmailAction {

	
	private static final Logger LOG = Logger.getLogger(SolGroupGenerateOrderEmailAction.class);
	private static final String productImageFormat = "thumbnail";
	
	private ImageFormatMapping imageFormatMapping;
	private MediaContainerService mediaContainerService;
	
	@Override
	public Transition executeAction(final BusinessProcessModel businessProcessModel) throws RetryLaterException
	{
		
		final Transition transitionResult = super.executeAction(businessProcessModel);
		if(transitionResult.equals(Transition.OK)) {
		
			if(!(businessProcessModel instanceof OrderProcessModel)) {
				LOG.error("Business process with code " + businessProcessModel.getCode() + " is not an instance of OrderProcessModel !!!");
				return Transition.NOK;
			}
			
			
			final OrderProcessModel orderProcessModel = (OrderProcessModel)businessProcessModel;
			final OrderModel order = orderProcessModel.getOrder();
			
			if(order==null) {
				LOG.error("Order process with code " + businessProcessModel.getCode() + " contains a NULL order !!!");
				return Transition.NOK;
			}
			
			final CatalogVersionModel contentCatalogVersion = obtainContentCatalog(businessProcessModel);
			
			if(CollectionUtils.isNotEmpty(order.getEntries())) {
				for(final AbstractOrderEntryModel abstractOrderEntry : order.getEntries()) {
					final ProductModel product = abstractOrderEntry.getProduct();
					
					if(product==null) {
						LOG.warn("AbstractOrderEntry with PK " + abstractOrderEntry.getPk().toString() + " has a NULL product !!!");
						continue;
					}
					
					
					try
					{

						final MediaModel productPicture = product.getPicture();
						if(productPicture==null) {
							LOG.warn("Product " + product.getCode() + " has picture attribute NULL !!!");
							continue;
						}
						final MediaContainerModel mediaContainer = productPicture.getMediaContainer();
						if(mediaContainer==null) {
							LOG.warn("Product " + product.getCode() + " has a media picture with mediaContainer NULL !!!");
							continue;
						}
						
						final String mediaFormatQualifier = getImageFormatMapping().getMediaFormatQualifierForImageFormat(productImageFormat);
						if (mediaFormatQualifier != null)
						{
							final MediaFormatModel mediaFormat = getMediaService().getFormat(mediaFormatQualifier);
							if (mediaFormat != null)
							{
								final MediaModel media = getMediaContainerService().getMediaForFormat(mediaContainer, mediaFormat);
								if (media != null)
								{
									attachMediaInline(businessProcessModel, contentCatalogVersion, media, product.getCode());
								}
							}
						}
					}
					catch (final ModelNotFoundException ignore)
					{
						// Ignore
					}
					
				}
			}
			
			
		
		}
		
		return Transition.OK;
		
	}

	protected ImageFormatMapping getImageFormatMapping() {
		return imageFormatMapping;
	}

	@Required
	public void setImageFormatMapping(final ImageFormatMapping imageFormatMapping) {
		this.imageFormatMapping = imageFormatMapping;
	}

	protected MediaContainerService getMediaContainerService() {
		return mediaContainerService;
	}

	@Required
	public void setMediaContainerService(final MediaContainerService mediaContainerService) {
		this.mediaContainerService = mediaContainerService;
	}
	
}
