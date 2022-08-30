/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.solgroup.constants;

/**
 * Global class for all Solgroupwebservices constants. You can add global constants for your extension into this class.
 */
public final class SolgroupwebservicesConstants extends GeneratedSolgroupwebservicesConstants
{
	public static final String EXTENSIONNAME = "solgroupwebservices";

	private SolgroupwebservicesConstants()
	{
		//empty to avoid instantiating this constant class
	}

	// implement here constants used by this extension

    public static final String PLATFORM_LOGO_CODE = "solgroupwebservicesPlatformLogo";
    
    public static final String CONFIRM_ACTION_OK = "OK";
    public static final String CONFIRM_ACTION_KO = "OK";
    public static final String SUBORDER_EXPORT_PROCESS = "suborder-export-process";
    public static final String CONSIGNMENT_PROCESS_NAME = "consignment-custom-process";
    
	public static final String PROPERTY_NAME_PO_EXPORT_ORDER_SERVICE_URL = "app.core.services.serviceUrl.po.exportOrder";
	public static final String PROPERTY_NAME_DELIVERY_OFFSET_DATE = "com.solgroup.deliveryDate.offset.days";
	
    public static final String PROPERTY_NAME_PO_USR = "app.core.services.po.usr";
    public static final String PROPERTY_NAME_PO_PWD = "app.core.services.po.pwd";
    public static final String PROPERTY_NAME_PO_PROXY_HOST = "app.core.services.po.proxy.host";
    public static final String PROPERTY_NAME_PO_PROXY_PORT = "app.core.services.po.proxy.port";
    public static final String PROPERTY_NAME_PO_TIMEOUT_SOAP = "app.core.services.po.soap.timeout";



}
