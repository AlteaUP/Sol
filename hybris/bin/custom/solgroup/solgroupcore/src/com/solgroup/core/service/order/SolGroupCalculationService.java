package com.solgroup.core.service.order;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.util.TaxValue;

import java.util.Map;
import java.util.Set;

public interface SolGroupCalculationService extends CalculationService {

    void calculateTotalTaxValues(final AbstractOrderEntryModel entry);
    double calculateTotalTaxValues(final AbstractOrderModel order, final boolean recalculate, final int digits,
                                             final double taxAdjustmentFactor, final Map<TaxValue, Map<Set<TaxValue>, Double>> taxValueMap);
}
