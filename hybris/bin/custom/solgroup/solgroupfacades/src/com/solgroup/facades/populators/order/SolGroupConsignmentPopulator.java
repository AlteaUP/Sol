package com.solgroup.facades.populators.order;

import de.hybris.platform.commercefacades.order.converters.populator.ConsignmentPopulator;
import de.hybris.platform.commercefacades.order.data.ConsignmentData;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;

public class SolGroupConsignmentPopulator extends ConsignmentPopulator {

    @Override
    public void populate(final ConsignmentModel source, final ConsignmentData target) {
        super.populate(source, target);

        target.setDocumentType(source.getDocumentType());
        target.setTrackingLink(source.getTrackingLink());
        target.setDocumentNumber(source.getDocumentNumber());
        target.setCarrierCode(source.getCarrier());
        target.setStatusDisplay(source.getStatus().toString());

    }

}
