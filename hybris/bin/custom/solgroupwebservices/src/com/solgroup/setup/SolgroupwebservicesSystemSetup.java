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
package com.solgroup.setup;

import static com.solgroup.constants.SolgroupwebservicesConstants.PLATFORM_LOGO_CODE;

import de.hybris.platform.core.initialization.SystemSetup;

import java.io.InputStream;

import com.solgroup.constants.SolgroupwebservicesConstants;
import com.solgroup.service.SolgroupwebservicesService;


@SystemSetup(extension = SolgroupwebservicesConstants.EXTENSIONNAME)
public class SolgroupwebservicesSystemSetup
{
	private final SolgroupwebservicesService solgroupwebservicesService;

	public SolgroupwebservicesSystemSetup(final SolgroupwebservicesService solgroupwebservicesService)
	{
		this.solgroupwebservicesService = solgroupwebservicesService;
	}

	@SystemSetup(process = SystemSetup.Process.INIT, type = SystemSetup.Type.ESSENTIAL)
	public void createEssentialData()
	{
		solgroupwebservicesService.createLogo(PLATFORM_LOGO_CODE);
	}

	private InputStream getImageStream()
	{
		return SolgroupwebservicesSystemSetup.class.getResourceAsStream("/solgroupwebservices/sap-hybris-platform.png");
	}
}
