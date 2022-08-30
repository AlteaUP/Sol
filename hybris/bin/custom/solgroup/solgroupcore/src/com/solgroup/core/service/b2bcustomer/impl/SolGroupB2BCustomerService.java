package com.solgroup.core.service.b2bcustomer.impl;


import de.hybris.platform.b2b.services.B2BCustomerService;

public interface SolGroupB2BCustomerService<U, C> extends B2BCustomerService<U, C> {
	
	void toggleB2BCustomer(U b2bCustomer, Boolean active, Boolean saveCustomer);
		
	
	U findB2BCustomerByEmail(String email);


}
