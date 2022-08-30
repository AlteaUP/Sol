package com.solgroup.core.actions.identityConfirmation;

import com.solgroup.core.model.IdentityConfirmationProcessModel;

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.task.RetryLaterException;

public class IdentityConfirmationChangeAction extends AbstractProceduralAction {

    @Override
    public void executeAction(final BusinessProcessModel businessProcess) throws RetryLaterException, Exception {
        if (businessProcess instanceof IdentityConfirmationProcessModel) {
            final IdentityConfirmationProcessModel identitiyConfirmationProcess = (IdentityConfirmationProcessModel) businessProcess;

            // Get customer
            final CustomerModel customer = identitiyConfirmationProcess.getCustomer();
            if (customer != null) {
                customer.setIdentityConfirmationSent(Boolean.TRUE);
                getModelService().save(customer);
            }
        }

    }

}
