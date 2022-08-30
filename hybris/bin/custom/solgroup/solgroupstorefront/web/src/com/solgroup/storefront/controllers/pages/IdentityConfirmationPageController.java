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

import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.ResourceBreadcrumbBuilder;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController;
import de.hybris.platform.acceleratorstorefrontcommons.forms.ForgottenPwdForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.UpdatePwdForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.UpdatePasswordFormValidator;
import de.hybris.platform.b2bacceleratorfacades.customer.exception.InvalidPasswordException;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.customer.impl.DefaultCustomerFacade;
import de.hybris.platform.commerceservices.customer.TokenInvalidatedException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import com.solgroup.facades.customer.SolGroupCustomerFacade;
import com.solgroup.storefront.controllers.ControllerConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Controller for the forgotten password pages. Supports requesting a password reset email as well as changing the
 * password once you have got the token that was sent via email.
 */
@Controller
@RequestMapping(value = "/login/identityConfirmation")
public class IdentityConfirmationPageController extends AbstractPageController
{

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(IdentityConfirmationPageController.class);

	private static final String REDIRECT_LOGIN = "redirect:/login";
	private static final String REDIRECT_HOME = "redirect:/";
	private static final String UPDATE_PWD_CMS_PAGE = "updatePassword";

	private static final String GENERIC_ERROR_CMS_PAGE = "genericError";

	@Resource(name = "customerFacade")
	private SolGroupCustomerFacade customerFacade;

	@Resource(name = "simpleBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder resourceBreadcrumbBuilder;

	@Resource(name = "updatePasswordFormValidator")
	private UpdatePasswordFormValidator updatePasswordFormValidator;


	@RequestMapping(value = "/new", method = RequestMethod.GET)
	public String getChangePassword(@RequestParam(required = false) final String token, final Model model)
			throws CMSItemNotFoundException
	{
		if (StringUtils.isBlank(token))
		{
			return REDIRECT_HOME;
		}
		try 
		{
			customerFacade.validateToken(token);
		}
		catch (Exception e)
		{
			
			// No page found - display the notFound page with error from controller
			storeCmsPageInModel(model, getContentPageForLabelOrId(GENERIC_ERROR_CMS_PAGE));
			setUpMetaDataForContentPage(model, getContentPageForLabelOrId(GENERIC_ERROR_CMS_PAGE));
			GlobalMessages.addErrorMessage(model, "system.error.page.generic");
			return ControllerConstants.Views.Pages.Error.GenericErrorPage;
			
			
		}
		
			
		final UpdatePwdForm form = new UpdatePwdForm();
		form.setToken(token);
		model.addAttribute(form);
		storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("newAccount.title"));
		return ControllerConstants.Views.Pages.IdentityConfirmation.IdentityConfirmationPage;
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public String changePassword(@Valid final UpdatePwdForm form, final BindingResult bindingResult, final Model model,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		getUpdatePasswordFormValidator().validate(form, bindingResult);
		if (bindingResult.hasErrors())
		{
			prepareErrorMessage(model, UPDATE_PWD_CMS_PAGE);
			return ControllerConstants.Views.Pages.IdentityConfirmation.IdentityConfirmationPage;
		}
		if (!StringUtils.isBlank(form.getToken()))
		{
			try
			{
				//customerFacade.updatePassword(form.getToken(), form.getPwd());
				customerFacade.identityConfirmation_createPassword(form.getToken(), form.getPwd());
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER,
						"account.confirmation.password.updated");
			}
			catch (final TokenInvalidatedException e)
			{
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "identityConfirmation.token.invalidated");
				prepareErrorMessage(model, UPDATE_PWD_CMS_PAGE);
				return ControllerConstants.Views.Pages.IdentityConfirmation.IdentityConfirmationPage;
			}
			catch (IllegalArgumentException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug(e);
				}
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "identityConfirmation.token.invalid");
				prepareErrorMessage(model, UPDATE_PWD_CMS_PAGE);
				return ControllerConstants.Views.Pages.IdentityConfirmation.IdentityConfirmationPage;
			} 
			catch (final InvalidPasswordException e) {

				if (LOG.isDebugEnabled()) {
					LOG.debug(e);
				}
				bindingResult.rejectValue("pwd", "identityConfirmation.notValidReason.regex", new Object[] {},
						"identityConfirmation.notValidReason.regex");
				prepareErrorMessage(model, UPDATE_PWD_CMS_PAGE);
				return ControllerConstants.Views.Pages.IdentityConfirmation.IdentityConfirmationPage;
			} catch (final Exception e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug(e);
				}
				bindingResult.rejectValue("pwd", "identityConfirmation.notValidReason.generic", new Object[] {},
						"identityConfirmation.notValidReason.generic");
				prepareErrorMessage(model, UPDATE_PWD_CMS_PAGE);
				return ControllerConstants.Views.Pages.IdentityConfirmation.IdentityConfirmationPage;
			}
			
//			catch (final RuntimeException e)
//			{
//				if (LOG.isDebugEnabled())
//				{
//					LOG.debug(e);
//				}
//				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "updatePwd.token.invalid");
//			}
		}
		return REDIRECT_LOGIN;
	}

	/**
	 * Prepares the view to display an error message
	 * 
	 * @throws CMSItemNotFoundException
	 */
	protected void prepareErrorMessage(final Model model, final String page) throws CMSItemNotFoundException
	{
		GlobalMessages.addErrorMessage(model, "form.global.error");
		storeCmsPageInModel(model, getContentPageForLabelOrId(page));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(page));
	}


	public UpdatePasswordFormValidator getUpdatePasswordFormValidator()
	{
		return updatePasswordFormValidator;
	}
}
