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
package com.solgroup.facades.exception;

import de.hybris.platform.b2bacceleratorfacades.exception.DomainException;

/**
 * The Class de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException.
 */
public class EntityValidationException extends DomainException
{
    private static final long serialVersionUID = 1L;
    /**
     * Creates a new instance with the given message.
     *
     * @param message the reason for this de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException
     */
    public EntityValidationException(final String message)
    {
        super(message);
    }
    /**
     * Creates a new instance using the given message and cause
     * exception.
     *
     * @param message The reason for this de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException.
     * @param cause the Throwable that caused this de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException.
     */
    public EntityValidationException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
