package com.solgroup.storefront.forms;

public class UpdatePoForm {
//	@NotNull(message = "{basket.error.quantity.notNull}")
//	@Min(value = 0, message = "{basket.error.quantity.invalid}")
//	@Digits(fraction = 0, integer = 10, message = "{basket.error.quantity.invalid}")
	private String purchaseOrderNumber;

	public void setPurchaseOrderNumber(final String purchaseOrderNumber)
	{
		this.purchaseOrderNumber = purchaseOrderNumber;
	}

	public String getPurchaseOrderNumber()
	{
		return purchaseOrderNumber;
	}
}
