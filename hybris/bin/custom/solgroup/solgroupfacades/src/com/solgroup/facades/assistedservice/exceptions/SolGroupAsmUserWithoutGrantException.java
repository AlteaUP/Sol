package com.solgroup.facades.assistedservice.exceptions;

import de.hybris.platform.assistedserviceservices.exception.AssistedServiceException;

/**
 * @author fmilazzo
 *
 */
@SuppressWarnings("serial")
public class SolGroupAsmUserWithoutGrantException extends AssistedServiceException {

    public SolGroupAsmUserWithoutGrantException(final String message) {
        super(message);
    }

    @Override
    public String getMessageCode() {
        return "solgroup.asm.login.blocked";
    }

    @Override
    public String getAlertClass() {
        return ASM_ALERT_CREDENTIALS;
    }
}
