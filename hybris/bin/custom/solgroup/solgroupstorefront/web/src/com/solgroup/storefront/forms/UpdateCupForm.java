package com.solgroup.storefront.forms;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class UpdateCupForm {
//	@NotNull(message = "{basket.error.quantity.notNull}")
//	@Min(value = 0, message = "{basket.error.quantity.invalid}")
//	@Digits(fraction = 0, integer = 10, message = "{basket.error.quantity.invalid}")
	private String cup;

	public void setCup(final String cup)
	{
		this.cup = cup;
	}

	public String getCup()
	{
		return cup;
	}
}
