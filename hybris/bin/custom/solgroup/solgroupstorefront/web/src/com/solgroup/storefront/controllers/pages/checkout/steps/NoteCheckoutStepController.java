package com.solgroup.storefront.controllers.pages.checkout.steps;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.solgroup.facades.order.SolGroupCheckoutFacade;
import com.solgroup.storefront.controllers.ControllerConstants;
import com.solgroup.storefront.forms.NoteForm;

import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.data.CartData;

@Controller
@RequestMapping(value = "/checkout/multi/note")
public class NoteCheckoutStepController extends AbstractCheckoutStepController {

    private static final String ADD_NOTE = "add-note";
    private static final String CART_DATA_ATTR = "cartData";

    @Resource(name = "b2bCheckoutFacade")
    private SolGroupCheckoutFacade b2bCheckoutFacade;

    @Override
    @RequestMapping(value = "/show", method = RequestMethod.GET)
    @RequireHardLogIn
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
        final CartData cartData = b2bCheckoutFacade.getCheckoutCart();
        model.addAttribute(CART_DATA_ATTR, cartData);
        setupAddNotePage(model);
        return ControllerConstants.Views.Pages.MultiStepCheckout.AddNotePage;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @RequireHardLogIn
    public String addNoteToCart(final Model model, @Valid final NoteForm noteForm, final BindingResult bindingResult,
            final HttpServletRequest request, final RedirectAttributes redirectModel) throws CMSItemNotFoundException {
        b2bCheckoutFacade.updateNoteInSessionCart(noteForm.getNote(), noteForm.getAgentVoucher());
        setupAddNotePage(model);
        return getCheckoutStep().nextStep();

    }

    @RequestMapping(value = "/back", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    public String back(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().previousStep();
    }

    @RequestMapping(value = "/next", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    public String next(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().nextStep();
    }

    protected CheckoutStep getCheckoutStep() {
        return getCheckoutStep(ADD_NOTE);
    }

    protected void setupAddNotePage(final Model model) throws CMSItemNotFoundException {
        model.addAttribute("metaRobots", "noindex,nofollow");
        model.addAttribute("hasNoPaymentInfo", Boolean.valueOf(getCheckoutFlowFacade().hasNoPaymentInfo()));
        prepareDataForPage(model);
        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.addNote.breadcrumb"));
        final ContentPageModel contentPage = getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL);
        storeCmsPageInModel(model, contentPage);
        setUpMetaDataForContentPage(model, contentPage);
        setCheckoutStepLinksForModel(model, getCheckoutStep());
    }

}
