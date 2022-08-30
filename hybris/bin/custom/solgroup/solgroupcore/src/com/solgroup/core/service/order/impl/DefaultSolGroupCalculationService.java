package com.solgroup.core.service.order.impl;

import com.solgroup.core.service.order.SolGroupCalculationService;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.order.impl.DefaultCalculationService;
import de.hybris.platform.util.TaxValue;
import org.apache.commons.collections.MapUtils;

import java.util.*;

public class DefaultSolGroupCalculationService extends DefaultCalculationService implements SolGroupCalculationService {

    @Override
    public void calculateTotalTaxValues(AbstractOrderEntryModel entry) {
        final AbstractOrderModel order = entry.getOrder();
        final double quantity = entry.getQuantity().doubleValue();
        final double totalPrice = entry.getTotalPrice().doubleValue();
        final CurrencyModel curr = order.getCurrency();
        final int digits = curr.getDigits().intValue();

        if(order.getPaymentInfo() != null && order.getPaymentInfo().getRequireTaxCalculation() != null
                && order.getPaymentInfo().getRequireTaxCalculation()){

            entry.setTaxValues(TaxValue.apply(quantity, totalPrice, digits, entry.getTaxValues(), order.getNet().booleanValue(),
                    curr.getIsocode()));
        }else {
            entry.setTaxValues(new ArrayList<>());
        }
    }

    @Override
    public double calculateTotalTaxValues(AbstractOrderModel order, boolean recalculate, int digits, double taxAdjustmentFactor, Map<TaxValue, Map<Set<TaxValue>, Double>> taxValueMap) {

        if (recalculate)
        {
            if (order.getPaymentInfo() != null && order.getPaymentInfo().getRequireTaxCalculation() != null
                    && order.getPaymentInfo().getRequireTaxCalculation())
            {
                final CurrencyModel curr = order.getCurrency();
                final String iso = curr.getIsocode();
                //Do we still need it?
                //removeAllTotalTaxValues();
                final boolean net = order.getNet().booleanValue();
                double totalTaxes = 0.0;
                // compute absolute taxes if necessary
                if (MapUtils.isNotEmpty(taxValueMap))
                {
                    // adjust total taxes if additional costs or discounts are present
                    //	create tax values which contains applied values too
                    final Collection orderTaxValues = new ArrayList<TaxValue>(taxValueMap.size());
                    for (final Map.Entry<TaxValue, Map<Set<TaxValue>, Double>> taxValueEntry : taxValueMap.entrySet())
                    {
                        // e.g. VAT_FULL 19%
                        final TaxValue unappliedTaxValue = taxValueEntry.getKey();
                        // e.g. { (VAT_FULL 19%, CUSTOM 2%) -> 10EUR, (VAT_FULL) -> 20EUR }
                        // or, in case of absolute taxes one single entry
                        // { (ABS_1 4,50EUR) -> 2 (pieces) }
                        final Map<Set<TaxValue>, Double> taxGroups = taxValueEntry.getValue();

                        final TaxValue appliedTaxValue;

                        if (unappliedTaxValue.isAbsolute())
                        {
                            // absolute tax entries ALWAYS map to a single-entry map -> we'll use a shortcut here:
                            final double quantitySum = taxGroups.entrySet().iterator().next().getValue().doubleValue();
                            appliedTaxValue = calculateAbsoluteTotalTaxValue(curr, iso, digits, net, unappliedTaxValue, quantitySum);
                        }
                        else if (net)
                        {
                            appliedTaxValue = applyNetMixedRate(unappliedTaxValue, taxGroups, digits, taxAdjustmentFactor);
                        }
                        else
                        {
                            appliedTaxValue = applyGrossMixedRate(unappliedTaxValue, taxGroups, digits, taxAdjustmentFactor);
                        }
                        totalTaxes += appliedTaxValue.getAppliedValue();
                        orderTaxValues.add(appliedTaxValue);
                    }
                    order.setTotalTaxValues(orderTaxValues);
                }
                return totalTaxes;
            }
            else
            {
                order.setTotalTaxValues(new ArrayList<>());
                return 0;
            }
        } else {
            return order.getTotalTax().doubleValue();
        }
    }
}
