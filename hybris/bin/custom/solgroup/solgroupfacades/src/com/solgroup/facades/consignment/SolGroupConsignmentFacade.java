package com.solgroup.facades.consignment;

import java.util.Map;

import de.hybris.platform.acceleratorfacades.product.data.ReadOnlyOrderGridData;
import de.hybris.platform.commercefacades.order.data.ConsignmentData;

/**
 * 
 * @author fmilazzo
 *
 */
public interface SolGroupConsignmentFacade {

    ConsignmentData getConsignmentDetailsForCodeWithoutUser(final String consignmentCode, final String orderCode);

    Map<String, ReadOnlyOrderGridData> getReadOnlyConsignmentGridForProductInOrder(
            final ConsignmentData consignmentData);

}
