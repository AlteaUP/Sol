/**
 *
 */
package com.solgroup.core.ws.service;

import java.io.InputStream;

import com.solgroup.core.model.LegacySystemModel;

import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.c2l.CountryModel;


/**
 * @author marcodanielecoppola
 *
 */
public interface CommonWsService
{
	void prepareImportDataJob(Object dataToImport, String jobName) throws Exception;

	Object parseXmlStream(final Class objectClass, final InputStream stream);
	Object parseXmlStream_withoutRootNode(final Class objectClass, final InputStream stream);

	CMSSiteModel getCMSSite(final String legacySystemCode, final String countryIsoCode);
	
	CountryModel getCountryForLegacySystemAndSite(LegacySystemModel legacySystemModel, CMSSiteModel site);

}
