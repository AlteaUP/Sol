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
package com.solgroup.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.apache.cxf.feature.Features;


/**
 *
 */

@WebService(targetNamespace = "http://importws.ws.solgroup.com/")
@Features(features = "org.apache.cxf.feature.LoggingFeature")
public interface ImportWS<T>
{
	@WebMethod(operationName = "importData")
	void importData(@WebParam(name = "Import_Hybris")T data) throws Exception;
}
