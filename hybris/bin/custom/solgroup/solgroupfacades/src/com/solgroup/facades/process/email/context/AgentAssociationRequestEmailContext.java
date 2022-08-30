package com.solgroup.facades.process.email.context;

import org.apache.commons.lang.StringUtils;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;

/**
 * 
 * @author fmilazzo
 *
 */
public class AgentAssociationRequestEmailContext extends AbstractEmailContext<OrderProcessModel> {

    public static final String AGENT_VOUCHER = "agentVoucher";

    @Override
    public void init(final OrderProcessModel orderProcessModel, final EmailPageModel emailPageModel) {
        super.init(orderProcessModel, emailPageModel);

        if (StringUtils.isNotEmpty(orderProcessModel.getOrder().getAgentVoucher())) {
            put(AGENT_VOUCHER, orderProcessModel.getOrder().getAgentVoucher());
        } else {
            put(AGENT_VOUCHER, getConfigurationService().getConfiguration().getString("default.agent.voucher"));
        }

    }

    @Override
    protected BaseSiteModel getSite(final OrderProcessModel orderProcessModel) {
        return orderProcessModel.getOrder().getSite();
    }

    @Override
    protected CustomerModel getCustomer(final OrderProcessModel orderProcessModel) {
        return (CustomerModel) orderProcessModel.getOrder().getUser();
    }

    @Override
    protected LanguageModel getEmailLanguage(final OrderProcessModel orderProcessModel) {
        return orderProcessModel.getOrder().getLanguage();
        // return orderProcessModel.getOrder().getSite().getDefaultLanguage();

    }

}
