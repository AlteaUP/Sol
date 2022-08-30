package com.solgroup.fulfilmentprocess.actions.orderexport;


import com.solgroup.core.enums.SubOrderProcessStatus;
import com.solgroup.core.model.SubOrderProcessModel;

import com.solgroup.service.utils.SolgroupWebServiceUtils;
import de.hybris.platform.processengine.action.AbstractAction;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class CheckOrderUpdateAction extends AbstractAction<SubOrderProcessModel> {
    private static final Logger LOG = Logger.getLogger(CheckOrderUpdateAction.class);

    public enum Transition {
        ERROR, BACKOFFICE_UPDATE, LEGACY_UPDATE, ORDER_FREEZED;

        public static Set<String> getStringValues() {
            final Set<String> res = new HashSet<String>();

            for (final Transition transition : Transition.values()) {
                res.add(transition.toString());
            }
            return res;
        }
    }

    @Override public final String execute(final SubOrderProcessModel process) {
        
        if (process.getStatus().equals(SubOrderProcessStatus.ORDER_FREEZED)) {
            return Transition.ORDER_FREEZED.toString();
        } else if (process.getStatus().equals(SubOrderProcessStatus.LEGACY_UPDATE)) {
            return Transition.LEGACY_UPDATE.toString();
        } else if (process.getStatus().equals(SubOrderProcessStatus.BACKOFFICE_UPDATE)) {
            return Transition.BACKOFFICE_UPDATE.toString();
        } else {
            return Transition.ERROR.toString();
        }
    }

    @Override public Set<String> getTransitions() {
        return Transition.getStringValues();
    }
}