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
package com.solgroup.commercefacades.product.converter.populator;

import com.solgroup.core.model.TechAttachmentLinkModel;
import com.solgroup.facades.product.data.TechAttachmentLinkData;
import de.hybris.platform.cms2.enums.LinkTargets;
import de.hybris.platform.commercefacades.product.PriceDataFactory;
import de.hybris.platform.commercefacades.product.converters.populator.AbstractProductPopulator;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.price.CommercePriceService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.europe1.jalo.PriceRow;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.order.price.PriceInformation;
import de.hybris.platform.jalo.security.JaloSecurityException;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;
import de.hybris.platform.variants.model.GenericVariantProductModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Populate the product data with the custom price information
 */
public class ProductExtendedPopulator<SOURCE extends ProductModel, TARGET extends ProductData>
		extends AbstractProductPopulator<SOURCE, TARGET> {
	private static final Logger LOG = Logger.getLogger(ProductExtendedPopulator.class);

	private CommercePriceService commercePriceService;
	private PriceDataFactory priceDataFactory;
	private UserService userService;

	protected CommercePriceService getCommercePriceService() {
		return commercePriceService;
	}

	@Required
	public void setCommercePriceService(final CommercePriceService commercePriceService) {
		this.commercePriceService = commercePriceService;
	}

	protected PriceDataFactory getPriceDataFactory() {
		return priceDataFactory;
	}

	@Required
	public void setPriceDataFactory(final PriceDataFactory priceDataFactory) {
		this.priceDataFactory = priceDataFactory;
	}

	@Override
	public void populate(final SOURCE productModel, final TARGET productData) throws ConversionException {
		final PriceDataType priceType;
		final PriceInformation info;

		if (CollectionUtils.isEmpty(productModel.getVariants())) {
			priceType = PriceDataType.BUY;
			info = getCommercePriceService().getWebPriceForProduct(productModel);
		} else {
			priceType = PriceDataType.FROM;
			info = getCommercePriceService().getFromPriceForProduct(productModel);
		}
		if (productModel instanceof GenericVariantProductModel){
			GenericVariantProductModel variantProduct = (GenericVariantProductModel) productModel;

			productData.setBaseProductName(variantProduct.getBaseProduct().getName());
		}
		populatePriceInfo(productData, priceType, info);

		populateTechAttachmentLinks(productModel, productData);
		populateMedAttachmentLinks(productModel, productData);

	}

	private void populateTechAttachmentLinks(final SOURCE productModel, final TARGET productData) {
		final String WELDER_TYPE = "WELDER";
		final String WELDER_CUSTOMER_GROUP = "welderCustomerGroup";
		List<TechAttachmentLinkData> techAttachmentsData = new ArrayList<TechAttachmentLinkData>();
		CustomerModel customer = (CustomerModel)userService.getCurrentUser();
		Set<PrincipalGroupModel> customerGroups = customer.getAllGroups();
		for (TechAttachmentLinkModel techAttachmentLink : productModel.getTechAttachmentLinks()) {
			if (techAttachmentLink.isVisible()) {

				if(techAttachmentLink.getTechAttachmentType().getCode().equalsIgnoreCase(WELDER_TYPE)){
					for (PrincipalGroupModel customerGroup : customerGroups) {
						if(customerGroup.getUid().equalsIgnoreCase(WELDER_CUSTOMER_GROUP)){
							//If welder attachment type and the user has the welder customer group, insert the attachment link
							TechAttachmentLinkData techAttachmentsTarget = new TechAttachmentLinkData();
							techAttachmentsTarget.setDescription(techAttachmentLink.getDescription());
							techAttachmentsTarget.setExternal(techAttachmentLink.isExternal());
							techAttachmentsTarget.setLinkName(techAttachmentLink.getLinkName());
							techAttachmentsTarget.setStyleAttributes(techAttachmentLink.getStyleAttributes());
							techAttachmentsTarget.setTarget(techAttachmentLink.getTarget() == LinkTargets.NEWWINDOW);
							techAttachmentsTarget.setTitle(techAttachmentLink.getTitle());
							techAttachmentsTarget.setUrl(getTechAttachmentUrl(techAttachmentLink));
							techAttachmentsData.add(techAttachmentsTarget);
						}
					}

				}else{
					TechAttachmentLinkData techAttachmentsTarget = new TechAttachmentLinkData();
					techAttachmentsTarget.setDescription(techAttachmentLink.getDescription());
					techAttachmentsTarget.setExternal(techAttachmentLink.isExternal());
					techAttachmentsTarget.setLinkName(techAttachmentLink.getLinkName());
					techAttachmentsTarget.setStyleAttributes(techAttachmentLink.getStyleAttributes());
					techAttachmentsTarget.setTarget(techAttachmentLink.getTarget() == LinkTargets.NEWWINDOW);
					techAttachmentsTarget.setTitle(techAttachmentLink.getTitle());
					techAttachmentsTarget.setUrl(getTechAttachmentUrl(techAttachmentLink));
					techAttachmentsData.add(techAttachmentsTarget);
				}
			}

		}

		productData.setTechAttachments(techAttachmentsData);
	}

	private void populateMedAttachmentLinks(final SOURCE productModel, final TARGET productData) {
		List<TechAttachmentLinkData> techAttachmentsData = new ArrayList<TechAttachmentLinkData>();
		for (TechAttachmentLinkModel techAttachmentLink : productModel.getMedicalAttachmentLinks()) {
			if (techAttachmentLink.isVisible()) {
				TechAttachmentLinkData techAttachmentsTarget = new TechAttachmentLinkData();
				techAttachmentsTarget.setDescription(techAttachmentLink.getDescription());
				techAttachmentsTarget.setExternal(techAttachmentLink.isExternal());
				techAttachmentsTarget.setLinkName(techAttachmentLink.getLinkName());
				techAttachmentsTarget.setStyleAttributes(techAttachmentLink.getStyleAttributes());
				techAttachmentsTarget.setTarget(techAttachmentLink.getTarget() == LinkTargets.NEWWINDOW);
				techAttachmentsTarget.setTitle(techAttachmentLink.getTitle());
				techAttachmentsTarget.setUrl(getTechAttachmentUrl(techAttachmentLink));
				techAttachmentsData.add(techAttachmentsTarget);
			}

		}

		productData.setMedicalAttachments(techAttachmentsData);
	}

	private String getTechAttachmentUrl(TechAttachmentLinkModel techAttachmentLink) {
		String url = "";
		if (techAttachmentLink.getWithBaseUrl()) {
			url += Config.getParameter("solgroup.techattachments.baseUrl")+ techAttachmentLink.getUrl();
		}else {
			if (techAttachmentLink.getMediaAttachment()==null) {
				url = techAttachmentLink.getUrl();
			}else{
				url = techAttachmentLink.getMediaAttachment().getURL();
			}
		}
		return url;
	}

	private void populatePriceInfo(final TARGET productData, final PriceDataType priceType,
			final PriceInformation info) {
		if (info != null) {
			PriceData priceData = getPriceDataFactory().create(priceType,
					BigDecimal.valueOf(info.getPriceValue().getValue()), info.getPriceValue().getCurrencyIso());

			PriceRow priceRow = (PriceRow) info.getQualifierValue("pricerow");

			try {
				if (priceRow.getAttribute("customPrice") == null) {
					productData.setCustomPrice(Boolean.FALSE);
				} else {
					productData.setCustomPrice((Boolean) priceRow.getAttribute("customPrice"));
				}

			} catch (JaloInvalidParameterException | JaloSecurityException e) {
				LOG.error(e.getStackTrace());
			}

		} else {
			productData.setPurchasable(Boolean.FALSE);
		}
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}
