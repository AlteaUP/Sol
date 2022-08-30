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
package com.solgroup.facades.process.email.context;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.company.SolGroupCompanyService;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.quote.data.QuoteData;
import de.hybris.platform.commerceservices.model.process.QuoteProcessModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.QuoteService;
import de.hybris.platform.servicelayer.dto.converter.Converter;

/**
 * Velocity context for a quote notification email.
 */
public class QuoteNotificationEmailContext extends AbstractEmailContext<QuoteProcessModel> {
    private QuoteService quoteService;

    private Converter<QuoteModel, QuoteData> quoteConverter;

    private QuoteData quoteData;

    private SolGroupCompanyService solgroupCompanyService;

    public static final String VENDOR_DISPLAY_NAME = "vendorDisplayName";

    @Override
    public void init(final QuoteProcessModel quoteProcessModel, final EmailPageModel emailPageModel) {
        super.init(quoteProcessModel, emailPageModel);

        final B2BUnitModel b2bUnit = getB2BUnit(getCustomer(quoteProcessModel));
        UserModel vendor = null;
        if (b2bUnit != null) {
            vendor = getSolgroupCompanyService().findAssignedAgents(b2bUnit);
        }

        if (vendor != null) {
            put(VENDOR_DISPLAY_NAME, vendor.getDisplayName());
        }

        quoteData = getQuoteConverter().convert(getQuote(quoteProcessModel));
    }

    public QuoteData getQuote() {
        return quoteData;
    }

    @Override
    protected BaseSiteModel getSite(final QuoteProcessModel quoteProcessModel) {
        return getQuote(quoteProcessModel).getSite();
    }

    @Override
    protected CustomerModel getCustomer(final QuoteProcessModel quoteProcessModel) {
        return (CustomerModel) getQuote(quoteProcessModel).getUser();
    }

    @Override
    protected LanguageModel getEmailLanguage(final QuoteProcessModel quoteProcessModel) {
        return getCustomer(quoteProcessModel).getSessionLanguage();
    }

    protected QuoteModel getQuote(final QuoteProcessModel quoteProcessModel) {
        return Optional.of(quoteProcessModel).map(QuoteProcessModel::getQuoteCode).map(getQuoteService()::getCurrentQuoteForCode).get();
    }

    private B2BUnitModel getB2BUnit(final UserModel userModel) {
        if (userModel instanceof B2BCustomerModel) {
            final B2BCustomerModel b2bCustomer = (B2BCustomerModel) userModel;
            return b2bCustomer.getDefaultB2BUnit();
        }
        return null;
    }

    @Required
    public void setQuoteService(final QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    protected QuoteService getQuoteService() {
        return quoteService;
    }

    @Required
    public void setQuoteConverter(final Converter<QuoteModel, QuoteData> quoteConverter) {
        this.quoteConverter = quoteConverter;
    }

    protected SolGroupCompanyService getSolgroupCompanyService() {
        return solgroupCompanyService;
    }

    @Required
    public void setSolgroupCompanyService(final SolGroupCompanyService solgroupCompanyService) {
        this.solgroupCompanyService = solgroupCompanyService;
    }

    protected Converter<QuoteModel, QuoteData> getQuoteConverter() {
        return quoteConverter;
    }

    public String getVendorDisplayName() {
        return (String) get(VENDOR_DISPLAY_NAME);
    }

}
