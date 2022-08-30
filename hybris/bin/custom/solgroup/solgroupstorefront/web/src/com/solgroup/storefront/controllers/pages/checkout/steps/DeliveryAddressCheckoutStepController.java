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

import com.solgroup.facades.order.SolGroupCheckoutFacade;
import com.solgroup.storefront.controllers.ControllerConstants;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateQuoteCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.validation.ValidationResults;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;
import de.hybris.platform.acceleratorstorefrontcommons.util.AddressDataUtil;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.address.data.AddressVerificationResult;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commerceservices.address.AddressVerificationDecision;
import de.hybris.platform.commerceservices.order.CommerceCartCalculationStrategy;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.CartService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Config;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;



@Controller
@RequestMapping(value = "/checkout/multi/delivery-address")
public class DeliveryAddressCheckoutStepController extends AbstractCheckoutStepController
{
	private static final String DELIVERY_ADDRESS = "delivery-address";
	private static final String SHOW_SAVE_TO_ADDRESS_BOOK_ATTR = "showSaveToAddressBook";
	
	private static final Logger LOGGER = Logger.getLogger(DeliveryAddressCheckoutStepController.class);

	@Resource(name = "addressDataUtil")
	private AddressDataUtil addressDataUtil;
	
	@Resource(name = "b2bCheckoutFacade")
	private SolGroupCheckoutFacade b2bCheckoutFacade;
	
	@Resource(name = "calculationService")
	private CalculationService calculationService;
	
	@Resource(name = "cartService")
	private CartService cartService;
	
	@Resource(name = "productService")
	private ProductService productService;
	
	
	@Resource(name = "modelService")
	private ModelService modelService;
	
	@Resource(name = "checkoutCartCalculationStrategy")
	private CommerceCartCalculationStrategy commerceCartCalculationStrategy;
	
	

	@Override
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	@RequireHardLogIn
	@PreValidateQuoteCheckoutStep
	@PreValidateCheckoutStep(checkoutStep = DELIVERY_ADDRESS)
	public String enterStep(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException
	{
		b2bCheckoutFacade.setDeliveryAddressIfAvailable();
		
		//TODO remove comment block
		/*CartModel cartModel = cartService.getSessionCart();
		
		List<AbstractOrderEntryModel> entries = cartModel.getEntries();
		
		List<ProductDeliveryCost> realDeliveryCosts = new ArrayList<ProductDeliveryCost>();
		
		Double realDeliveryCost = new Double(0);
		
		for(AbstractOrderEntryModel entry : entries){
			String code = entry.getProduct().getCode();
			ProductModel product = productService.getProductForCode(code);
			List<ZoneDeliveryModeModel> deliveryModes = product.getProductDeliveryMode();
			if(deliveryModes != null && !deliveryModes.isEmpty() && product.getMaterial().getCode().equalsIgnoreCase("mat")){
				ZoneDeliveryModeModel deliveryMode = deliveryModes.get(0);
				ZoneDeliveryModeValueModel value = deliveryMode.getValues().iterator().next();
				Double unitCost = value.getValue();
				Double weight = product.getWeight();
				Long qty = entry.getQuantity();
				
				Double totalWeight = qty * weight;
				
				Double totalCost = totalWeight * unitCost;
				
				ProductDeliveryCost cost = new ProductDeliveryCost();
				cost.setCost(totalCost);
				cost.setDeliveryModeDescription(deliveryMode.getDescription());
				cost.setProduct(product.getCode());
				
				realDeliveryCosts.add(cost);
				
				realDeliveryCost = realDeliveryCost + cost.getCost();
			}
			
		}
		
		cartModel.setCalculated(false);
		
		cartModel.setDeliveryCost(realDeliveryCost);

		try{
			recalculateCart(cartModel);
		}
		catch(Exception e){
			LOGGER.fatal(e);
		}
		
		modelService.save(cartModel);
		modelService.refresh(cartModel);
		
		cartService.setSessionCart(cartModel);*/
		
		final CartData cartData = b2bCheckoutFacade.getCheckoutCart();

		populateCommonModelAttributes(model, cartData, new AddressForm());

		return ControllerConstants.Views.Pages.MultiStepCheckout.AddEditDeliveryAddressPage;
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@RequireHardLogIn
	public String add(final AddressForm addressForm, final BindingResult bindingResult, final Model model,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		final CartData cartData = b2bCheckoutFacade.getCheckoutCart();
		
//		if(cartData.getDeliveryAddress() == null && addressForm.getCountryIso() == null){
		if(addressForm.getCountryIso() != null && addressForm.getSaveInAddressBook()){

			getAddressValidator().validate(addressForm, bindingResult);
			populateCommonModelAttributes(model, cartData, addressForm);
	
			if (bindingResult.hasErrors())
			{
				GlobalMessages.addErrorMessage(model, "address.error.formentry.invalid");
				return ControllerConstants.Views.Pages.MultiStepCheckout.AddEditDeliveryAddressPage;
			}
	
			final AddressData newAddress = addressDataUtil.convertToAddressData(addressForm);
			newAddress.setVisibleInAddressBook(true);
			
			//processAddressVisibilityAndDefault(addressForm, newAddress);
	
			// Verify the address data.
			final AddressVerificationResult<AddressVerificationDecision> verificationResult = getAddressVerificationFacade()
					.verifyAddressData(newAddress);
			final boolean addressRequiresReview = getAddressVerificationResultHandler().handleResult(verificationResult, newAddress,
					model, redirectModel, bindingResult, getAddressVerificationFacade().isCustomerAllowedToIgnoreAddressSuggestions(),
					"checkout.multi.address.updated");
	
			if (addressRequiresReview)
			{
				return ControllerConstants.Views.Pages.MultiStepCheckout.AddEditDeliveryAddressPage;
			}
	
			getUserFacade().addAddress(newAddress);
	
			final AddressData previousSelectedAddress = b2bCheckoutFacade.getCheckoutCart().getDeliveryAddress();
			// Set the new address as the selected checkout delivery address
			getCheckoutFacade().setDeliveryAddress(newAddress);
			if (previousSelectedAddress != null && !previousSelectedAddress.isVisibleInAddressBook())
			{ // temporary address should be removed
				getUserFacade().removeAddress(previousSelectedAddress);
			}
	
//			// Set the new address as the selected checkout delivery address
//			getCheckoutFacade().setDeliveryAddress(newAddress);
		}
		
		

		return getCheckoutStep().nextStep();
	}

	protected void processAddressVisibilityAndDefault(final AddressForm addressForm, final AddressData newAddress)
	{
		if (addressForm.getSaveInAddressBook() != null)
		{
			newAddress.setVisibleInAddressBook(addressForm.getSaveInAddressBook().booleanValue());
			if (addressForm.getSaveInAddressBook().booleanValue() && getUserFacade().isAddressBookEmpty())
			{
				newAddress.setDefaultAddress(true);
			}
		}
		else if (getCheckoutCustomerStrategy().isAnonymousCheckout())
		{
			newAddress.setDefaultAddress(true);
			newAddress.setVisibleInAddressBook(true);
		}
	}

	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	@RequireHardLogIn
	public String editAddressForm(@RequestParam("editAddressCode") final String editAddressCode, final Model model,
			final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException
	{
		final ValidationResults validationResults = getCheckoutStep().validate(redirectAttributes);
		if (getCheckoutStep().checkIfValidationErrors(validationResults))
		{
			return getCheckoutStep().onValidation(validationResults);
		}

		AddressData addressData = null;
		if (StringUtils.isNotEmpty(editAddressCode))
		{
			addressData = getCheckoutFacade().getDeliveryAddressForCode(editAddressCode);
		}

		final AddressForm addressForm = new AddressForm();
		final boolean hasAddressData = addressData != null;
		if (hasAddressData)
		{
			addressDataUtil.convert(addressData, addressForm);
		}

		final CartData cartData = b2bCheckoutFacade.getCheckoutCart();
		populateCommonModelAttributes(model, cartData, addressForm);

		if (addressData != null)
		{
			model.addAttribute(SHOW_SAVE_TO_ADDRESS_BOOK_ATTR, Boolean.valueOf(!addressData.isVisibleInAddressBook()));
		}

		return ControllerConstants.Views.Pages.MultiStepCheckout.AddEditDeliveryAddressPage;
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	@RequireHardLogIn
	public String edit(final AddressForm addressForm, final BindingResult bindingResult, final Model model,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		getAddressValidator().validate(addressForm, bindingResult);

		final CartData cartData = b2bCheckoutFacade.getCheckoutCart();
		populateCommonModelAttributes(model, cartData, addressForm);

		if (bindingResult.hasErrors())
		{
			GlobalMessages.addErrorMessage(model, "address.error.formentry.invalid");
			return ControllerConstants.Views.Pages.MultiStepCheckout.AddEditDeliveryAddressPage;
		}

		final AddressData newAddress = addressDataUtil.convertToAddressData(addressForm);

		processAddressVisibility(addressForm, newAddress);

		newAddress.setDefaultAddress(getUserFacade().isAddressBookEmpty() || getUserFacade().getAddressBook().size() == 1
				|| Boolean.TRUE.equals(addressForm.getDefaultAddress()));

		// Verify the address data.
		final AddressVerificationResult<AddressVerificationDecision> verificationResult = getAddressVerificationFacade()
				.verifyAddressData(newAddress);
		final boolean addressRequiresReview = getAddressVerificationResultHandler().handleResult(verificationResult, newAddress,
				model, redirectModel, bindingResult, getAddressVerificationFacade().isCustomerAllowedToIgnoreAddressSuggestions(),
				"checkout.multi.address.updated");

		if (addressRequiresReview)
		{
			if (StringUtils.isNotEmpty(addressForm.getAddressId()))
			{
				final AddressData addressData = getCheckoutFacade().getDeliveryAddressForCode(addressForm.getAddressId());
				if (addressData != null)
				{
					model.addAttribute(SHOW_SAVE_TO_ADDRESS_BOOK_ATTR, Boolean.valueOf(!addressData.isVisibleInAddressBook()));
					model.addAttribute("edit", Boolean.TRUE);
				}
			}

			return ControllerConstants.Views.Pages.MultiStepCheckout.AddEditDeliveryAddressPage;
		}

		getUserFacade().editAddress(newAddress);
		getCheckoutFacade().setDeliveryModeIfAvailable();
		getCheckoutFacade().setDeliveryAddress(newAddress);

		return getCheckoutStep().nextStep();
	}

	protected void processAddressVisibility(final AddressForm addressForm, final AddressData newAddress)
	{

		if (addressForm.getSaveInAddressBook() == null)
		{
			newAddress.setVisibleInAddressBook(true);
		}
		else
		{
			newAddress.setVisibleInAddressBook(Boolean.TRUE.equals(addressForm.getSaveInAddressBook()));
		}
	}

	@RequestMapping(value = "/remove", method =
	{ RequestMethod.GET, RequestMethod.POST })
	@RequireHardLogIn
	public String removeAddress(@RequestParam("addressCode") final String addressCode, final RedirectAttributes redirectModel,
			final Model model) throws CMSItemNotFoundException
	{
		if (getCheckoutFacade().isRemoveAddressEnabledForCart())
		{
			final AddressData addressData = new AddressData();
			addressData.setId(addressCode);
			getUserFacade().removeAddress(addressData);
			GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER,
					"account.confirmation.address.removed");
		}
		storeCmsPageInModel(model, getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL));
		model.addAttribute("addressForm", new AddressForm());

		return getCheckoutStep().currentStep();
	}

	@RequestMapping(value = "/select", method = RequestMethod.POST)
	@RequireHardLogIn
	public String doSelectSuggestedAddress(final AddressForm addressForm, final RedirectAttributes redirectModel)
	{
		final Set<String> resolveCountryRegions = org.springframework.util.StringUtils
				.commaDelimitedListToSet(Config.getParameter("resolve.country.regions"));

		final AddressData selectedAddress = addressDataUtil.convertToAddressData(addressForm);
		final CountryData countryData = selectedAddress.getCountry();

		if (!resolveCountryRegions.contains(countryData.getIsocode()))
		{
			selectedAddress.setRegion(null);
		}

		if (addressForm.getSaveInAddressBook() != null)
		{
			selectedAddress.setVisibleInAddressBook(addressForm.getSaveInAddressBook().booleanValue());
		}

		if (Boolean.TRUE.equals(addressForm.getEditAddress()))
		{
			getUserFacade().editAddress(selectedAddress);
		}
		else
		{
			getUserFacade().addAddress(selectedAddress);
		}

		final AddressData previousSelectedAddress = b2bCheckoutFacade.getCheckoutCart().getDeliveryAddress();
		// Set the new address as the selected checkout delivery address
		getCheckoutFacade().setDeliveryAddress(selectedAddress);
		if (previousSelectedAddress != null && !previousSelectedAddress.isVisibleInAddressBook())
		{ // temporary address should be removed
			getUserFacade().removeAddress(previousSelectedAddress);
		}

		GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER, "checkout.multi.address.added");

		return getCheckoutStep().nextStep();
	}


	/**
	 * This method gets called when the "Use this Address" button is clicked. It sets the selected delivery address on
	 * the checkout facade - if it has changed, and reloads the page highlighting the selected delivery address.
	 *
	 * @param selectedAddressCode
	 *           - the id of the delivery address.
	 *
	 * @return - a URL to the page to load.
	 */
	@RequestMapping(value = "/select", method = RequestMethod.GET)
	@RequireHardLogIn
	public String doSelectDeliveryAddress(@RequestParam("selectedAddressCode") final String selectedAddressCode,
			final RedirectAttributes redirectAttributes)
	{
//		final ValidationResults validationResults = getCheckoutStep().validate(redirectAttributes);
//		if (getCheckoutStep().checkIfValidationErrors(validationResults))
//		{
//			return getCheckoutStep().onValidation(validationResults);
//		}
		if (StringUtils.isNotBlank(selectedAddressCode))
		{
			final AddressData selectedAddressData = b2bCheckoutFacade.getDeliveryAddressForCode(selectedAddressCode);
			final boolean hasSelectedAddressData = selectedAddressData != null;
			if (hasSelectedAddressData)
			{
				setDeliveryAddress(selectedAddressData);
			}
		}
		return getCheckoutStep().nextStep();
	}

	protected void setDeliveryAddress(final AddressData selectedAddressData)
	{
		final AddressData cartCheckoutDeliveryAddress = b2bCheckoutFacade.getCheckoutCart().getDeliveryAddress();
		if (isAddressIdChanged(cartCheckoutDeliveryAddress, selectedAddressData))
		{
			b2bCheckoutFacade.setDeliveryAddress(selectedAddressData);
			if (cartCheckoutDeliveryAddress != null && !cartCheckoutDeliveryAddress.isVisibleInAddressBook())
			{ // temporary address should be removed
				getUserFacade().removeAddress(cartCheckoutDeliveryAddress);
			}
		}
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

	protected String getBreadcrumbKey()
	{
		return "checkout.multi." + getCheckoutStep().getProgressBarId() + ".breadcrumb";
	}

	protected CheckoutStep getCheckoutStep()
	{
		return getCheckoutStep(DELIVERY_ADDRESS);
	}

	protected void populateCommonModelAttributes(final Model model, final CartData cartData, final AddressForm addressForm)
			throws CMSItemNotFoundException
	{
		model.addAttribute("cartData", cartData);
		model.addAttribute("addressForm", addressForm);
		model.addAttribute("deliveryAddresses", getDeliveryAddresses(cartData.getDeliveryAddress()));
		model.addAttribute("noAddress", Boolean.valueOf(b2bCheckoutFacade.hasNoDeliveryAddress()));
		model.addAttribute("addressFormEnabled", Boolean.valueOf(true));
		model.addAttribute("removeAddressEnabled", Boolean.valueOf(true));
		model.addAttribute(SHOW_SAVE_TO_ADDRESS_BOOK_ATTR, Boolean.TRUE);
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, getResourceBreadcrumbBuilder().getBreadcrumbs(getBreadcrumbKey()));
		model.addAttribute("metaRobots", "noindex,nofollow");
		if (StringUtils.isNotBlank(addressForm.getCountryIso()))
		{
			model.addAttribute("regions", getI18NFacade().getRegionsForCountryIso(addressForm.getCountryIso()));
			model.addAttribute("country", addressForm.getCountryIso());
		}
		prepareDataForPage(model);
		storeCmsPageInModel(model, getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL));
		setCheckoutStepLinksForModel(model, getCheckoutStep());
	}
	
	protected List<AddressData> getDeliveryAddresses(final AddressData selectedAddressData) // NOSONAR
	{
		List<AddressData> deliveryAddressesTemp = null;
		List<AddressData> deliveryAddresses = new ArrayList<AddressData>();
		if (selectedAddressData != null)
		{
			deliveryAddressesTemp = (List<AddressData>) getCheckoutFacade().getSupportedDeliveryAddresses(false);

			for (final AddressData deliveryAddress : deliveryAddressesTemp)
			{
				if (deliveryAddress.isShippingAddress())
				{
					deliveryAddresses.add(deliveryAddress);
				}
			}

			if (deliveryAddresses == null || deliveryAddresses.isEmpty())
			{
				deliveryAddresses = Collections.singletonList(selectedAddressData);
			}
			else if (!isAddressOnList(deliveryAddresses, selectedAddressData))
			{
				deliveryAddresses.add(selectedAddressData);
			}
		}

		return deliveryAddresses == null ? Collections.<AddressData> emptyList() : deliveryAddresses;
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
