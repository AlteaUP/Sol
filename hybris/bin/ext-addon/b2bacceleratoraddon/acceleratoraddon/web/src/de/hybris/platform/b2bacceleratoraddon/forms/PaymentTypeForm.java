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
package de.hybris.platform.b2bacceleratoraddon.forms;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


public class PaymentTypeForm
{
	private String paymentType;
	private String costCenterId;
	private String purchaseOrderNumber;
	private List<EntryPropertiesPlus> entryProperties;

	@NotNull(message = "{general.required}")
	@Size(min = 1, max = 255, message = "{general.required}")
	public String getPaymentType()
	{
		return paymentType;
	}

	public void setPaymentType(final String paymentType)
	{
		this.paymentType = paymentType;
	}

	public String getCostCenterId()
	{
		return costCenterId;
	}

	public void setCostCenterId(final String costCenterId)
	{
		this.costCenterId = costCenterId;
	}

	public String getPurchaseOrderNumber()
	{
		return purchaseOrderNumber;
	}

	public void setPurchaseOrderNumber(final String purchaseOrderNumber)
	{
		this.purchaseOrderNumber = purchaseOrderNumber;
	}

	/**
	 * @return the entryProperties
	 */
	public List<EntryPropertiesPlus> getEntryProperties()
	{
		return entryProperties;
	}

	/**
	 * @param entryProperties
	 *           the entryProperties to set
	 */
	public void setEntryProperties(final List<EntryPropertiesPlus> entryProperties)
	{
		this.entryProperties = entryProperties;
	}
}
