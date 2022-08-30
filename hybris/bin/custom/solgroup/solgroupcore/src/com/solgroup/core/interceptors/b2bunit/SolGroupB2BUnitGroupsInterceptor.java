package com.solgroup.core.interceptors.b2bunit;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;
import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.enums.B2BUnitTypeEnum;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.user.UserService;
import jersey.repackaged.com.google.common.collect.Sets;

public class SolGroupB2BUnitGroupsInterceptor implements PrepareInterceptor<B2BUnitModel> {

	private Logger LOG = Logger.getLogger(SolGroupB2BUnitGroupsInterceptor.class);
	
	private UserService userService;
	private ConfigurationService configurationService;
	
	private static final String pharmaClientTypes_propertyKey = "pharma.client.types";
	private static final String pharmaClientGroupId_propertyKey = "pharma.client.groupId";
	
	@Override
	public void onPrepare(B2BUnitModel b2bUnit, InterceptorContext ctx) throws InterceptorException {
		
		if(ctx.isNew(b2bUnit) || ctx.isModified(b2bUnit, B2BUnitModel.RESPONSIBLECOMPANY) || ctx.isModified(b2bUnit,B2BUnitModel.CLIENTTYPE) || ctx.isModified(b2bUnit,B2BUnitModel.GROUPS)) {
			
			Set<PrincipalGroupModel> currentGroups = b2bUnit.getGroups();
			Set<PrincipalGroupModel> newGroups = Sets.newHashSet();
			if(currentGroups!=null) {
				for(PrincipalGroupModel principalGroup : currentGroups) {
					if( !(principalGroup instanceof CompanyModel) && !principalGroup.getUid().equals(getConfigurationService().getConfiguration().getString(pharmaClientGroupId_propertyKey))) {
						newGroups.add(principalGroup);
					}
		 		}
			}
			
			// Add responsible company
			if(
					b2bUnit.getResponsibleCompany() instanceof B2BUnitModel && 
					((B2BUnitModel)b2bUnit.getResponsibleCompany()).getB2bUnitType().equals(B2BUnitTypeEnum.POINT_OF_SALE) 
			) {
				LOG.warn(String.format("[%s] has an invalid responsible compnay: resp. company code [%s], resp. company type [%s]",
						b2bUnit.getUid(),
						b2bUnit.getResponsibleCompany().getUid(), 
						((B2BUnitModel)b2bUnit.getResponsibleCompany()).getB2bUnitType().getCode()));
			}
			else {
				newGroups.add(b2bUnit.getResponsibleCompany());
			}
			
			
			
			// Pharma clientType
			List<String> clientTypes_pharma = null;
			String pharmaClientTypes_str = getConfigurationService().getConfiguration().getString(pharmaClientTypes_propertyKey);
			if(StringUtils.isNotEmpty(pharmaClientTypes_str)) {
				clientTypes_pharma = Lists.newArrayList(pharmaClientTypes_str.split(","));
			}
			else {
				clientTypes_pharma = Lists.newArrayList();
			}
			
			// Get client type
			if(b2bUnit.getClientType()!=null) {
				String clientType = b2bUnit.getClientType().getCode();
				if(clientTypes_pharma.contains(clientType.toUpperCase())) {
					newGroups.add(getUserService().getUserGroupForUID(getConfigurationService().getConfiguration().getString(pharmaClientGroupId_propertyKey)));
				}
			}
			
			b2bUnit.setGroups(newGroups);
		
		}
	}

	protected UserService getUserService() {
		return userService;
	}

	@Required
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	protected ConfigurationService getConfigurationService() {
		return configurationService;
	}

	@Required
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
	
	
	
	

}
