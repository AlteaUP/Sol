/**
 *
 */
package com.solgroup.core.dao.b2bunit.impl;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.EmployeeModel;
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

import com.solgroup.core.dao.b2bcustomer.SolGroupB2BCustomerDao;
import com.solgroup.core.dao.b2bunit.SolGroupB2BUnitDao;

import jersey.repackaged.com.google.common.collect.Maps;

/**
 * @author salvo
 *
 */
public class DefaultSolGroupB2BUnitDao extends AbstractItemDao implements SolGroupB2BUnitDao {

	@Override
	public CompanyModel findCompany(final String erpCode, final CMSSiteModel cmsSite) {

		final StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select {c." + ItemModel.PK + "} from ");
		queryBuilder.append("{" + CompanyModel._TYPECODE + " as c}");
		queryBuilder.append(" where ");
		queryBuilder.append("{c." + CompanyModel.ERPCODE + "} = ?erpCode ");
		queryBuilder.append(" AND {c." + CompanyModel.CMSSITE + "} = ?cmsSite");

		final Map<String, Object> params = Maps.newHashMap();
		params.put("erpCode", erpCode);
		params.put("cmsSite", cmsSite);

		final List<CompanyModel> result = getFlexibleSearchService()
				.<CompanyModel> search(queryBuilder.toString(), params).getResult();

		if (CollectionUtils.isEmpty(result)) {
			return null;
		} else {
			return result.get(0);
		}
	}

	@Override
	public B2BUnitModel findB2BUnit(CompanyModel legalEntity, String erpCode, CMSSiteModel cmsSite) {

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select {b." + ItemModel.PK + "} from {");
		queryBuilder.append(B2BUnitModel._TYPECODE + " as b ");
		queryBuilder.append(" join " + CompanyModel._TYPECODE + " as c on {c." + ItemModel.PK + "}={b."
				+ B2BUnitModel.LEGALENTITY + "}");
		queryBuilder.append("} where");
		queryBuilder.append("{b." + B2BUnitModel.ERPCODE + "}=?erpCode ");
		queryBuilder.append("AND {c." + CompanyModel.ERPCODE + "}=?legalEntityErpCode ");
		queryBuilder.append("AND {b." + B2BUnitModel.CMSSITE + "}=?cmsSite ");
		queryBuilder.append("AND {c." + CompanyModel.CMSSITE + "}=?cmsSite ");

		final Map<String, Object> params = Maps.newHashMap();
		params.put("erpCode", erpCode);
		params.put("cmsSite", cmsSite);
		params.put("legalEntityErpCode", legalEntity.getErpCode());

		final List<B2BUnitModel> result = getFlexibleSearchService()
				.<B2BUnitModel> search(queryBuilder.toString(), params).getResult();

		if (CollectionUtils.isEmpty(result)) {
			return null;
		} else {
			return result.get(0);
		}
	}
	
	@Override
	public List<String> findVisibilityCategories(B2BUnitModel b2bUnit) {
		
		
		List<String> result = Lists.newArrayList();
		
		Set<PrincipalGroupModel> groups = b2bUnit.getGroups();
		
		// No groups found into responsibleCompany --> return empty result
		if(CollectionUtils.isEmpty(groups)) {
			return result;
		}
		
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select {cat.code} from {");
		queryBuilder.append("Category2PrincipalRelation as cpr");
		queryBuilder.append(" join UserGroup as ug on {ug.pk}={cpr.target}");
		queryBuilder.append(" join VisibilityCategory as cat on {cat.pk}={cpr.source}");
		queryBuilder.append("}");
		queryBuilder.append(" where ");
		queryBuilder.append("{ug.pk} IN (?groups)");
		
		Map<String,Object> params = Maps.newHashMap();
		params.put("groups", groups);
		
        FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(queryBuilder.toString(), params);
        flexibleSearchQuery.setResultClassList(Arrays.asList(String.class));
        
        SearchResult searchResult = getFlexibleSearchService().search(flexibleSearchQuery);

        if (searchResult != null && searchResult.getResult() != null) {
            for (int i = 0; i < searchResult.getResult().size(); i++) {
                Object resultRow = searchResult.getResult().get(i);
                if (resultRow instanceof String) {
                    if(StringUtils.isNotEmpty(resultRow.toString())) {
                    	result.add(resultRow.toString());
                    }
                }
            }
        }

		return result;
	}

	@Override
	public EmployeeModel findAgentForB2BUnit(B2BUnitModel b2bUnit) {
		
		String vendorCode = b2bUnit.getVendorCode();
		if(StringUtils.isEmpty(vendorCode)) {
			return null;
		}
		
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select {e." + ItemModel.PK + "} from {" + EmployeeModel._TYPECODE + " as e}");
		queryBuilder.append(" where ");
		queryBuilder.append(" {e." + EmployeeModel.VENDORCODE + "}=?vendorCode");
		
		Map<String,Object> params = Maps.newHashMap();
		params.put("vendorCode", vendorCode);
		
		final List<EmployeeModel> result = getFlexibleSearchService()
				.<EmployeeModel> search(queryBuilder.toString(), params).getResult();

		if (CollectionUtils.isEmpty(result)) {
			return null;
		} else {
			return result.get(0);
		}

	}


}
