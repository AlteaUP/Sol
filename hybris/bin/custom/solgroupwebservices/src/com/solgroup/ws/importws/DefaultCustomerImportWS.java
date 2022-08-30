/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2017 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * Hybris ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with SAP Hybris.
 */
package com.solgroup.ws.importws;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.ws.service.CommonWsService;
import com.solgroup.core.ws.services.customer.xml.CustomerImportHybris;
import com.solgroup.ws.ImportWS;


/**
 *
 */

public class DefaultCustomerImportWS implements ImportWS<CustomerImportHybris>
{

	private CommonWsService commonWsService;
	private static final String jobName = "importCustomerJob";

	@Override

	public void importData(final CustomerImportHybris data) throws Exception
	{

		getCommonWsService().prepareImportDataJob(data, jobName);

	}

	protected CommonWsService getCommonWsService()
	{
		return commonWsService;
	}

	@Required
	public void setCommonWsService(final CommonWsService commonWsService)
	{
		this.commonWsService = commonWsService;
	}
}
