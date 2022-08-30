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
package com.solgroup.core.actions.quote;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.commerceservices.model.process.QuoteProcessModel;
import de.hybris.platform.commerceservices.order.CommerceQuoteService;
import de.hybris.platform.core.enums.QuoteState;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.order.QuoteService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.task.RetryLaterException;

/**
 * This action class checks for seller approver response. If quote was approved, a new quote snapshot will be created
 * with status as BUYER_OFFER
 */
public class CreateBuyerOfferAction extends AbstractQuoteDecisionAction<QuoteProcessModel> {
    private QuoteService quoteService;
    private CommerceQuoteService commerceQuoteService;
    private SessionService sessionService;
    private UserService userService;
    private static final Logger LOG = Logger.getLogger(CheckSellerApproverResponseOnQuoteAction.class);

    @Override
    public Transition executeAction(final QuoteProcessModel process) throws RetryLaterException, Exception {
        Transition result = Transition.ERROR;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("In CreateBuyerOfferAction for process code : [%s]", process.getCode()));
        }

        final QuoteModel quoteModel = getQuoteService().getCurrentQuoteForCode(process.getQuoteCode());

        if (QuoteState.SELLERAPPROVER_APPROVED.equals(quoteModel.getState())) {

            sessionService.executeInLocalView(new SessionExecutionBody() {
                @Override
                public Object execute() {
                    return getCommerceQuoteService().createQuoteSnapshotWithState(quoteModel, QuoteState.BUYER_OFFER);

                }
            }, userService.getAdminUser());

            result = Transition.OK;

        } else {
            result = Transition.NOK;
        }
        return result;
    }

    protected QuoteService getQuoteService() {
        return quoteService;
    }

    @Required
    public void setQuoteService(final QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    protected CommerceQuoteService getCommerceQuoteService() {
        return commerceQuoteService;
    }

    @Required
    public void setCommerceQuoteService(final CommerceQuoteService commerceQuoteService) {
        this.commerceQuoteService = commerceQuoteService;
    }

    protected SessionService getSessionService() {
        return sessionService;
    }

    @Required
    public void setSessionService(final SessionService sessionService) {
        this.sessionService = sessionService;
    }

    protected UserService getUserService() {
        return userService;
    }

    @Required
    public void setUserService(final UserService userService) {
        this.userService = userService;
    }

}
