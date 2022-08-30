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
package com.solgroup.storefront.controllers.pages.checkout.steps;


import com.solgroup.b2bcommercefacades.company.SolgroupB2BUnitFacade;
import com.solgroup.core.service.b2bunit.SolGroupB2BUnitService;
import com.solgroup.facades.order.SolGroupCheckoutFacade;
import com.solgroup.storefront.controllers.ControllerConstants;
import de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum;
import de.hybris.platform.acceleratorservices.payment.constants.PaymentConstants;
import de.hybris.platform.acceleratorservices.payment.data.PaymentData;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.PaymentDetailsForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.SopPaymentDetailsForm;
import de.hybris.platform.acceleratorstorefrontcommons.util.AddressDataUtil;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CardTypeData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commerceservices.order.CommerceCartCalculationStrategy;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.*;


@Controller
@RequestMapping(value = "/checkout/multi/payment-method")
public class PaymentMethodCheckoutStepController extends AbstractCheckoutStepController
{
	protected static final Map<String, String> CYBERSOURCE_SOP_CARD_TYPES = new HashMap<>();
	private static final String PAYMENT_METHOD = "payment-method";
	private static final String CART_DATA_ATTR = "cartData";
	private static final String CART_REGION_ISO = "IT";

	private static final Logger LOGGER = Logger.getLogger(PaymentMethodCheckoutStepController.class);

	@Resource(name = "addressDataUtil")
	private AddressDataUtil addressDataUtil;
	
	@Resource(name="userService")
	UserService userService;
	
	@Resource(name="baseSiteService")
	BaseSiteService baseSiteService;
	
	@Resource(name = "solGroupCheckoutFacade")
	private SolGroupCheckoutFacade solGroupCheckoutFacade;
	
	@Resource(name="addressConverter")
	private Converter<AddressModel, AddressData> addressConverter;

	@Resource(name = "checkoutCartCalculationStrategy")
	private CommerceCartCalculationStrategy commerceCartCalculationStrategy;

	@Resource(name = "cartService")
	private CartService cartService;
	
	@Resource(name = "b2bUnitFacade")
	private SolgroupB2BUnitFacade solGroupB2BUnitFacade;

	@ModelAttribute("billingCountries")
	public Collection<CountryData> getBillingCountries()
	{
		return getCheckoutFacade().getBillingCountries();
	}

	@ModelAttribute("cardTypes")
	public Collection<CardTypeData> getCardTypes()
	{
		return getCheckoutFacade().getSupportedCardTypes();
	}

	@ModelAttribute("months")
	public List<SelectOption> getMonths()
	{
		final List<SelectOption> months = new ArrayList<SelectOption>();

		months.add(new SelectOption("1", "01"));
		months.add(new SelectOption("2", "02"));
		months.add(new SelectOption("3", "03"));
		months.add(new SelectOption("4", "04"));
		months.add(new SelectOption("5", "05"));
		months.add(new SelectOption("6", "06"));
		months.add(new SelectOption("7", "07"));
		months.add(new SelectOption("8", "08"));
		months.add(new SelectOption("9", "09"));
		months.add(new SelectOption("10", "10"));
		months.add(new SelectOption("11", "11"));
		months.add(new SelectOption("12", "12"));

		return months;
	}

	@ModelAttribute("startYears")
	public List<SelectOption> getStartYears()
	{
		final List<SelectOption> startYears = new ArrayList<SelectOption>();
		final Calendar calender = new GregorianCalendar();

		for (int i = calender.get(Calendar.YEAR); i > calender.get(Calendar.YEAR) - 6; i--)
		{
			startYears.add(new SelectOption(String.valueOf(i), String.valueOf(i)));
		}

		return startYears;
	}

	@ModelAttribute("expiryYears")
	public List<SelectOption> getExpiryYears()
	{
		final List<SelectOption> expiryYears = new ArrayList<SelectOption>();
		final Calendar calender = new GregorianCalendar();

		for (int i = calender.get(Calendar.YEAR); i < calender.get(Calendar.YEAR) + 11; i++)
		{
			expiryYears.add(new SelectOption(String.valueOf(i), String.valueOf(i)));
		}

		return expiryYears;
	}

	@Override
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	@RequireHardLogIn
//	@PreValidateQuoteCheckoutStep
//	@PreValidateCheckoutStep(checkoutStep = PAYMENT_METHOD)
	public String enterStep(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException
	{
		getCheckoutFacade().setDeliveryModeIfAvailable();
		setupAddPaymentPage(model);
		
		CartData cartData = solGroupCheckoutFacade.getCheckoutCart();
		
		B2BPaymentTypeData paymentType = cartData.getPaymentType();
		
		model.addAttribute("paymentType", paymentType.getCode());
		
		// Use the checkout PCI strategy for getting the URL for creating new subscriptions.
		final CheckoutPciOptionEnum subscriptionPciOption = getCheckoutFlowFacade().getSubscriptionPciOption();
		setCheckoutStepLinksForModel(model, getCheckoutStep());
		if(CheckoutPaymentType.CARD.getCode().equals(paymentType.getCode())){
			if (CheckoutPciOptionEnum.HOP.equals(subscriptionPciOption))
			{
				// Redirect the customer to the HOP page or show error message if it fails (e.g. no HOP configurations).
				try
				{
					final PaymentData hostedOrderPageData = getPaymentFacade().beginHopCreateSubscription("/checkout/multi/hop/response",
							"/integration/merchant_callback");
					model.addAttribute("hostedOrderPageData", hostedOrderPageData);
	
					final boolean hopDebugMode = getSiteConfigService().getBoolean(PaymentConstants.PaymentProperties.HOP_DEBUG_MODE,
							false);
					model.addAttribute("hopDebugMode", Boolean.valueOf(hopDebugMode));
	
					return ControllerConstants.Views.Pages.MultiStepCheckout.HostedOrderPostPage;
				}
				catch (final Exception e)
				{
					LOGGER.error("Failed to build beginCreateSubscription request", e);
					GlobalMessages.addErrorMessage(model, "checkout.multi.paymentMethod.addPaymentDetails.generalError");
				}
			}
			else if (CheckoutPciOptionEnum.SOP.equals(subscriptionPciOption))
			{
				// Build up the SOP form data and render page containing form
				final SopPaymentDetailsForm sopPaymentDetailsForm = new SopPaymentDetailsForm();
				try
				{
					setupSilentOrderPostPage(sopPaymentDetailsForm, model);
	//				return ControllerConstants.Views.Pages.MultiStepCheckout.SilentOrderPostPage;
				}
				catch (final Exception e)
				{
					LOGGER.error("Failed to build beginCreateSubscription request", e);
					GlobalMessages.addErrorMessage(model, "checkout.multi.paymentMethod.addPaymentDetails.generalError");
					model.addAttribute("sopPaymentDetailsForm", sopPaymentDetailsForm);
				}
			}
		}
		else{
			
			AddressData billingAddress = solGroupB2BUnitFacade.getBillingAddress(cartData.getB2bCustomerData().getUid());
			
			
			
//			B2BCustomerModel customerModel = (B2BCustomerModel)userService.getUserForUID(cartData.getB2bCustomerData().getUid());
//			
//			
//			AddressModel billingAddressModel = solGroupB2BUnitService.getBillingAddress(customerModel.getDefaultB2BUnit());
//			
//			
//			CompanyModel company = customerModel.getDefaultB2BUnit();
//			
//			
//			
////			AddressModel addressModel = company.getBillingAddress();
//			
//			Collection<AddressModel> addresses = company.getAddresses();
//			
//			AddressModel addressModel = null;
//			
//			for(AddressModel address : addresses){
//				if(address.getBillingAddress()){
//					addressModel = address;
//					break;
//				}
//			}
//			
//			if(addressModel == null){
//				CompanyModel responsibleCompany = company.getResponsibleCompany();
//				addresses = responsibleCompany.getAddresses();
//				for(AddressModel address : addresses){
//					if(address.getBillingAddress()){
//						addressModel = address;
//						break;
//					}
//				}
//			}
//			
//			if(addressModel == null){
//				for(AddressModel address : addresses){
//					if(address.getShippingAddress()){
//						addressModel = address;
//						break;
//					}
//				}
//			}
			
			
			
			PaymentData paymentData = new PaymentData();
			Map<String, String> parameters = new HashMap<String, String>();
			
			//AddressData addressData = addressConverter.convert(addressModel);
			
//			parameters.put("billTo_city", addressModel.getTown());
//			parameters.put("billTo_company", addressModel.getCompany());
//			parameters.put("billTo_companyTaxID", "");
//			parameters.put("billTo_country", addressModel.getCountry().getIsocode());
//			parameters.put("billTo_customerID", "");
//			parameters.put("billTo_dateOfBirth", "");
//			parameters.put("billTo_email", addressModel.getEmail());
//			parameters.put("billTo_firstName", addressModel.getFirstname());
//			parameters.put("billTo_lastName", addressModel.getLastname());
//			parameters.put("billTo_phoneNumber", addressModel.getPhone1());
//			parameters.put("billTo_postalCode", addressModel.getPostalcode());
//			parameters.put("billTo_state", addressModel.getCountry().getName());
//			parameters.put("billTo_street1", addressModel.getStreetname());
//			parameters.put("billTo_street2", "");
//			if(addressModel.getTitle() != null){
//				parameters.put("billTo_titleCode", addressModel.getTitle().getCode());
//			}
//			model.addAttribute("billingAddress", addressData);

			
			parameters.put("billTo_city", billingAddress.getTown());
			parameters.put("billTo_company", billingAddress.getCompanyName());
			parameters.put("billTo_companyTaxID", "");
			parameters.put("billTo_country", billingAddress.getCountry().getIsocode());
			parameters.put("billTo_customerID", "");
			parameters.put("billTo_dateOfBirth", "");
			parameters.put("billTo_email", billingAddress.getEmail());
			parameters.put("billTo_firstName", billingAddress.getFirstName());
			parameters.put("billTo_lastName", billingAddress.getLastName());
			parameters.put("billTo_phoneNumber", billingAddress.getPhone());
			parameters.put("billTo_postalCode", billingAddress.getPostalCode());
			parameters.put("billTo_state", billingAddress.getCountry().getName());
			parameters.put("billTo_street1", billingAddress.getStreetName());
			parameters.put("billTo_street2", "");
			if(billingAddress.getTitle() != null){
				parameters.put("billTo_titleCode", billingAddress.getTitle());
			}
			model.addAttribute("billingAddress", billingAddress);

			
			paymentData.setParameters(parameters);
			
			final SopPaymentDetailsForm sopPaymentDetailsForm = new SopPaymentDetailsForm();
			model.addAttribute("silentOrderPageData", paymentData);
			sopPaymentDetailsForm.setParameters(paymentData.getParameters());
			model.addAttribute("paymentFormUrl", "/checkout/multi/note/show");
			model.addAttribute(sopPaymentDetailsForm);
			
			final PaymentDetailsForm paymentDetailsForm = new PaymentDetailsForm();
			final AddressForm addressForm = new AddressForm();
//			addressForm.setAddressId(addressModel.getPk().getLongValueAsString());
//			addressForm.setTownCity(addressModel.getTown());
//			addressForm.setCountryIso(addressModel.getCountry().getIsocode());
//			if(addressModel.getRegion()!=null && addressModel.getRegion().getIsocode()!=null) {
//				addressForm.setRegionIso(addressModel.getRegion().getIsocode());
//			}
//			addressForm.setFirstName(addressModel.getFirstname());
//			addressForm.setLastName(addressModel.getLastname());
//			addressForm.setPostcode(addressModel.getPostalcode());
//			if(addressModel.getTitle() != null){
//				addressForm.setTitleCode(addressModel.getTitle().getCode());
//			}
//			addressForm.setPhone(addressModel.getPhone1());
//			addressForm.setLine1(addressModel.getLine1());
//			addressForm.setLine2(addressModel.getLine2());


			
			
			addressForm.setAddressId(billingAddress.getId());
			addressForm.setTownCity(billingAddress.getTown());
			addressForm.setCountryIso(billingAddress.getCountry().getIsocode());
			if(billingAddress.getRegion()!=null && billingAddress.getRegion().getIsocode()!=null) {
				addressForm.setRegionIso(billingAddress.getRegion().getIsocode());
			}
			addressForm.setFirstName(billingAddress.getFirstName());
			addressForm.setLastName(billingAddress.getLastName());
			addressForm.setPostcode(billingAddress.getPostalCode());
			if(billingAddress.getTitle() != null){
				addressForm.setTitleCode(billingAddress.getTitle());
			}
			addressForm.setPhone(billingAddress.getPhone());
			addressForm.setLine1(billingAddress.getLine1());
			addressForm.setLine2(billingAddress.getLine2());
			
			
			
			
			paymentDetailsForm.setBillingAddress(addressForm);
			model.addAttribute(paymentDetailsForm);
		}
		
		

		// If not using HOP or SOP we need to build up the payment details form
//		final PaymentDetailsForm paymentDetailsForm = new PaymentDetailsForm();
//		final AddressForm addressForm = new AddressForm();
//		paymentDetailsForm.setBillingAddress(addressForm);
//		model.addAttribute(paymentDetailsForm);

		CartModel cartModel = cartService.getSessionCart();

		cartModel.setCalculated(false);

		for (AbstractOrderEntryModel cartEntryModel: cartModel.getEntries()){
			cartEntryModel.setCalculated(false);
		}

		try {
			recalculateCart(cartModel);

		} catch (Exception e) {
			LOG.info("Error during cart calculation");
		}

		cartData = solGroupCheckoutFacade.getCheckoutCart();

		model.addAttribute(CART_DATA_ATTR, cartData);

		return ControllerConstants.Views.Pages.MultiStepCheckout.AddPaymentMethodPage;
	}

	@RequestMapping(value =
	{ "/add" }, method = RequestMethod.POST)
	@RequireHardLogIn
	public String add(final Model model, @Valid final PaymentDetailsForm paymentDetailsForm, final BindingResult bindingResult)
			throws CMSItemNotFoundException
	{
		getPaymentDetailsValidator().validate(paymentDetailsForm, bindingResult);
		setupAddPaymentPage(model);

		final CartData cartData = solGroupCheckoutFacade.getCheckoutCart();
		model.addAttribute(CART_DATA_ATTR, cartData);

		if (bindingResult.hasErrors())
		{
			GlobalMessages.addErrorMessage(model, "checkout.error.paymentethod.formentry.invalid");
			return ControllerConstants.Views.Pages.MultiStepCheckout.AddPaymentMethodPage;
		}

		final CCPaymentInfoData paymentInfoData = new CCPaymentInfoData();
		fillInPaymentData(paymentDetailsForm, paymentInfoData);

		final AddressData addressData;
		if (Boolean.FALSE.equals(paymentDetailsForm.getNewBillingAddress()))
		{
			addressData = solGroupCheckoutFacade.getCheckoutCart().getDeliveryAddress();
			if (addressData == null)
			{
				GlobalMessages.addErrorMessage(model,
						"checkout.multi.paymentMethod.createSubscription.billingAddress.noneSelectedMsg");
				return ControllerConstants.Views.Pages.MultiStepCheckout.AddPaymentMethodPage;
			}

			addressData.setBillingAddress(true); // mark this as billing address
		}
		else
		{
			final AddressForm addressForm = paymentDetailsForm.getBillingAddress();
			addressData = addressDataUtil.convertToAddressData(addressForm);
			addressData.setShippingAddress(Boolean.TRUE.equals(addressForm.getShippingAddress()));
			addressData.setBillingAddress(Boolean.TRUE.equals(addressForm.getBillingAddress()));
		}

		getAddressVerificationFacade().verifyAddressData(addressData);
		paymentInfoData.setBillingAddress(addressData);

		final CCPaymentInfoData newPaymentSubscription = getCheckoutFacade().createPaymentSubscription(paymentInfoData);
		if (!checkPaymentSubscription(model, paymentDetailsForm, newPaymentSubscription))
		{
			return ControllerConstants.Views.Pages.MultiStepCheckout.AddPaymentMethodPage;
		}

		model.addAttribute("paymentId", newPaymentSubscription.getId());
		setCheckoutStepLinksForModel(model, getCheckoutStep());

		return getCheckoutStep().nextStep();
	}

	protected boolean checkPaymentSubscription(final Model model, @Valid final PaymentDetailsForm paymentDetailsForm,
			final CCPaymentInfoData newPaymentSubscription)
	{
		if (newPaymentSubscription != null && StringUtils.isNotBlank(newPaymentSubscription.getSubscriptionId()))
		{
			if (Boolean.TRUE.equals(paymentDetailsForm.getSaveInAccount()) && getUserFacade().getCCPaymentInfos(true).size() <= 1)
			{
				getUserFacade().setDefaultPaymentInfo(newPaymentSubscription);
			}
			getCheckoutFacade().setPaymentDetails(newPaymentSubscription.getId());
		}
		else
		{
			GlobalMessages.addErrorMessage(model, "checkout.multi.paymentMethod.createSubscription.failedMsg");
			return false;
		}
		return true;
	}

	protected void fillInPaymentData(@Valid final PaymentDetailsForm paymentDetailsForm, final CCPaymentInfoData paymentInfoData)
	{
		paymentInfoData.setId(paymentDetailsForm.getPaymentId());
		paymentInfoData.setCardType(paymentDetailsForm.getCardTypeCode());
		paymentInfoData.setAccountHolderName(paymentDetailsForm.getNameOnCard());
		paymentInfoData.setCardNumber(paymentDetailsForm.getCardNumber());
		paymentInfoData.setStartMonth(paymentDetailsForm.getStartMonth());
		paymentInfoData.setStartYear(paymentDetailsForm.getStartYear());
		paymentInfoData.setExpiryMonth(paymentDetailsForm.getExpiryMonth());
		paymentInfoData.setExpiryYear(paymentDetailsForm.getExpiryYear());
		if (Boolean.TRUE.equals(paymentDetailsForm.getSaveInAccount()) || getCheckoutCustomerStrategy().isAnonymousCheckout())
		{
			paymentInfoData.setSaved(true);
		}
		paymentInfoData.setIssueNumber(paymentDetailsForm.getIssueNumber());
	}

	@RequestMapping(value = "/remove", method = RequestMethod.POST)
	@RequireHardLogIn
	public String remove(@RequestParam(value = "paymentInfoId") final String paymentMethodId,
			final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException
	{
		getUserFacade().unlinkCCPaymentInfo(paymentMethodId);
		GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.CONF_MESSAGES_HOLDER,
				"text.account.profile.paymentCart.removed");
		return getCheckoutStep().currentStep();
	}

	/**
	 * This method gets called when the "Use These Payment Details" button is clicked. It sets the selected payment
	 * method on the checkout facade and reloads the page highlighting the selected payment method.
	 *
	 * @param selectedPaymentMethodId
	 *           - the id of the payment method to use.
	 * @return - a URL to the page to load.
	 */
	@RequestMapping(value = "/choose", method = RequestMethod.GET)
	@RequireHardLogIn
	public String doSelectPaymentMethod(@RequestParam("selectedPaymentMethodId") final String selectedPaymentMethodId)
	{
		if (StringUtils.isNotBlank(selectedPaymentMethodId))
		{
			getCheckoutFacade().setPaymentDetails(selectedPaymentMethodId);
		}
		return getCheckoutStep().nextStep();
	}

	@RequestMapping(value = "/back", method = RequestMethod.GET)
	@RequireHardLogIn
	@Override
	public String back(final RedirectAttributes redirectAttributes)
	{
		return getCheckoutStep().previousStep();
	}

	@RequestMapping(value = "/next", method = RequestMethod.GET)
	@RequireHardLogIn
	@Override
	public String next(final RedirectAttributes redirectAttributes)
	{
		return getCheckoutStep().nextStep();
	}

	protected CardTypeData createCardTypeData(final String code, final String name)
	{
		final CardTypeData cardTypeData = new CardTypeData();
		cardTypeData.setCode(code);
		cardTypeData.setName(name);
		return cardTypeData;
	}

	protected void setupAddPaymentPage(final Model model) throws CMSItemNotFoundException
	{
		model.addAttribute("metaRobots", "noindex,nofollow");
		model.addAttribute("hasNoPaymentInfo", Boolean.valueOf(getCheckoutFlowFacade().hasNoPaymentInfo()));
		prepareDataForPage(model);
		model.addAttribute(WebConstants.BREADCRUMBS_KEY,
				getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.paymentMethod.breadcrumb"));
		final ContentPageModel contentPage = getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL);
		storeCmsPageInModel(model, contentPage);
		setUpMetaDataForContentPage(model, contentPage);
		setCheckoutStepLinksForModel(model, getCheckoutStep());
	}

	protected void setupSilentOrderPostPage(final SopPaymentDetailsForm sopPaymentDetailsForm, final Model model)
	{
		try
		{
			final PaymentData silentOrderPageData = getPaymentFacade().beginSopCreateSubscription("/checkout/multi/sop/response",
					"/integration/merchant_callback");
			model.addAttribute("silentOrderPageData", silentOrderPageData);
			sopPaymentDetailsForm.setParameters(silentOrderPageData.getParameters());
			model.addAttribute("paymentFormUrl", silentOrderPageData.getPostUrl());
		}
		catch (final IllegalArgumentException e)
		{
			model.addAttribute("paymentFormUrl", "");
			model.addAttribute("silentOrderPageData", null);
			LOGGER.warn("Failed to set up silent order post page", e);
			GlobalMessages.addErrorMessage(model, "checkout.multi.sop.globalError");
		}

		final CartData cartData = solGroupCheckoutFacade.getCheckoutCart();
		model.addAttribute("silentOrderPostForm", new PaymentDetailsForm());
		model.addAttribute(CART_DATA_ATTR, cartData);
		model.addAttribute("deliveryAddress", cartData.getDeliveryAddress());
		model.addAttribute("sopPaymentDetailsForm", sopPaymentDetailsForm);
		model.addAttribute("paymentInfos", getUserFacade().getCCPaymentInfos(true));
		model.addAttribute("sopCardTypes", getSopCardTypes());
		if (StringUtils.isNotBlank(sopPaymentDetailsForm.getBillTo_country()))
		{
			model.addAttribute("regions", getI18NFacade().getRegionsForCountryIso(sopPaymentDetailsForm.getBillTo_country()));
			model.addAttribute("country", sopPaymentDetailsForm.getBillTo_country());
		}
	}

	protected Collection<CardTypeData> getSopCardTypes()
	{
		final Collection<CardTypeData> sopCardTypes = new ArrayList<CardTypeData>();

		final List<CardTypeData> supportedCardTypes = getCheckoutFacade().getSupportedCardTypes();
		for (final CardTypeData supportedCardType : supportedCardTypes)
		{
			// Add credit cards for all supported cards that have mappings for cybersource SOP
			if (CYBERSOURCE_SOP_CARD_TYPES.containsKey(supportedCardType.getCode()))
			{
				sopCardTypes.add(
						createCardTypeData(CYBERSOURCE_SOP_CARD_TYPES.get(supportedCardType.getCode()), supportedCardType.getName()));
			}
		}
		return sopCardTypes;
	}

	protected CheckoutStep getCheckoutStep()
	{
		return getCheckoutStep(PAYMENT_METHOD);
	}

	static
	{
		// Map hybris card type to Cybersource SOP credit card
		CYBERSOURCE_SOP_CARD_TYPES.put("visa", "001");
		CYBERSOURCE_SOP_CARD_TYPES.put("master", "002");
		CYBERSOURCE_SOP_CARD_TYPES.put("amex", "003");
		CYBERSOURCE_SOP_CARD_TYPES.put("diners", "005");
		CYBERSOURCE_SOP_CARD_TYPES.put("maestro", "024");
	}

	protected void recalculateCart(CartModel cart) throws Exception
	{

		if (cart != null)
		{
			//calculationService.recalculate(cart);
			commerceCartCalculationStrategy.calculateCart(cart);
		}
	}

}
