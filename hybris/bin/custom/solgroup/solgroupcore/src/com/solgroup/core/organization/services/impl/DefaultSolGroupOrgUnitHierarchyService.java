package com.solgroup.core.organization.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.hybris.platform.commerceservices.model.OrgUnitModel;
import de.hybris.platform.commerceservices.organization.services.impl.DefaultOrgUnitHierarchyService;

public class DefaultSolGroupOrgUnitHierarchyService extends DefaultOrgUnitHierarchyService {
	
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultSolGroupOrgUnitHierarchyService.class);
	
	@Override
	synchronized public <T extends OrgUnitModel> void generateUnitPaths(final Class<T> unitType)
	{
		LOG.info(".............................");
	}

}
