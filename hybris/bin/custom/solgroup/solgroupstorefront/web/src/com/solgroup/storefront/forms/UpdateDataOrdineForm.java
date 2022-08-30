package com.solgroup.storefront.forms;

import java.util.Date;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class UpdateDataOrdineForm {
//	@NotNull(message = "{basket.error.quantity.notNull}")
//	@Min(value = 0, message = "{basket.error.quantity.invalid}")
//	@Digits(fraction = 0, integer = 10, message = "{basket.error.quantity.invalid}")
	private String dataOrdine;

	public void setDataOrdine(final String dataOrdine)
	{
		this.dataOrdine = dataOrdine;
	}

	public String getDataOrdine()
	{
		return dataOrdine;
	}
}
