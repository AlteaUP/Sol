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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.ws.service.CommonWsService;
import com.solgroup.core.ws.services.customerpricelist.xml.CustomerPriceListsImportHybris;
import com.solgroup.ws.ImportWS;


/**
 *
 */
@WebService(targetNamespace = "http://importws.ws.solgroup.com/")
public class DefaultPriceListImportWS implements ImportWS<CustomerPriceListsImportHybris>
{

	private CommonWsService commonWsService;

	private static final String jobName = "importPriceListsJob";

	@Override
	@WebMethod(operationName = "importData")
	public void importData(final CustomerPriceListsImportHybris data) throws Exception
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
