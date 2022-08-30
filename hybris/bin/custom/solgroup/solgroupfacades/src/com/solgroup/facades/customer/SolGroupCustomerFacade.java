package com.solgroup.facades.customer;

import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commerceservices.customer.TokenInvalidatedException;

public interface SolGroupCustomerFacade extends CustomerFacade {
	
	void identityConfirmation_createPassword(String token, String newPassword) throws TokenInvalidatedException;
	
	boolean validateToken(String token) throws TokenInvalidatedException,IllegalArgumentException;
	
	void forgottenPassword_searchByEmail(final String email);
	
}
