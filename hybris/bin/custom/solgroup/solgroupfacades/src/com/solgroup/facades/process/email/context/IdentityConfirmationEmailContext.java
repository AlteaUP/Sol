/*
 * mdc
 */
package com.solgroup.facades.process.email.context;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.model.IdentityConfirmationProcessModel;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.commerceservices.model.process.StoreFrontCustomerProcessModel;


/**
 * Velocity context for a Identity Confirmation email.
 */
public class IdentityConfirmationEmailContext extends CustomerEmailContext
{
	private String token;
	private String path;


	public String getToken()
	{
		return token;
	}

	public void setToken(final String token)
	{
		this.token = token;
	}

	public String getURLEncodedToken() throws UnsupportedEncodingException
	{
		return URLEncoder.encode(token, "UTF-8");
	}

	
	public String getSecureIdentityConfirmationUrl() throws UnsupportedEncodingException
	{   
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(),true, getPath(),
				"token=" + getURLEncodedToken());
	}

	public String getDisplaySecureIdentityConfirmationUrl() throws UnsupportedEncodingException
	{

//		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), getUrlEncodingAttributes(),true, getPath(),
//				"token=" + getURLEncodedToken());
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(),getUrlEncodingAttributes(), true, getPath());
		
	}


	@Override
	public void init(final StoreFrontCustomerProcessModel storeFrontCustomerProcessModel, final EmailPageModel emailPageModel)
	{
		super.init(storeFrontCustomerProcessModel, emailPageModel);
		if (storeFrontCustomerProcessModel instanceof IdentityConfirmationProcessModel)
		{
			setToken(((IdentityConfirmationProcessModel) storeFrontCustomerProcessModel).getToken());
		}
	}

	protected String getPath() {
		return path;
	}

	@Required
	public void setPath(String path) {
		this.path = path;
	}
}
