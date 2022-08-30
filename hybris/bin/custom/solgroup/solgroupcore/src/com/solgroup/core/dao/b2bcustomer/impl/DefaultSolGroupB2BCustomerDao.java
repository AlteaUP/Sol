/**
 *
 */
package com.solgroup.core.dao.b2bcustomer.impl;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;

import com.google.common.collect.Sets;
import com.solgroup.core.dao.b2bcustomer.SolGroupB2BCustomerDao;

import jersey.repackaged.com.google.common.collect.Maps;

/**
 * @author salvo
 *
 */
public class DefaultSolGroupB2BCustomerDao extends AbstractItemDao implements SolGroupB2BCustomerDao {


	@Override
	public List<B2BCustomerModel> findIdentityConfirmationSentB2BCustomer(Boolean emailSent, Boolean b2bUnitActive, CMSSiteModel site) {

		final StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select {c." + ItemModel.PK + "} from ");
		queryBuilder.append("{" + B2BCustomerModel._TYPECODE + " as c join ");
		queryBuilder.append(B2BUnitModel._TYPECODE + " as u on ");
		queryBuilder.append("{c." + B2BCustomerModel.DEFAULTB2BUNIT + "} = {u." + B2BUnitModel.PK + "}}");
		queryBuilder.append(" where ");
		queryBuilder.append("{c." + B2BCustomerModel.IDENTITYCONFIRMATIONSENT + "} = ?emailSent and ");
		queryBuilder.append("{c." + B2BCustomerModel.CMSSITE + "} = ?site and ");
		queryBuilder.append("{u." + B2BUnitModel.ACTIVE + "} = ?b2bUnitActive ");

		final Map<String, Object> params = Maps.newHashMap();
		params.put("emailSent", emailSent);
		params.put("site", site);
		params.put("b2bUnitActive", b2bUnitActive);

		final List<B2BCustomerModel> result = getFlexibleSearchService()
				.<B2BCustomerModel> search(queryBuilder.toString(), params).getResult();

		return result;

	}
	


}
