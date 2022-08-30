package com.solgroup.storefront.forms;


public class UpdateCgiForm {
//	@NotNull(message = "{basket.error.quantity.notNull}")
//	@Min(value = 0, message = "{basket.error.quantity.invalid}")
//	@Digits(fraction = 0, integer = 10, message = "{basket.error.quantity.invalid}")
	private String cgi;

	public void setCgi(final String cgi)
	{
		this.cgi = cgi;
	}

	public String getCgi()
	{
		return cgi;
	}
}
