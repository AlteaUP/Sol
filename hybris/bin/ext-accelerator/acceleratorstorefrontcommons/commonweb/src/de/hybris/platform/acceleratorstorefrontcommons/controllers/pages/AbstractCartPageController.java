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
package de.hybris.platform.acceleratorstorefrontcommons.controllers.pages;

import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.acceleratorservices.config.SiteConfigService;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.forms.SaveCartForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.UpdateQuantityForm;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cmsfacades.exception.ValidationError;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.SaveCartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.session.SessionService;
import jersey.repackaged.com.google.common.collect.Maps;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Lists;
import com.solgroup.cart.GlobalOrderEntryPropertiesUpdateResult;
import com.solgroup.cart.SingleOrderEntryPropertyUpdateResult;
import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.facades.abstractOrder.SolGroupAbstractOrderFacade;

import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.CigFormValidator;
import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.CupFormValidator;
import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.OrderDateFormValidator;
import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.PurchaseOrderNumberFormValidator;


public abstract class AbstractCartPageController extends AbstractPageController
{

	private static final String CART_CMS_PAGE_LABEL = "cart";
	private static final String CONTINUE_URL = "continueUrl";
	
	protected static final String ERROR_UPDATE_ALL = "error.update.all";
	protected static final String ERROR_UPDATE_PO = "error.update.po";
	protected static final String ERROR_UPDATE_CIG = "error.update.cig";
	protected static final String ERROR_UPDATE_CUP = "error.update.cup";
	protected static final String ERROR_UPDATE_ORDERDATE = "error.update.orderdate";
	protected static final String SUCCESS_UPDATE_ORDER_ENTRY_PROPERTIES = "success.update.orderentry.properties";


	/**
	 * @deprecated Since 6.0. use
	 *             {@link de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCartPageController#LOGGER}
	 *             instead.
	 */
	@Deprecated
	protected static final Logger LOG = Logger.getLogger(AbstractCartPageController.class);
	private static final Logger LOGGER = Logger.getLogger(AbstractCartPageController.class);

	@Resource(name = "cartFacade")
	private CartFacade cartFacade;

	@Resource(name = "siteConfigService")
	private SiteConfigService siteConfigService;

	@Resource(name = "sessionService")
	private SessionService sessionService;

	@Resource(name = "acceleratorCheckoutFacade")
	private AcceleratorCheckoutFacade checkoutFacade;

	@Resource(name = "saveCartFacade")
	private SaveCartFacade saveCartFacade;
	
	@Resource(name = "solGroupAbstractOrderFacade")
	private SolGroupAbstractOrderFacade solGroupAbstractOrderFacade;


	protected void createProductList(final Model model) throws CMSItemNotFoundException
	{
		final CartData cartData = cartFacade.getSessionCartWithEntryOrdering(false);
		createProductEntryList(model, cartData);

		storeCmsPageInModel(model, getContentPageForLabelOrId(CART_CMS_PAGE_LABEL));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(CART_CMS_PAGE_LABEL));
	}

	protected void createProductEntryList(final Model model, final CartData cartData)
	{
		boolean hasPickUpCartEntries = false;
		if (cartData.getEntries() != null && !cartData.getEntries().isEmpty())
		{
			for (final OrderEntryData entry : cartData.getEntries())
			{
				if (!hasPickUpCartEntries && entry.getDeliveryPointOfService() != null)
				{
					hasPickUpCartEntries = true;
				}
				final UpdateQuantityForm uqf = new UpdateQuantityForm();
				uqf.setQuantity(entry.getQuantity());
				model.addAttribute("updateQuantityForm" + entry.getEntryNumber(), uqf);
			}
		}

		model.addAttribute("cartData", cartData);
		model.addAttribute("hasPickUpCartEntries", Boolean.valueOf(hasPickUpCartEntries));
	}

	protected void continueUrl(final Model model) throws CMSItemNotFoundException
	{
		final String continueUrl = (String) sessionService.getAttribute(WebConstants.CONTINUE_URL);
		model.addAttribute(CONTINUE_URL, (continueUrl != null && !continueUrl.isEmpty()) ? continueUrl : ROOT);
	}

	protected void prepareDataForPage(final Model model) throws CMSItemNotFoundException
	{
		continueUrl(model);

		createProductList(model);

		setupCartPageRestorationData(model);
		clearSessionRestorationData();

		model.addAttribute("isOmsEnabled", Boolean.valueOf(getSiteConfigService().getBoolean("oms.enabled", false)));
		model.addAttribute("supportedCountries", cartFacade.getDeliveryCountries());
		model.addAttribute("expressCheckoutAllowed", Boolean.valueOf(checkoutFacade.isExpressCheckoutAllowedForCart()));
		model.addAttribute("taxEstimationEnabled", Boolean.valueOf(checkoutFacade.isTaxEstimationEnabledForCart()));
		model.addAttribute("savedCartCount", saveCartFacade.getSavedCartsCountForCurrentUser());
		if (!model.containsAttribute("saveCartForm"))
		{
			model.addAttribute("saveCartForm", new SaveCartForm());
		}
	}

	/**
	 * Remove the session data of the cart restoration.
	 */
	protected void clearSessionRestorationData()
	{
		getSessionService().removeAttribute(WebConstants.CART_RESTORATION);
		getSessionService().removeAttribute(WebConstants.CART_RESTORATION_ERROR_STATUS);
	}

	/**
	 * Prepare the restoration data and always display any modifications on the cart page.
	 *
	 * @param model
	 *           - the cart page
	 */
	public void setupCartPageRestorationData(final Model model)
	{
		if (getSessionService().getAttribute(WebConstants.CART_RESTORATION) != null)
		{
			if (getSessionService().getAttribute(WebConstants.CART_RESTORATION_ERROR_STATUS) != null)
			{
				model.addAttribute("restorationErrorMsg", getSessionService()
						.getAttribute(WebConstants.CART_RESTORATION_ERROR_STATUS));
			}
			else
			{
				model.addAttribute("restorationData", getSessionService().getAttribute(WebConstants.CART_RESTORATION));
			}
		}
		model.addAttribute("showModifications", Boolean.TRUE);
	}

	protected boolean validateCart(final RedirectAttributes redirectModel)
	{
		//Validate the cart
		List<CartModificationData> modifications = new ArrayList<>();
		try
		{
			modifications = cartFacade.validateCartData();
		}
		catch (final CommerceCartModificationException e)
		{
			LOGGER.error("Failed to validate cart", e);
		}
		if (!modifications.isEmpty())
		{
			redirectModel.addFlashAttribute("validationData", modifications);

			// Invalid cart. Bounce back to the cart page.
			return true;
		}
		return false;
	}
	
	protected GlobalOrderEntryPropertiesUpdateResult updateAllOrderEntryProperties(String purchaseOrderNumber, String cig, String cup, String orderDate) {
			// Result object
			GlobalOrderEntryPropertiesUpdateResult result = new GlobalOrderEntryPropertiesUpdateResult();
			result.setCig(cig);
			result.setCup(cup);
			result.setPurchaseOrderNumber(purchaseOrderNumber);
			result.setDataOrdine(orderDate);
			result.setError(Boolean.FALSE);
			result.setSuccessDescription(getMessageSource().getMessage(SUCCESS_UPDATE_ORDER_ENTRY_PROPERTIES, null,getI18nService().getCurrentLocale()));
			
			List<String> errorDescriptions = Lists.newArrayList();
	
			
			// Validity check
			PurchaseOrderNumberFormValidator purchaseOrderNumberFormValidator = new PurchaseOrderNumberFormValidator();
			CigFormValidator cigFormValidator = new CigFormValidator();
			CupFormValidator cupFormValidator = new CupFormValidator();
			OrderDateFormValidator orderDateFormValidator = new OrderDateFormValidator();
			BindingResult validationResult = new ValidationError("updateAllPropertiesForm"); 
			purchaseOrderNumberFormValidator.validate(purchaseOrderNumber, validationResult);
			cigFormValidator.validate(cig, validationResult);
			cupFormValidator.validate(cup, validationResult);
			orderDateFormValidator.validate(orderDate, validationResult);
			
			if(validationResult.hasErrors()) {
				result.setError(Boolean.TRUE);
				for(ObjectError error : validationResult.getAllErrors()) {
					errorDescriptions.add(getMessageSource().getMessage(error.getDefaultMessage(), null,getI18nService().getCurrentLocale()));
					if(error.getCode().equals(SolgroupCoreConstants.ORDERENTRY_PURCHASE_ORDER_NUMER)) {
						result.setPo_error(Boolean.TRUE);
					}
					else if(error.getCode().equals(SolgroupCoreConstants.ORDERENTRY_CIG)) {
						result.setCig_error(Boolean.TRUE);
					}
					else if(error.getCode().equals(SolgroupCoreConstants.ORDERENTRY_CUP)) {
						result.setCup_error(Boolean.TRUE);
					}
					else if(error.getCode().equals(SolgroupCoreConstants.ORDERENTRY_ORDER_DATA)) {
						result.setDataOrdine_error(Boolean.TRUE);
					}
				}
			}
	
			
			
	
			// Update data
			if(result.getError().equals(Boolean.FALSE)) {
	
				Map<String,Object> propertiesMap = Maps.newHashMap();
				propertiesMap.put(SolgroupCoreConstants.ORDERENTRY_PURCHASE_ORDER_NUMER, purchaseOrderNumber);
				propertiesMap.put(SolgroupCoreConstants.ORDERENTRY_CIG, cig);
				propertiesMap.put(SolgroupCoreConstants.ORDERENTRY_CUP, cup);
				
				// Convert orderDate into an object
				if(StringUtils.isNotEmpty(orderDate)) {
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SolgroupCoreConstants.ORDERENTRY_ORDER_DATA_FORMAT);
					java.util.Date date = null;
					try {
						date = simpleDateFormat.parse(orderDate);
						propertiesMap.put(SolgroupCoreConstants.ORDERENTRY_ORDER_DATA, date);
					} catch (ParseException e) {
						LOG.error(e.getMessage(), e);
						result.setError(Boolean.FALSE);
						errorDescriptions.add(getMessageSource().getMessage(ERROR_UPDATE_ALL, null,getI18nService().getCurrentLocale()));
						result.setErrorDescriptions(errorDescriptions);
						result.setSuccessDescription("");
						return result;
					}
				}
	
				// Perform update
				Boolean updateResult = solGroupAbstractOrderFacade.updateAllAbstractOrderEntryAttribute(CartModel._TYPECODE, null, propertiesMap);
	
				if(updateResult.equals(Boolean.FALSE)) {
					result.setError(Boolean.FALSE);
					errorDescriptions.add(getMessageSource().getMessage(ERROR_UPDATE_ALL, null,getI18nService().getCurrentLocale()));
				}
				
			}
			
			
			result.setErrorDescriptions(errorDescriptions);
			if(result.getError().equals(Boolean.TRUE)) {
				result.setSuccessDescription("");
			}
			return result;
	}
	
	
	protected SingleOrderEntryPropertyUpdateResult updatePurchaseOrderNumber(String purchaseOrderNumber, String productCode, String cartCode) {
		SingleOrderEntryPropertyUpdateResult result = new SingleOrderEntryPropertyUpdateResult();
		result.setError(Boolean.FALSE);
		result.setSuccessDescription(getMessageSource().getMessage(SUCCESS_UPDATE_ORDER_ENTRY_PROPERTIES, null,getI18nService().getCurrentLocale()));
		
		
		// Validity check
		PurchaseOrderNumberFormValidator purchaseOrderNumberFormValidator = new PurchaseOrderNumberFormValidator();
		BindingResult validationResult = new ValidationError("updatePoPropertyForm"); 
		purchaseOrderNumberFormValidator.validate(purchaseOrderNumber, validationResult);
		
		if(validationResult.hasErrors()) {
			result.setError(Boolean.TRUE);
			result.setErrorDescription(getMessageSource().getMessage(validationResult.getAllErrors().get(0).getDefaultMessage(), null,getI18nService().getCurrentLocale()));
			result.setPropertyValue(SolgroupCoreConstants.ORDERENTRY_PURCHASE_ORDER_NUMER);
		}
		
		
		else {
			boolean updateResult = solGroupAbstractOrderFacade.updateAbstractOrderEntryAttribute(CartModel._TYPECODE, cartCode, productCode, SolgroupCoreConstants.ORDERENTRY_PURCHASE_ORDER_NUMER, purchaseOrderNumber);
			if(!updateResult) {
				result.setError(Boolean.TRUE);
				result.setErrorDescription(getMessageSource().getMessage(ERROR_UPDATE_PO, null,getI18nService().getCurrentLocale()));
				result.setPropertyValue(SolgroupCoreConstants.ORDERENTRY_PURCHASE_ORDER_NUMER);
			}
		}
		
		if(result.getError().equals(Boolean.TRUE)) {
			result.setSuccessDescription("");
		}
		return result;

	}

	protected SingleOrderEntryPropertyUpdateResult updateCig(String cig, String productCode, String cartCode) {
		SingleOrderEntryPropertyUpdateResult result = new SingleOrderEntryPropertyUpdateResult();
		result.setError(Boolean.FALSE);
		result.setSuccessDescription(getMessageSource().getMessage(SUCCESS_UPDATE_ORDER_ENTRY_PROPERTIES, null,getI18nService().getCurrentLocale()));
		
		
		// Validity check
		CigFormValidator cigFormValidator = new CigFormValidator();
		BindingResult validationResult = new ValidationError("updateCigPropertyForm"); 
		cigFormValidator.validate(cig, validationResult);
		
		if(validationResult.hasErrors()) {
			result.setError(Boolean.TRUE);
			result.setErrorDescription(getMessageSource().getMessage(validationResult.getAllErrors().get(0).getDefaultMessage(), null,getI18nService().getCurrentLocale()));
			result.setPropertyValue(SolgroupCoreConstants.ORDERENTRY_CIG);
		}
		
		
		else {
			boolean updateResult = solGroupAbstractOrderFacade.updateAbstractOrderEntryAttribute(CartModel._TYPECODE, cartCode, productCode, SolgroupCoreConstants.ORDERENTRY_CIG, cig);
			if(!updateResult) {
				result.setError(Boolean.TRUE);
				result.setErrorDescription(getMessageSource().getMessage(ERROR_UPDATE_CIG, null,getI18nService().getCurrentLocale()));
				result.setPropertyValue(SolgroupCoreConstants.ORDERENTRY_CIG);
			}
		}
		
		if(result.getError().equals(Boolean.TRUE)) {
			result.setSuccessDescription("");
		}
		return result;

	}


	protected SingleOrderEntryPropertyUpdateResult updateCup(String cup, String productCode, String cartCode) {
		SingleOrderEntryPropertyUpdateResult result = new SingleOrderEntryPropertyUpdateResult();
		result.setError(Boolean.FALSE);
		result.setSuccessDescription(getMessageSource().getMessage(SUCCESS_UPDATE_ORDER_ENTRY_PROPERTIES, null,getI18nService().getCurrentLocale()));
		
		
		// Validity check
		CupFormValidator cupFormValidator = new CupFormValidator();
		BindingResult validationResult = new ValidationError("updateCupPropertyForm"); 
		cupFormValidator.validate(cup, validationResult);
		
		if(validationResult.hasErrors()) {
			result.setError(Boolean.TRUE);
			result.setErrorDescription(getMessageSource().getMessage(validationResult.getAllErrors().get(0).getDefaultMessage(), null,getI18nService().getCurrentLocale()));
			result.setPropertyValue(SolgroupCoreConstants.ORDERENTRY_CUP);
		}
		
		
		else {
			boolean updateResult = solGroupAbstractOrderFacade.updateAbstractOrderEntryAttribute(CartModel._TYPECODE, cartCode, productCode, SolgroupCoreConstants.ORDERENTRY_CUP, cup);
			if(!updateResult) {
				result.setError(Boolean.TRUE);
				result.setErrorDescription(getMessageSource().getMessage(ERROR_UPDATE_CUP, null,getI18nService().getCurrentLocale()));
				result.setPropertyValue(SolgroupCoreConstants.ORDERENTRY_CUP);
			}
		}
		
		if(result.getError().equals(Boolean.TRUE)) {
			result.setSuccessDescription("");
		}
		return result;

	}


	
	protected SingleOrderEntryPropertyUpdateResult updateOrderDate(String orderDate, String productCode, String cartCode) {
		SingleOrderEntryPropertyUpdateResult result = new SingleOrderEntryPropertyUpdateResult();
		result.setError(Boolean.FALSE);
		result.setSuccessDescription(getMessageSource().getMessage(SUCCESS_UPDATE_ORDER_ENTRY_PROPERTIES, null,getI18nService().getCurrentLocale()));
		
		
		// Validity check
		OrderDateFormValidator orderDateFormValidator = new OrderDateFormValidator();
		BindingResult validationResult = new ValidationError("updateOrderDatePropertyForm"); 
		orderDateFormValidator.validate(orderDate, validationResult);
		
		if(validationResult.hasErrors()) {
			result.setError(Boolean.TRUE);
			result.setErrorDescription(getMessageSource().getMessage(validationResult.getAllErrors().get(0).getDefaultMessage(), null,getI18nService().getCurrentLocale()));
			result.setPropertyValue(SolgroupCoreConstants.ORDERENTRY_ORDER_DATA);
		}
		
		
		else {
			boolean updateResult = solGroupAbstractOrderFacade.updateAbstractOrderEntryAttribute(CartModel._TYPECODE, cartCode, productCode, SolgroupCoreConstants.ORDERENTRY_ORDER_DATA, orderDate);
			if(!updateResult) {
				result.setError(Boolean.TRUE);
				result.setErrorDescription(getMessageSource().getMessage(ERROR_UPDATE_ORDERDATE, null,getI18nService().getCurrentLocale()));
				result.setPropertyValue(SolgroupCoreConstants.ORDERENTRY_ORDER_DATA);
			}
		}
		
		if(result.getError().equals(Boolean.TRUE)) {
			result.setSuccessDescription("");
		}
		return result;

	}

	
	@Override
	public SiteConfigService getSiteConfigService()
	{
		return siteConfigService;
	}

	protected CartFacade getCartFacade()
	{
		return cartFacade;
	}

	@Override
	protected SessionService getSessionService()
	{
		return sessionService;
	}

	protected AcceleratorCheckoutFacade getCheckoutFacade()
	{
		return checkoutFacade;
	}

}
