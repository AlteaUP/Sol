package com.solgroup.core.service.quotes.impl;

import com.solgroup.core.service.quotes.SolGroupQuoteService;

import de.hybris.platform.commerceservices.enums.QuoteAction;
import de.hybris.platform.commerceservices.enums.QuoteUserType;
import de.hybris.platform.commerceservices.event.QuoteBuyerSubmitEvent;
import de.hybris.platform.commerceservices.event.QuoteSellerApprovalSubmitEvent;
import de.hybris.platform.commerceservices.order.impl.DefaultCommerceQuoteService;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.util.ServicesUtil;

public class DefaultSolGroupQuoteService extends DefaultCommerceQuoteService implements SolGroupQuoteService {

	@Override
	public QuoteModel submitQuote(final QuoteModel quoteModel, final UserModel userModel)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("quoteModel", quoteModel);
		ServicesUtil.validateParameterNotNullStandardMessage("userModel", userModel);

		getQuoteActionValidationStrategy().validate(QuoteAction.SUBMIT, quoteModel, userModel);

		QuoteModel updatedQuoteModel = isSessionQuoteSameAsRequestedQuote(quoteModel)
				? updateQuoteFromCart(getCartService().getSessionCart(), userModel) : quoteModel;

		validateQuoteTotal(updatedQuoteModel);

		getQuoteMetadataValidationStrategy().validate(QuoteAction.SUBMIT, updatedQuoteModel, userModel);

		updatedQuoteModel = getQuoteUpdateExpirationTimeStrategy().updateExpirationTime(QuoteAction.SUBMIT, updatedQuoteModel,
				userModel);
		updatedQuoteModel = getQuoteUpdateStateStrategy().updateQuoteState(QuoteAction.SUBMIT, updatedQuoteModel, userModel);
		getModelService().save(updatedQuoteModel);
		getModelService().refresh(updatedQuoteModel);

		final QuoteUserType quoteUserType = getQuoteUserTypeIdentificationStrategy().getCurrentQuoteUserType(userModel).get();
		if (QuoteUserType.BUYER.equals(quoteUserType))
		{
			final QuoteBuyerSubmitEvent quoteBuyerSubmitEvent = new QuoteBuyerSubmitEvent(updatedQuoteModel, userModel,
					quoteUserType);
			getEventService().publishEvent(quoteBuyerSubmitEvent);
		}
		else if (QuoteUserType.SELLER.equals(quoteUserType))
		{
			// Seller approve directly a quotation after its submit
			final QuoteSellerApprovalSubmitEvent quoteSellerApprovalSubmitEvent = new QuoteSellerApprovalSubmitEvent(
					updatedQuoteModel, userModel, getQuoteUserTypeIdentificationStrategy().getCurrentQuoteUserType(userModel).get());
			getEventService().publishEvent(quoteSellerApprovalSubmitEvent);

		}

		return updatedQuoteModel;
	}
	
	
	
}
