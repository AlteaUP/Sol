package com.solgroup.facades.storesession.impl;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.commercefacades.storesession.data.LanguageData;
import de.hybris.platform.commercefacades.storesession.impl.DefaultStoreSessionFacade;
import de.hybris.platform.core.model.c2l.LanguageModel;

public class DefaultSolGroupStoreSessionFacade extends DefaultStoreSessionFacade {

	
	private CMSSiteService cmsSiteService;
	private Logger LOG = Logger.getLogger(DefaultSolGroupStoreSessionFacade.class);
	
	@Override
	protected void initializeSessionLanguage(final List<Locale> preferredLocales)
	{

		
		CMSSiteModel cmsSite = getCmsSiteService().getCurrentSite();
		if(cmsSite.getDefaultLanguage()!=null) {
			LanguageModel defaultSiteLanguage = cmsSite.getDefaultLanguage();
			setCurrentLanguage(StringUtils.defaultString(defaultSiteLanguage.getIsocode()));
			LOG.debug(String.format("Use default language [%s] from site [%s]", defaultSiteLanguage.getIsocode(), cmsSite.getUid()));
			return;
		}

		LOG.info("No default language found for site " + cmsSite.getUid());
		
		if (preferredLocales != null && !preferredLocales.isEmpty())
		{
			// Find the preferred locale that is in our set of supported languages
			final Collection<LanguageData> storeLanguages = getAllLanguages();
			if (storeLanguages != null && !storeLanguages.isEmpty())
			{
				final LanguageData bestLanguage = findBestLanguage(storeLanguages, preferredLocales);
				if (bestLanguage != null)
				{
					setCurrentLanguage(StringUtils.defaultString(bestLanguage.getIsocode()));
					return;
				}
			}
		}

		// Try to use the default language for the site
		final LanguageData defaultLanguage = getDefaultLanguage();
		if (defaultLanguage != null)
		{
			setCurrentLanguage(defaultLanguage.getIsocode());
		}
	}

	protected CMSSiteService getCmsSiteService() {
		return cmsSiteService;
	}

	@Required
	public void setCmsSiteService(CMSSiteService cmsSiteService) {
		this.cmsSiteService = cmsSiteService;
	}
	
	
	

}
