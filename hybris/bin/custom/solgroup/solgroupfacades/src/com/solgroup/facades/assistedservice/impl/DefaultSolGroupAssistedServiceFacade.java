package com.solgroup.facades.assistedservice.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.enums.B2BUnitTypeEnum;
import com.solgroup.core.login.SolGroupLoginService;
import com.solgroup.core.service.b2bunit.impl.DefaultSolGroupB2BUnitService;
import com.solgroup.facades.assistedservice.SolGroupAssistedServiceFacade;
import com.solgroup.facades.assistedservice.exceptions.SolGroupAsmUserWithoutGrantException;

import de.hybris.platform.assistedservicefacades.constants.AssistedservicefacadesConstants;
import de.hybris.platform.assistedservicefacades.impl.DefaultAssistedServiceFacade;
import de.hybris.platform.assistedserviceservices.constants.AssistedserviceservicesConstants;
import de.hybris.platform.assistedserviceservices.exception.AssistedServiceAgentBadCredentialsException;
import de.hybris.platform.assistedserviceservices.exception.AssistedServiceAgentBlockedException;
import de.hybris.platform.assistedserviceservices.exception.AssistedServiceAgentNoCustomerAndCartIdException;
import de.hybris.platform.assistedserviceservices.exception.AssistedServiceAgentNotLoggedInException;
import de.hybris.platform.assistedserviceservices.exception.AssistedServiceException;
import de.hybris.platform.assistedserviceservices.exception.AssistedServiceWrongCartIdException;
import de.hybris.platform.assistedserviceservices.exception.AssistedServiceWrongCustomerIdException;
import de.hybris.platform.assistedserviceservices.utils.CustomerEmulationParams;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.CustomerListSearchService;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.ticket.enums.EventType;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.localization.Localization;

public class DefaultSolGroupAssistedServiceFacade extends DefaultAssistedServiceFacade
        implements SolGroupAssistedServiceFacade {

    private DefaultSolGroupB2BUnitService solGroupB2BUnitService;
    //private SessionService sessionService;
    private CustomerListSearchService customerListSearchService;
    private SolGroupLoginService solGroupLoginService;
    
    private static final Logger LOG = Logger.getLogger(DefaultSolGroupAssistedServiceFacade.class);

    @Override
    protected void loginAssistedServiceAgent(final String username, final String password,
            final boolean isTokenBasedLogin) throws AssistedServiceException {

        try {
            UserModel agent = getUserService().getUserForUID(username);

            if (agent.isLoginDisabled()) {
                throw new AssistedServiceAgentBlockedException("Account was blocked.");
            }

            // Login to ASM is allowed only to B2BCustomer related with Organizations.
            if (agent instanceof B2BCustomerModel) {
                B2BUnitModel b2bUnitModel = ((B2BCustomerModel) agent).getDefaultB2BUnit();
                if (b2bUnitModel != null) {
                    if (b2bUnitModel.getB2bUnitType() != null) {
                        if (!B2BUnitTypeEnum.ORGANIZATION_1LEVEL.equals(b2bUnitModel.getB2bUnitType())
                                && !B2BUnitTypeEnum.ORGANIZATION_2LEVEL.equals(b2bUnitModel.getB2bUnitType())) {
                            throw new SolGroupAsmUserWithoutGrantException(
                                    "B2BUnit or UserID invalid for B2BCustomer authentication.");
                        }
                    }
                }
                getSessionService().setAttribute(SolgroupCoreConstants.SESSION_PARAM_B2BUNITS_BRANCH_SET,
                        getSolGroupB2BUnitService().getBranch(b2bUnitModel));
            }

            if (isTokenBasedLogin) {
                launchAssistedServiceMode();

                // ensure credentials (password didn't change)
                if (!agent.getEncodedPassword().equals(password)) {
                    sanityCheckForAgent(agent);
                }

                loginAssistedServiceAgent(agent);
                LOG.info(String.format("Agent [%s] has been loged in using saml", agent.getUid()));
            } else {
                verifyFormLoginAbility();
                if (getAsmSession() == null) {
                    launchAssistedServiceMode();
                }

                if (!getPasswordEncoderService().isValid(agent, password)) {
                    sanityCheckForAgent(agent);
                }

                loginAssistedServiceAgent(agent);
                LOG.info(String.format("Agent [%s] has been loged in using login form", agent.getUid()));

            }
            // reset brute force counter after successful login
            resetBruteForceCounter(agent);
        } catch (final UnknownIdentifierException | ClassMismatchException e) {
            LOG.debug(e.getMessage(), e);
            throw new AssistedServiceAgentBadCredentialsException("Unknown user id.");
        }
    }

    @Override
    public void emulateCustomer(final String customerId, final String cartId, final String orderId)
            throws AssistedServiceException {
        try {
            // validate session at first
            validateSession();
        } catch (final AssistedServiceAgentNotLoggedInException e) {
            // in case AS agent isn't logged in yet - save parameters in session
            getAsmSession().setSavedEmulationData(new CustomerEmulationParams(customerId, cartId, orderId));
            throw e;
        }

        // Both parameters can't be blank at the same time
        if (StringUtils.isBlank(customerId) && StringUtils.isBlank(cartId)) {
            throw new AssistedServiceAgentNoCustomerAndCartIdException(
                    Localization.getLocalizedString("asm.emulate.error.no_customer_or_cart_id_provided"));
        }

        try {
            UserModel customer = getAssistedServiceService().getCustomer(customerId);
            // emulate only customers and not agents
            if (customer instanceof CustomerModel) {
                // attach cart to the session in case it's been provided
                if (StringUtils.isNotBlank(cartId)) {
                    final CartModel cart = getAssistedServiceService().getCartByCode(cartId, customer);

                    if (cart == null) {
                        final OrderModel order = getAssistedServiceService().gerOrderByCode(cartId, customer);
                        if (order == null) {
                            throw new AssistedServiceWrongCartIdException("Cart ID/Order ID not found");
                        }
                        if (getUserService().isAnonymousUser(customer)) {
                            customer = order.getUser();
                        }
                        getSessionService().setAttribute(AssistedservicefacadesConstants.ASM_ORDER_ID_TO_REDIRECT,
                                order.getCode());
                        attachLatestSessionCart(customer);
                    } else {
                        // Fix for BAD-1100
                        cart.setCurrency(getCommerceCommonI18NService().getCurrentCurrency());
                        getCartService().setSessionCart(cart);
                    }
                } else {
                    // in case no cartId has been provided get the latest modified one
                    attachLatestSessionCart(customer);
                }

                if (StringUtils.isNotBlank(orderId)) {
                    final OrderModel order = getAssistedServiceService().gerOrderByCode(orderId, customer);
                    if (order == null) {
                        getAsmSession().setFlashErrorMessage("asm.emulate.error.order_not_found");
                        getAsmSession().setFlashErrorMessageArgs(orderId);
                    } else {
                        getSessionService().setAttribute(AssistedservicefacadesConstants.ASM_ORDER_ID_TO_REDIRECT,
                                order.getCode());
                    }
                }
                getUserService().setCurrentUser(customer);
                getAsmSession().setEmulatedCustomer(customer);

                createSessionEvent(getAsmSession().getAgent(), customer, EventType.START_SESSION_EVENT);

                getSolGroupLoginService().prepareUserSession(customer, true);
                
                // Store branch in session
//                if (customer instanceof B2BCustomerModel) {
//                    B2BCustomerModel b2bCustomer = (B2BCustomerModel) customer;
//                    B2BUnitModel b2bUnit = b2bCustomer.getDefaultB2BUnit();
//                    if (b2bUnit == null) {
//                        throw new AssistedServiceException("No b2bUnit for b2bCustomer " + customerId);
//                    }
//                    Set<B2BUnitModel> branch = getSolGroupB2BUnitService().getBranch(b2bUnit);
//                    if (branch == null) {
//                        branch = Sets.newHashSet();
//                    }
//                    sessionService.setAttribute(SolgroupCoreConstants.SESSION_PARAM_B2BUNITS_BRANCH_SET, branch);
//                }
                
                
            } else {
                throw new AssistedServiceWrongCustomerIdException(
                        "This id belongs to non-customer person. Will not add customer and/or cart to the session.");
            }

        } catch (final UnknownIdentifierException e) {
            throw new AssistedServiceWrongCustomerIdException(
                    "Unknown customer id. Will not add customer and/or cart to the session.", e);
        }
    }

    @Override
    public List<CustomerData> getSuggestedCustomerList(final String id) throws AssistedServiceException {
        validateSession();
        final PageableData pageableData = new PageableData();
        pageableData.setCurrentPage(0);
        pageableData.setPageSize(Config.getInt(AssistedserviceservicesConstants.DEFAULT_SUGGESTED_CUSTOMERS_LIMIT, 20));

        final UserModel loggedUserModel = getAsmSession().getAgent();
        Map<String, Object> parametersMap = new HashMap<>();
        boolean isAgentOrB2BCustomer = false;

        if (loggedUserModel instanceof B2BCustomerModel || loggedUserModel instanceof EmployeeModel) {
//            parametersMap.put("B2BUNIT", ((B2BCustomerModel) loggedUserModel).getDefaultB2BUnit());
//            parametersMap.put("BRANCH_SET",
//                    getSessionService().getAttribute(SolgroupCoreConstants.B2BUNITS_BRANCH_SET));
            isAgentOrB2BCustomer = true;
        }

//        if (loggedUserModel instanceof EmployeeModel) {
//            EmployeeModel agent = (EmployeeModel) loggedUserModel;
//            parametersMap.put("VENDOR_CODE", agent.getVendorCode());
//            isAgentOrB2BCustomer = true;
//        }

        if (isAgentOrB2BCustomer) {
            parametersMap.put("ID", id);
            final SearchPageData<CustomerModel> customers = getCustomerListSearchService().getPagedCustomers(
                    SolgroupCoreConstants.SOLGROUP_B2B_CUSTOMER_LIST_UID, loggedUserModel.getUid(), pageableData,
                    parametersMap);
            return Converters.convertAll(customers.getResults(), getCustomerConverter());
        }

        return super.getCustomers(id, pageableData);
    }

    @Override
    protected boolean isAssistedServiceAgent(final UserModel asmAgent) {
        final Set<UserGroupModel> userGroups = getUserService().getAllUserGroupsForUser(asmAgent);
        for (final UserGroupModel userGroup : userGroups) {
            if (AssistedserviceservicesConstants.AS_AGENT_GROUP_UID.equals(userGroup.getUid())
                    && asmAgent instanceof EmployeeModel) {
                return true;
            } else if (SolgroupCoreConstants.ORGANIZATION_GROUP_ID.equals(userGroup.getUid())
                    && asmAgent instanceof CustomerModel) {
                return true;
            }

        }
        return false;
    }

    public DefaultSolGroupB2BUnitService getSolGroupB2BUnitService() {
        return solGroupB2BUnitService;
    }

    @Required
    public void setSolGroupB2BUnitService(DefaultSolGroupB2BUnitService solGroupB2BUnitService) {
        this.solGroupB2BUnitService = solGroupB2BUnitService;
    }

//    @Override
//    protected SessionService getSessionService() {
//        return sessionService;
//    }

//    @Override
//    @Required
//    public void setSessionService(SessionService sessionService) {
//        this.sessionService = sessionService;
//    }

    protected CustomerListSearchService getCustomerListSearchService() {
        return customerListSearchService;
    }

    @Required
    public void setCustomerListSearchService(CustomerListSearchService customerListSearchService) {
        this.customerListSearchService = customerListSearchService;
    }

	protected SolGroupLoginService getSolGroupLoginService() {
		return solGroupLoginService;
	}

	@Required
	public void setSolGroupLoginService(SolGroupLoginService solGroupLoginService) {
		this.solGroupLoginService = solGroupLoginService;
	}
    
    

}
