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
package com.solgroup.storefront.controllers.pages;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.solgroup.acceleratorfacades.csv.impl.DefaultSolgroupCsvFacade;
import com.solgroup.cart.GlobalOrderEntryPropertiesUpdateResult;
import com.solgroup.cart.SingleOrderEntryPropertyUpdateResult;
import com.solgroup.facades.abstractOrder.SolGroupAbstractOrderFacade;
import com.solgroup.facades.cart.SolGroupB2BCommerceCartFacade;
import com.solgroup.facades.order.SolGroupCheckoutFacade;
import com.solgroup.facades.product.impl.DefaultSolGroupProductVariantFacade;
import com.solgroup.storefront.controllers.ControllerConstants;
import com.solgroup.storefront.forms.UpdateCgiForm;
import com.solgroup.storefront.forms.UpdateCupForm;
import com.solgroup.storefront.forms.UpdateDataOrdineForm;
import com.solgroup.storefront.forms.UpdatePoForm;

import de.hybris.platform.acceleratorfacades.cart.action.CartEntryAction;
import de.hybris.platform.acceleratorfacades.cart.action.CartEntryActionFacade;
import de.hybris.platform.acceleratorfacades.cart.action.exceptions.CartEntryActionException;
import de.hybris.platform.acceleratorfacades.flow.impl.SessionOverrideCheckoutFlowFacade;
import de.hybris.platform.acceleratorservices.controllers.page.PageType;
import de.hybris.platform.acceleratorservices.enums.CheckoutFlowEnum;
import de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.ResourceBreadcrumbBuilder;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCartPageController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.SaveCartForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.UpdateQuantityForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.VoucherForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.SaveCartFormValidator;
import de.hybris.platform.acceleratorstorefrontcommons.util.XSSFilterUtil;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.SaveCartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.CommerceSaveCartParameterData;
import de.hybris.platform.commercefacades.order.data.CommerceSaveCartResultData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.quote.data.QuoteData;
import de.hybris.platform.commercefacades.voucher.VoucherFacade;
import de.hybris.platform.commercefacades.voucher.exceptions.VoucherOperationException;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.CommerceSaveCartException;
import de.hybris.platform.core.enums.QuoteState;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.util.Config;


/**
 * Controller for cart page
 */
@Controller
@RequestMapping(value = "/cart")
public class CartPageController extends AbstractCartPageController
{
	public static final String SHOW_CHECKOUT_STRATEGY_OPTIONS = "storefront.show.checkout.flows";
	public static final String ERROR_MSG_TYPE = "errorMsg";
	public static final String SUCCESSFUL_MODIFICATION_CODE = "success";
	public static final String VOUCHER_FORM = "voucherForm";
	public static final String SITE_QUOTES_ENABLED = "site.quotes.enabled.";
	private static final String CART_CHECKOUT_ERROR = "cart.checkout.error";

	private static final String ACTION_CODE_PATH_VARIABLE_PATTERN = "{actionCode:.*}";

	private static final String REDIRECT_CART_URL = REDIRECT_PREFIX + "/cart";
	private static final String REDIRECT_QUOTE_EDIT_URL = REDIRECT_PREFIX + "/quote/%s/edit/";
	private static final String REDIRECT_QUOTE_VIEW_URL = REDIRECT_PREFIX + "/my-account/my-quotes/%s/";
	private static final String REDIRECT_SAVEDCART_URL = REDIRECT_PREFIX + "/import";

	private static final Logger LOG = Logger.getLogger(CartPageController.class);

	@Resource(name = "simpleBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder resourceBreadcrumbBuilder;

	@Resource(name = "enumerationService")
	private EnumerationService enumerationService;

    @Resource(name = "solGroupProductVariantFacade")
    private DefaultSolGroupProductVariantFacade defaultSolGroupProductVariantFacade;

	@Resource(name = "saveCartFacade")
	private SaveCartFacade saveCartFacade;

	@Resource(name = "saveCartFormValidator")
	private SaveCartFormValidator saveCartFormValidator;

	@Resource(name = "csvFacade")
	private DefaultSolgroupCsvFacade csvFacade;

	@Resource(name = "voucherFacade")
	private VoucherFacade voucherFacade;

	@Resource(name = "baseSiteService")
	private BaseSiteService baseSiteService;

	@Resource(name = "cartEntryActionFacade")
	private CartEntryActionFacade cartEntryActionFacade;
	
	@Resource(name = "b2bCheckoutFacade")
	private SolGroupCheckoutFacade b2bCheckoutFacade;
	
	@Resource(name = "solGroupB2BCommerceCartFacade")
	private SolGroupB2BCommerceCartFacade solGroupB2BCommerceCartFacade;
	
	@Resource(name = "solGroupAbstractOrderFacade")
	private SolGroupAbstractOrderFacade solGroupAbstractOrderFacade;
	
	@ModelAttribute("showCheckoutStrategies")
	public boolean isCheckoutStrategyVisible()
	{
		return getSiteConfigService().getBoolean(SHOW_CHECKOUT_STRATEGY_OPTIONS, false);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showCart(final Model model) throws CMSItemNotFoundException
	{
		return prepareCartUrl(model);
	}

	protected String prepareCartUrl(final Model model) throws CMSItemNotFoundException
	{
		final Optional<String> quoteEditUrl = getQuoteUrl();
		if (quoteEditUrl.isPresent())
		{
			return quoteEditUrl.get();
		}
		else
		{
			prepareDataForPage(model);

			return ControllerConstants.Views.Pages.Cart.CartPage;
		}
	}

	protected Optional<String> getQuoteUrl()
	{
		final QuoteData quoteData = getCartFacade().getSessionCart().getQuoteData();

		return quoteData != null
				? (QuoteState.BUYER_OFFER.equals(quoteData.getState())
						? Optional.of(String.format(REDIRECT_QUOTE_VIEW_URL, urlEncode(quoteData.getCode())))
						: Optional.of(String.format(REDIRECT_QUOTE_EDIT_URL, urlEncode(quoteData.getCode()))))
				: Optional.empty();
	}

	/**
	 * Handle the '/cart/checkout' request url. This method checks to see if the cart is valid before allowing the
	 * checkout to begin. Note that this method does not require the user to be authenticated and therefore allows us to
	 * validate that the cart is valid without first forcing the user to login. The cart will be checked again once the
	 * user has logged in.
	 *
	 * @return The page to redirect to
	 */
	@RequestMapping(value = "/checkout", method = RequestMethod.GET)
	@RequireHardLogIn
	public String cartCheck(final RedirectAttributes redirectModel) throws CommerceCartModificationException
	{
		SessionOverrideCheckoutFlowFacade.resetSessionOverrides();

		if (!getCartFacade().hasEntries())
		{
			LOG.info("Missing or empty cart");

			// No session cart or empty session cart. Bounce back to the cart page.
			GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "basket.error.checkout.empty.cart",
					null);
			return REDIRECT_CART_URL;
		}


		if (validateCart(redirectModel))
		{
			GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, CART_CHECKOUT_ERROR, null);
			return REDIRECT_CART_URL;
		}

		// Redirect to the start of the checkout flow to begin the checkout process
		// We just redirect to the generic '/checkout' page which will actually select the checkout flow
		// to use. The customer is not necessarily logged in on this request, but will be forced to login
		// when they arrive on the '/checkout' page.
		return REDIRECT_PREFIX + "/checkout";
	}

    @RequestMapping(value = "/getProductVariantMatrix", method = RequestMethod.GET)
    public String getProductVariantMatrix(@RequestParam("productCode") final String productCode,
            @RequestParam(value = "readOnly", required = false, defaultValue = "false") final String readOnly,
            final Model model) {

        final CartData cartData = getCartFacade().getSessionCart();

        final ProductData productData = defaultSolGroupProductVariantFacade.getProductForCodeAndOptions(productCode,
                Arrays.asList(ProductOption.BASIC, ProductOption.CATEGORIES, ProductOption.VARIANT_MATRIX_EXTENDED,
                        ProductOption.VARIANT_MATRIX_PRICE, ProductOption.VARIANT_MATRIX_MEDIA,
                        ProductOption.VARIANT_MATRIX_STOCK, ProductOption.VARIANT_MATRIX_URL),
                cartData);

        model.addAttribute("product", productData);
        model.addAttribute("readOnly", Boolean.valueOf(readOnly));

        return ControllerConstants.Views.Fragments.Cart.ExpandGridInCart;
    }

	// This controller method is used to allow the site to force the visitor through a specified checkout flow.
	// If you only have a static configured checkout flow then you can remove this method.
	@RequestMapping(value = "/checkout/select-flow", method = RequestMethod.GET)
	@RequireHardLogIn
	public String initCheck(final Model model, final RedirectAttributes redirectModel,
			@RequestParam(value = "flow", required = false) final String flow,
			@RequestParam(value = "pci", required = false) final String pci) throws CommerceCartModificationException
	{
		SessionOverrideCheckoutFlowFacade.resetSessionOverrides();

		if (!getCartFacade().hasEntries())
		{
			LOG.info("Missing or empty cart");

			// No session cart or empty session cart. Bounce back to the cart page.
			GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "basket.error.checkout.empty.cart",
					null);
			return REDIRECT_CART_URL;
		}

		// Override the Checkout Flow setting in the session
		if (StringUtils.isNotBlank(flow))
		{
			final CheckoutFlowEnum checkoutFlow = enumerationService.getEnumerationValue(CheckoutFlowEnum.class,
					StringUtils.upperCase(flow));
			SessionOverrideCheckoutFlowFacade.setSessionOverrideCheckoutFlow(checkoutFlow);
		}

		// Override the Checkout PCI setting in the session
		if (StringUtils.isNotBlank(pci))
		{
			final CheckoutPciOptionEnum checkoutPci = enumerationService.getEnumerationValue(CheckoutPciOptionEnum.class,
					StringUtils.upperCase(pci));
			SessionOverrideCheckoutFlowFacade.setSessionOverrideSubscriptionPciOption(checkoutPci);
		}

		// Redirect to the start of the checkout flow to begin the checkout process
		// We just redirect to the generic '/checkout' page which will actually select the checkout flow
		// to use. The customer is not necessarily logged in on this request, but will be forced to login
		// when they arrive on the '/checkout' page.
		return REDIRECT_PREFIX + "/checkout";
	}

	@RequestMapping(value = "/entrygroups/{groupNumber}", method = RequestMethod.POST)
	public String removeGroup(@PathVariable("groupNumber") final Integer groupNumber, final Model model,
			final RedirectAttributes redirectModel)
	{
		final CartModificationData cartModification;
		try
		{
			cartModification = getCartFacade().removeEntryGroup(groupNumber);
			if (cartModification != null && !StringUtils.isEmpty(cartModification.getStatusMessage()))
			{
				GlobalMessages.addErrorMessage(model, cartModification.getStatusMessage());
			}
		}
		catch (final CommerceCartModificationException e)
		{
			LOG.error(e.getMessage(), e);
			GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "basket.export.cart.error", null);
		}
		return REDIRECT_CART_URL;
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String updateCartQuantities(@RequestParam("entryNumber") final long entryNumber, final Model model,
			@Valid final UpdateQuantityForm form, final BindingResult bindingResult, final HttpServletRequest request,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		if (bindingResult.hasErrors())
		{
			for (final ObjectError error : bindingResult.getAllErrors())
			{
				if ("typeMismatch".equals(error.getCode()))
				{
					GlobalMessages.addErrorMessage(model, "basket.error.quantity.invalid");
				}
				else
				{
					GlobalMessages.addErrorMessage(model, error.getDefaultMessage());
				}
			}
		}
		else if (getCartFacade().hasEntries())
		{
			try
			{
				final CartModificationData cartModification = getCartFacade().updateCartEntry(entryNumber,
						form.getQuantity().longValue());
				addFlashMessage(form, request, redirectModel, cartModification);

				// Redirect to the cart page on update success so that the browser doesn't re-post again
				return getCartPageRedirectUrl();
			}
			catch (final CommerceCartModificationException ex)
			{
				LOG.warn("Couldn't update product with the entry number: " + entryNumber + ".", ex);
			}
		}

		// if could not update cart, display cart/quote page again with error
		return prepareCartUrl(model);
	}
	
	@RequestMapping(value = "/updateAllProperties", method = RequestMethod.GET)
	@ResponseBody
	public GlobalOrderEntryPropertiesUpdateResult updateAll(@RequestParam("purchaseOrderNumber") final String purchaseOrderNumber,
			@RequestParam("dataOrdine") final String dataOrdine,
			@RequestParam("cig") final String cig,
			@RequestParam("cup") final String cup,final Model model){
		
		return super.updateAllOrderEntryProperties(purchaseOrderNumber, cig, cup, dataOrdine);

		
	}
	
	@RequestMapping(value = "/updatePo", method = RequestMethod.GET)
	@ResponseBody
	public SingleOrderEntryPropertyUpdateResult updateCartPo(
			@RequestParam(value="purchaseOrderNumber", required=true) final String purchaseOrderNumber, 
			@RequestParam(value="product", required=true) final String productCode,
			@RequestParam(value="cartCode", required=false) final String cartCode,
			final Model model)
	{
		return super.updatePurchaseOrderNumber(purchaseOrderNumber, productCode, cartCode);
	}

	
	
	@RequestMapping(value = "/updateCig", method = RequestMethod.GET)
	@ResponseBody
	public SingleOrderEntryPropertyUpdateResult updateCartCig(
			@RequestParam(value="cig", required=true) final String cig, 
			@RequestParam(value="product", required=true) final String productCode,
			@RequestParam(value="cartCode", required=false) final String cartCode,
			final Model model)
	{
		return super.updateCig(cig, productCode, cartCode);
	}

	@RequestMapping(value = "/updateCup", method = RequestMethod.GET)
	@ResponseBody
	public SingleOrderEntryPropertyUpdateResult updateCartCup(
			@RequestParam(value="cup", required=true) final String cup, 
			@RequestParam(value="product", required=true) final String productCode,
			@RequestParam(value="cartCode", required=false) final String cartCode,
			final Model model)
	{
		return super.updateCup(cup, productCode, cartCode);
	}


	@RequestMapping(value = "/updateOrderDate", method = RequestMethod.GET)
	@ResponseBody
	public SingleOrderEntryPropertyUpdateResult updateCartOrderDate(
			@RequestParam(value="orderDate", required=true) final String orderDate, 
			@RequestParam(value="product", required=true) final String productCode,
			@RequestParam(value="cartCode", required=false) final String cartCode,
			final Model model)
	{
		return super.updateOrderDate(orderDate, productCode, cartCode);
	}

	
	
	
	
	
	
	
	
	
	/*
	@RequestMapping(value = "/updateMultiD/updatePo", method = RequestMethod.POST)
	@ResponseBody
	public SingleOrderEntryPropertyUpdateResult updateCartPoMultiD(@RequestParam("entryNumber") final long entryNumber, @RequestParam("productCode") final String productCode, final Model model,
			@Valid final UpdatePoForm form, final BindingResult bindingResult, final HttpServletRequest request,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{

		SingleOrderEntryPropertyUpdateResult result = new SingleOrderEntryPropertyUpdateResult();
		
		if (bindingResult.hasErrors())
		{
			for (final ObjectError error : bindingResult.getAllErrors())
			{
				if ("typeMismatch".equals(error.getCode()))
				{
					GlobalMessages.addErrorMessage(model, "basket.error.quantity.invalid");
				}
				else
				{
					GlobalMessages.addErrorMessage(model, error.getDefaultMessage());
				}
			}
			result.setError(Boolean.TRUE);
		
		}
		else if (getCartFacade().hasEntries())
		{
			
			boolean po_valid = StringUtils.isAlphanumeric(form.getPurchaseOrderNumber()) && form.getPurchaseOrderNumber().length()<=SolgroupCoreConstants.PURCHASE_ORDER_NUMBER_MAX_LENGTH;
			if(!po_valid) {
				result.setError(Boolean.TRUE);
			}
			else {
				boolean updateResult = solGroupAbstractOrderFacade.updateAbstractOrderEntryAttribute(CartModel._TYPECODE, null, productCode, SolgroupCoreConstants.ORDERENTRY_PURCHASE_ORDER_NUMER, form.getPurchaseOrderNumber()); 
				if(updateResult) {
					result.setError(Boolean.FALSE);
				}
				else {
					result.setError(Boolean.TRUE);
				}
			}

			
			
		}

		if(result.getError().equals(Boolean.TRUE)) {
			//result.setErrorDescription(getMessageSource().getMessage(INVALID_PO_ERROR_KEY, null,getI18nService().getCurrentLocale()));
			result.setPropertyValue(form.getPurchaseOrderNumber());
		}
		return result;

	}
	*/
	
	/*
	@RequestMapping(value = "/updateCgi", method = RequestMethod.POST)
	@ResponseBody
	public SingleOrderEntryPropertyUpdateResult updateCartCgi(@RequestParam("entryNumber") final long entryNumber, final Model model,
			@Valid final UpdateCgiForm form, final BindingResult bindingResult, final HttpServletRequest request,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		
		SingleOrderEntryPropertyUpdateResult result = new SingleOrderEntryPropertyUpdateResult();

		
		if (bindingResult.hasErrors())
		{
			for (final ObjectError error : bindingResult.getAllErrors())
			{
				if ("typeMismatch".equals(error.getCode()))
				{
					GlobalMessages.addErrorMessage(model, "basket.error.quantity.invalid");
				}
				else
				{
					GlobalMessages.addErrorMessage(model, error.getDefaultMessage());
				}
			}
			result.setError(Boolean.TRUE);
		}
		else if (getCartFacade().hasEntries())
		{

			//return solGroupAbstractOrderFacade.updateAbstractOrderEntryAttribute(CartModel._TYPECODE, null, new Long(entryNumber), SolgroupCoreConstants.ORDERENTRY_CIG, form.getCgi());
			
		}

		// if could not update cart, display cart/quote page again with error
		//return Boolean.FALSE;
		return null;
	}
	*/
	
	/*
	@RequestMapping(value = "/updateMultiD/updateCgi", method = RequestMethod.POST)
	@ResponseBody
	public Boolean updateUpdateMultiDCartCgi(@RequestParam("entryNumber") final long entryNumber, @RequestParam("productCode") final String productCode, final Model model,
			@Valid final UpdateCgiForm form, final BindingResult bindingResult, final HttpServletRequest request,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		if (bindingResult.hasErrors())
		{
			for (final ObjectError error : bindingResult.getAllErrors())
			{
				if ("typeMismatch".equals(error.getCode()))
				{
					GlobalMessages.addErrorMessage(model, "basket.error.quantity.invalid");
				}
				else
				{
					GlobalMessages.addErrorMessage(model, error.getDefaultMessage());
				}
			}
		}
		else if (getCartFacade().hasEntries())
		{

			return solGroupAbstractOrderFacade.updateAbstractOrderEntryAttribute(CartModel._TYPECODE, null, productCode, SolgroupCoreConstants.ORDERENTRY_CIG, form.getCgi());
			
		}

		// if could not update cart, display cart/quote page again with error
		return Boolean.FALSE;
	}
	*/
	
	/*
	@RequestMapping(value = "/updateCup", method = RequestMethod.POST)
	@ResponseBody
	public Boolean updateCartCup(@RequestParam("entryNumber") final long entryNumber, final Model model,
			@Valid final UpdateCupForm form, final BindingResult bindingResult, final HttpServletRequest request,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		if (bindingResult.hasErrors())
		{
			for (final ObjectError error : bindingResult.getAllErrors())
			{
				if ("typeMismatch".equals(error.getCode()))
				{
					GlobalMessages.addErrorMessage(model, "basket.error.quantity.invalid");
				}
				else
				{
					GlobalMessages.addErrorMessage(model, error.getDefaultMessage());
				}
			}
		}
		else if (getCartFacade().hasEntries())
		{

			return solGroupAbstractOrderFacade.updateAbstractOrderEntryAttribute(CartModel._TYPECODE, null, new Long(entryNumber), SolgroupCoreConstants.ORDERENTRY_CUP, form.getCup());
			
		}

		// if could not update cart, display cart/quote page again with error
		return Boolean.FALSE;
	}
	*/
	
	/*
	@RequestMapping(value = "/updateMultiD/updateCup", method = RequestMethod.POST)
	@ResponseBody
	public Boolean updateCartCupMultiD(@RequestParam("entryNumber") final long entryNumber, @RequestParam("productCode") final String productCode, final Model model,
			@Valid final UpdateCupForm form, final BindingResult bindingResult, final HttpServletRequest request,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		if (bindingResult.hasErrors())
		{
			for (final ObjectError error : bindingResult.getAllErrors())
			{
				if ("typeMismatch".equals(error.getCode()))
				{
					GlobalMessages.addErrorMessage(model, "basket.error.quantity.invalid");
				}
				else
				{
					GlobalMessages.addErrorMessage(model, error.getDefaultMessage());
				}
			}
		}
		else if (getCartFacade().hasEntries())
		{
			
			return solGroupAbstractOrderFacade.updateAbstractOrderEntryAttribute(CartModel._TYPECODE, null, productCode, SolgroupCoreConstants.ORDERENTRY_CUP, form.getCup());
			
		}

		// if could not update cart, display cart/quote page again with error
		return Boolean.FALSE;
	}
	*/
	
	/*
	@RequestMapping(value = "/updateDataOrdine", method = RequestMethod.POST)
	@ResponseBody
	public Boolean updateCartDataOrdine(@RequestParam("entryNumber") final long entryNumber, final Model model,
			@Valid final UpdateDataOrdineForm form, final BindingResult bindingResult, final HttpServletRequest request,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		if (bindingResult.hasErrors())
		{
			for (final ObjectError error : bindingResult.getAllErrors())
			{
				if ("typeMismatch".equals(error.getCode()))
				{
					GlobalMessages.addErrorMessage(model, "basket.error.quantity.invalid");
				}
				else
				{
					GlobalMessages.addErrorMessage(model, error.getDefaultMessage());
				}
			}
		}
		else if (getCartFacade().hasEntries())
		{

			return solGroupAbstractOrderFacade.updateAbstractOrderEntryAttribute(CartModel._TYPECODE, null, new Long(entryNumber), SolgroupCoreConstants.ORDERENTRY_ORDER_DATA, form.getDataOrdine());
			
		}

		return Boolean.FALSE;
	}
	*/
	
	
	/*@RequestMapping(value = "/updateMultiD/updateDataOrdine", method = RequestMethod.POST)
	@ResponseBody
	public Boolean updateCartDataOrdineMultiD(@RequestParam("entryNumber") final long entryNumber, @RequestParam("productCode") final String productCode, final Model model,
			@Valid final UpdateDataOrdineForm form, final BindingResult bindingResult, final HttpServletRequest request,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		if (bindingResult.hasErrors())
		{
			for (final ObjectError error : bindingResult.getAllErrors())
			{
				if ("typeMismatch".equals(error.getCode()))
				{
					GlobalMessages.addErrorMessage(model, "basket.error.quantity.invalid");
				}
				else
				{
					GlobalMessages.addErrorMessage(model, error.getDefaultMessage());
				}
			}
		}
		else if (getCartFacade().hasEntries())
		{
			
			return solGroupAbstractOrderFacade.updateAbstractOrderEntryAttribute(CartModel._TYPECODE, null, productCode, SolgroupCoreConstants.ORDERENTRY_ORDER_DATA, form.getDataOrdine());			
		}

		return Boolean.FALSE;
	}
	*/

	@Override
	protected void prepareDataForPage(final Model model) throws CMSItemNotFoundException
	{
		super.prepareDataForPage(model);
		createProductEntryPoList(model);
		createProductEntryCgiList(model);
		createProductEntryCupList(model);
		createProductEntryDataOrdineList(model);

		if (!model.containsAttribute(VOUCHER_FORM))
		{
			model.addAttribute(VOUCHER_FORM, new VoucherForm());
		}

		// Because DefaultSiteConfigService.getProperty() doesn't set default boolean value for undefined property,
		// this property key was generated to use Config.getBoolean() method
		final String siteQuoteProperty = SITE_QUOTES_ENABLED.concat(getBaseSiteService().getCurrentBaseSite().getUid());
		model.addAttribute("siteQuoteEnabled", Config.getBoolean(siteQuoteProperty, Boolean.FALSE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("breadcrumb.cart"));
		model.addAttribute("pageType", PageType.CART.name());
	}
	
	protected void createProductEntryPoList(final Model model)
	{
		CartData cartData = b2bCheckoutFacade.getCheckoutCart();
		
		if (cartData.getEntries() != null && !cartData.getEntries().isEmpty())
		{
			for (final OrderEntryData entry : cartData.getEntries())
			{
				final UpdatePoForm uqf = new UpdatePoForm();
				uqf.setPurchaseOrderNumber(entry.getPurchaseOrderNumber());
				model.addAttribute("updatePoForm" + entry.getEntryNumber(), uqf);
			}
		}

		
	}
	
	protected void createProductEntryCgiList(final Model model)
	{
		CartData cartData = b2bCheckoutFacade.getCheckoutCart();
		
		if (cartData.getEntries() != null && !cartData.getEntries().isEmpty())
		{
			for (final OrderEntryData entry : cartData.getEntries())
			{
				final UpdateCgiForm uqf = new UpdateCgiForm();
				uqf.setCgi(entry.getCgi());
				model.addAttribute("updateCgiForm" + entry.getEntryNumber(), uqf);
			}
		}

		
	}
	
	protected void createProductEntryCupList(final Model model)
	{
		CartData cartData = b2bCheckoutFacade.getCheckoutCart();
		
		if (cartData.getEntries() != null && !cartData.getEntries().isEmpty())
		{
			for (final OrderEntryData entry : cartData.getEntries())
			{
				final UpdateCupForm uqf = new UpdateCupForm();
				uqf.setCup(entry.getCup());
				model.addAttribute("updateCupForm" + entry.getEntryNumber(), uqf);
			}
		}

		
	}
	
	protected void createProductEntryDataOrdineList(final Model model)
	{
		CartData cartData = b2bCheckoutFacade.getCheckoutCart();
		if (cartData.getEntries() != null && !cartData.getEntries().isEmpty())
		{
			for (final OrderEntryData entry : cartData.getEntries())
			{
				final UpdateDataOrdineForm uqf = new UpdateDataOrdineForm();
				uqf.setDataOrdine(entry.getDataOrdine());
				model.addAttribute("updateDataOrdineForm" + entry.getEntryNumber(), uqf);
			}
		}

		
	}

	protected void addFlashMessage(final UpdateQuantityForm form, final HttpServletRequest request,
			final RedirectAttributes redirectModel, final CartModificationData cartModification)
	{
		if (cartModification.getQuantity() == form.getQuantity().longValue())
		{
			// Success

			if (cartModification.getQuantity() == 0)
			{
				// Success in removing entry
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER, "basket.page.message.remove");
			}
			else
			{
				// Success in update quantity
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER, "basket.page.message.update");
			}
		}
		else if (cartModification.getQuantity() > 0)
		{
			// Less than successful
			GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
					"basket.page.message.update.reducedNumberOfItemsAdded.lowStock", new Object[]
					{ XSSFilterUtil.filter(cartModification.getEntry().getProduct().getName()), Long.valueOf(cartModification.getQuantity()), form.getQuantity(), request.getRequestURL().append(cartModification.getEntry().getProduct().getUrl()) });
		}
		else
		{
			// No more stock available
			GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
					"basket.page.message.update.reducedNumberOfItemsAdded.noStock", new Object[]
					{ XSSFilterUtil.filter(cartModification.getEntry().getProduct().getName()), request.getRequestURL().append(cartModification.getEntry().getProduct().getUrl()) });
		}
	}

	@SuppressWarnings("boxing")
	@ResponseBody
	@RequestMapping(value = "/updateMultiD", method = RequestMethod.POST)
	public CartData updateCartQuantitiesMultiD(@RequestParam("entryNumber") final Integer entryNumber,
			@RequestParam("productCode") final String productCode, final Model model, @Valid final UpdateQuantityForm form,
			final BindingResult bindingResult)
	{
		if (bindingResult.hasErrors())
		{
			for (final ObjectError error : bindingResult.getAllErrors())
			{
				if ("typeMismatch".equals(error.getCode()))
				{
					GlobalMessages.addErrorMessage(model, "basket.error.quantity.invalid");
				}
				else
				{
					GlobalMessages.addErrorMessage(model, error.getDefaultMessage());
				}
			}
		}
		else
		{
			try
			{
				final CartModificationData cartModification = getCartFacade()
						.updateCartEntry(getOrderEntryData(form.getQuantity(), productCode, entryNumber));
				if (cartModification.getStatusCode().equals(SUCCESSFUL_MODIFICATION_CODE))
				{
					GlobalMessages.addMessage(model, GlobalMessages.CONF_MESSAGES_HOLDER, cartModification.getStatusMessage(), null);
				}
				else if (!model.containsAttribute(ERROR_MSG_TYPE))
				{
					GlobalMessages.addMessage(model, GlobalMessages.ERROR_MESSAGES_HOLDER, cartModification.getStatusMessage(), null);
				}
			}
			catch (final CommerceCartModificationException ex)
			{
				LOG.warn("Couldn't update product with the entry number: " + entryNumber + ".", ex);
			}

		}
		return getCartFacade().getSessionCart();
	}

	@SuppressWarnings("boxing")
	protected OrderEntryData getOrderEntryData(final long quantity, final String productCode, final Integer entryNumber)
	{
		final OrderEntryData orderEntry = new OrderEntryData();
		orderEntry.setQuantity(quantity);
		orderEntry.setProduct(new ProductData());
		orderEntry.getProduct().setCode(productCode);
		orderEntry.setEntryNumber(entryNumber);
		return orderEntry;
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	@RequireHardLogIn
	public String saveCart(final SaveCartForm form, final BindingResult bindingResult, final RedirectAttributes redirectModel)
			throws CommerceSaveCartException
	{
		saveCartFormValidator.validate(form, bindingResult);
		if (bindingResult.hasErrors())
		{
			for (final ObjectError error : bindingResult.getAllErrors())
			{
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, error.getCode());
			}
			redirectModel.addFlashAttribute("saveCartForm", form);
		}
		else
		{
			final CommerceSaveCartParameterData commerceSaveCartParameterData = new CommerceSaveCartParameterData();
			commerceSaveCartParameterData.setName(form.getName());
			commerceSaveCartParameterData.setDescription(form.getDescription());
			commerceSaveCartParameterData.setEnableHooks(true);
			try
			{
				final CommerceSaveCartResultData saveCartData = saveCartFacade.saveCart(commerceSaveCartParameterData);
				solGroupB2BCommerceCartFacade.clearSessionCart();
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER, "basket.save.cart.on.success",
						new Object[]
						{ saveCartData.getSavedCartData().getName() });
				
			}
			catch (final CommerceSaveCartException csce)
			{
				LOG.error(csce.getMessage(), csce);
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "basket.save.cart.on.error",
						new Object[]
						{ form.getName() });
			}
		}
		return REDIRECT_CART_URL;
	}

	@RequestMapping(value = "/export", method = RequestMethod.GET, produces = "text/csv")
	public String exportCsvFile(final HttpServletResponse response, final RedirectAttributes redirectModel) throws IOException
	{
		response.setHeader("Content-Disposition", "attachment;filename=cart.csv");

		try (final StringWriter writer = new StringWriter())
		{
			try
			{
				final List<String> headers = new ArrayList<String>();
				headers.add(getMessageSource().getMessage("basket.export.cart.item.sku", null, getI18nService().getCurrentLocale()));
				headers.add(
						getMessageSource().getMessage("basket.export.cart.item.quantity", null, getI18nService().getCurrentLocale()));
				headers.add(getMessageSource().getMessage("basket.export.cart.item.name", null, getI18nService().getCurrentLocale()));
				headers
						.add(getMessageSource().getMessage("basket.export.cart.item.price", null, getI18nService().getCurrentLocale()));

				final CartData cartData = getCartFacade().getSessionCartWithEntryOrdering(false);
				
				csvFacade.generateCsvFromCart(headers, true, cartData, writer);

				StreamUtils.copy(writer.toString(), StandardCharsets.UTF_8, response.getOutputStream());
			}
			catch (final IOException e)
			{
				LOG.error(e.getMessage(), e);
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "basket.export.cart.error", null);

				return REDIRECT_CART_URL;
			}

		}

		//return null;
		return REDIRECT_CART_URL;
	}
	
	@RequestMapping(value = "/exportTemplate", method = RequestMethod.GET, produces = "text/csv")
    public String exportCsvFileHeader(final HttpServletResponse response, final RedirectAttributes redirectModel) throws IOException
    {
        response.setHeader("Content-Disposition", "attachment;filename=example.csv");

        try (final StringWriter writer = new StringWriter())
        {
            final List<String> headers = new ArrayList<String>();
            headers.add(getMessageSource().getMessage("basket.export.cart.item.sku", null, getI18nService().getCurrentLocale()));
            headers.add(
                    getMessageSource().getMessage("basket.export.cart.item.quantity", null, getI18nService().getCurrentLocale()));
            
            csvFacade.generateImportSavedCartTemplate(headers, writer);
            StreamUtils.copy(writer.toString(), StandardCharsets.UTF_8, response.getOutputStream());
        }
        catch (final IOException e)
        {
            LOG.error(e.getMessage(), e);
            GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "basket.export.template.error", null);

            return REDIRECT_CART_URL;
        }
        //return null;
        return REDIRECT_CART_URL;
    }

	@RequestMapping(value = "/voucher/apply", method = RequestMethod.POST)
	public String applyVoucherAction(@Valid final VoucherForm form, final BindingResult bindingResult,
			final RedirectAttributes redirectAttributes)
	{
		try
		{
			if (bindingResult.hasErrors())
			{
				redirectAttributes.addFlashAttribute("errorMsg",
						getMessageSource().getMessage("text.voucher.apply.invalid.error", null, getI18nService().getCurrentLocale()));
			}
			else
			{
				voucherFacade.applyVoucher(form.getVoucherCode());
				redirectAttributes.addFlashAttribute("successMsg",
						getMessageSource().getMessage("text.voucher.apply.applied.success", new Object[]
						{ form.getVoucherCode() }, getI18nService().getCurrentLocale()));
			}
		}
		catch (final VoucherOperationException e)
		{
			redirectAttributes.addFlashAttribute(VOUCHER_FORM, form);
			redirectAttributes.addFlashAttribute("errorMsg",
					getMessageSource().getMessage(e.getMessage(), null,
							getMessageSource().getMessage("text.voucher.apply.invalid.error", null, getI18nService().getCurrentLocale()),
							getI18nService().getCurrentLocale()));
			if (LOG.isDebugEnabled())
			{
				LOG.debug(e.getMessage(), e);
			}

		}

		return REDIRECT_CART_URL;
	}

	@RequestMapping(value = "/voucher/remove", method = RequestMethod.POST)
	public String removeVoucher(@Valid final VoucherForm form, final RedirectAttributes redirectModel)
	{
		try
		{
			voucherFacade.releaseVoucher(form.getVoucherCode());
		}
		catch (final VoucherOperationException e)
		{
			GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "text.voucher.release.error",
					new Object[]
					{ form.getVoucherCode() });
			if (LOG.isDebugEnabled())
			{
				LOG.debug(e.getMessage(), e);
			}

		}
		return REDIRECT_CART_URL;
	}

	public BaseSiteService getBaseSiteService()
	{
		return baseSiteService;
	}

	public void setBaseSiteService(final BaseSiteService baseSiteService)
	{
		this.baseSiteService = baseSiteService;
	}

	@RequestMapping(value = "/entry/execute/" + ACTION_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.POST)
	public String executeCartEntryAction(@PathVariable(value = "actionCode", required = true) final String actionCode,
			final RedirectAttributes redirectModel, @RequestParam("entryNumbers") final Long[] entryNumbers)
	{
		CartEntryAction action = null;
		try
		{
			action = CartEntryAction.valueOf(actionCode);
		}
		catch (final IllegalArgumentException e)
		{
			LOG.error(String.format("Unknown cart entry action %s", actionCode), e);
			GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "basket.page.entry.unknownAction");
			return getCartPageRedirectUrl();
		}

		try
		{
			final Optional<String> redirectUrl = cartEntryActionFacade.executeAction(action, Arrays.asList(entryNumbers));
			final Optional<String> successMessageKey = cartEntryActionFacade.getSuccessMessageKey(action);
			if (successMessageKey.isPresent())
			{
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER, successMessageKey.get());
			}
			if (redirectUrl.isPresent())
			{
				return redirectUrl.get();
			}
			else
			{
				return getCartPageRedirectUrl();
			}
		}
		catch (final CartEntryActionException e)
		{
			LOG.error(String.format("Failed to execute action %s", action), e);
			final Optional<String> errorMessageKey = cartEntryActionFacade.getErrorMessageKey(action);
			if (errorMessageKey.isPresent())
			{
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, errorMessageKey.get());
			}
			return getCartPageRedirectUrl();
		}
	}

	protected String getCartPageRedirectUrl()
	{
		final QuoteData quoteData = getCartFacade().getSessionCart().getQuoteData();
		return quoteData != null ? String.format(REDIRECT_QUOTE_EDIT_URL, urlEncode(quoteData.getCode())) : REDIRECT_CART_URL;
	}

}