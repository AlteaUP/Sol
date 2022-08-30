/**
 *
 */
package com.solgroup.core.dao.b2bcustomer;

import java.util.List;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;

/**
 * @author salvo
 *
 */
public interface SolGroupB2BCustomerDao
{

	
	public List<B2BCustomerModel> findIdentityConfirmationSentB2BCustomer(Boolean emailSent, Boolean b2bUnitActive, CMSSiteModel site);
	
}
