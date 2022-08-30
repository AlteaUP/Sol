package com.solgroup.core.asm.events;

import java.util.UUID;

import de.hybris.platform.assistedserviceservices.events.impl.DefaultCustomerSupportEventService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.ticketsystem.events.SessionEvent;
import de.hybris.platform.ticketsystem.events.model.SessionEventModel;

public class SolGroupCustomerSupportEventService extends DefaultCustomerSupportEventService {

    @Override
    protected SessionEventModel createAndPopulateSessionEventInfo(final Class csSessionEventClass,
            final SessionEvent asmEventData) {

        if (asmEventData.getAgent() instanceof EmployeeModel || asmEventData.getAgent() instanceof B2BCustomerModel) {
            final SessionEventModel csSessionEventModel = getModelService().create(csSessionEventClass);

            csSessionEventModel.setAgent((EmployeeModel) asmEventData.getAgent());
            csSessionEventModel.setEventTime(asmEventData.getCreatedAt());
            csSessionEventModel.setSessionID(UUID.randomUUID().toString());
            csSessionEventModel.setBaseSite(getBaseSiteService().getCurrentBaseSite());
            csSessionEventModel.setGroups(asmEventData.getAgentGroups());

            return csSessionEventModel;
        } else {
            throw new IllegalArgumentException("Wrong agentID value");
        }
    }

}
