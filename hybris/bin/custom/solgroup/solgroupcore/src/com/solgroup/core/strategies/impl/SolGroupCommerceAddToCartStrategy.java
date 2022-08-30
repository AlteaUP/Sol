package com.solgroup.core.strategies.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.CommerceCartModificationStatus;
import de.hybris.platform.commerceservices.order.impl.DefaultCommerceAddToCartStrategy;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.storelocator.model.PointOfServiceModel;

public class SolGroupCommerceAddToCartStrategy extends DefaultCommerceAddToCartStrategy {

    private CalculationService calculationService;

    @Override
    protected void mergeEntry(@Nonnull final CommerceCartModification modification,
            @Nonnull final CommerceCartParameter parameter) throws CommerceCartModificationException {
        ServicesUtil.validateParameterNotNullStandardMessage("modification", modification);
        if (modification.getEntry() == null
                || Objects.equals(modification.getEntry().getQuantity(), Long.valueOf(0L))) {
            // nothing to merge
            return;
        }
        ServicesUtil.validateParameterNotNullStandardMessage("parameter", parameter);
        if (parameter.isCreateNewEntry()) {
            return;
        }
        final AbstractOrderModel cart = modification.getEntry().getOrder();
        if (cart == null) {
            // The entry is not in cart (most likely it's a stub)
            return;
        }
        final AbstractOrderEntryModel mergeTarget = getEntryMergeStrategy().getEntryToMerge(cart.getEntries(),
                modification.getEntry());
        if (mergeTarget == null) {
            if (parameter.getEntryNumber() != CommerceCartParameter.DEFAULT_ENTRY_NUMBER) {
                throw new CommerceCartModificationException("The new entry can not be merged into the entry #"
                        + parameter.getEntryNumber() + ". Give a correct value or "
                        + CommerceCartParameter.DEFAULT_ENTRY_NUMBER + " to accept any suitable entry.");
            }
        } else {
            // Merge the original entry into the merge target and remove the original entry.
            final Map<Integer, Long> entryQuantities = new HashMap<>(2);
            entryQuantities.put(mergeTarget.getEntryNumber(), Long.valueOf(
                    modification.getEntry().getQuantity().longValue() + mergeTarget.getQuantity().longValue()));
            entryQuantities.put(modification.getEntry().getEntryNumber(), Long.valueOf(0L));
            getCartService().updateQuantities(parameter.getCart(), entryQuantities);

            // Bug fix from Techedge
            getCalculationService().calculateTotals(mergeTarget, true);
            getModelService().save(mergeTarget);

            modification.setEntry(mergeTarget);

        }

    }

    @Override
    protected void validateAddToCart(final CommerceCartParameter parameters) throws CommerceCartModificationException {
        final CartModel cartModel = parameters.getCart();
        final ProductModel productModel = parameters.getProduct();

        validateParameterNotNull(cartModel, "Cart model cannot be null");
        validateParameterNotNull(productModel, "Product model cannot be null");
        // if (productModel.getVariantType() != null)
        // {
        // throw new CommerceCartModificationException("Choose a variant instead of the base product");
        // }

        if (parameters.getQuantity() < 1) {
            throw new CommerceCartModificationException("Quantity must not be less than one");
        }
    }

    @Override
    protected CommerceCartModification doAddToCart(final CommerceCartParameter parameter)
            throws CommerceCartModificationException {
        CommerceCartModification modification;

        final CartModel cartModel = parameter.getCart();
        final ProductModel productModel = parameter.getProduct();
        final long quantityToAdd = parameter.getQuantity();
        final PointOfServiceModel deliveryPointOfService = parameter.getPointOfService();

        this.beforeAddToCart(parameter);
        validateAddToCart(parameter);

        if (isProductForCode(parameter).booleanValue()) {
            // So now work out what the maximum allowed to be added is (note that this may be negative!)
            final long actualAllowedQuantityChange = getAllowedCartAdjustmentForProduct(cartModel, productModel,
                    quantityToAdd, deliveryPointOfService);
            final Integer maxOrderQuantity = productModel.getMaxOrderQuantity();
            final long cartLevel = checkCartLevel(productModel, cartModel, deliveryPointOfService);
            final long cartLevelAfterQuantityChange = actualAllowedQuantityChange + cartLevel;

            if (actualAllowedQuantityChange > 0) {
                // We are allowed to add items to the cart
                final CartEntryModel entryModel = addCartEntry(parameter, actualAllowedQuantityChange);
                getModelService().save(entryModel);

                // FIX SOL355
                final boolean isQuantityForRefill = isRefillLqd(parameter.getProduct());

                final String statusCode = getStatusCodeAllowedQuantityChange(actualAllowedQuantityChange,
                        maxOrderQuantity, quantityToAdd, cartLevelAfterQuantityChange, isQuantityForRefill);

                modification = createAddToCartResp(parameter, statusCode, entryModel, actualAllowedQuantityChange);
            } else {
                // Not allowed to add any quantity, or maybe even asked to reduce the quantity
                // Do nothing!
                final String status = getStatusCodeForNotAllowedQuantityChange(maxOrderQuantity, maxOrderQuantity);

                modification = createAddToCartResp(parameter, status, createEmptyCartEntry(parameter), 0);

            }
        } else {
            modification = createAddToCartResp(parameter, CommerceCartModificationStatus.UNAVAILABLE,
                    createEmptyCartEntry(parameter), 0);
        }

        return modification;
    }

    // FIX SOL355
    private boolean isRefillLqd(ProductModel productModel) {
        final String refillMaterial = getConfigurationService().getConfiguration().getString("quantity.code.materials");
        final String refillUnit = getConfigurationService().getConfiguration().getString("quantity.code.units");
        final String material = productModel.getMaterial().getCode();
        final String unit = productModel.getUnit().getCode();
        if (refillMaterial.equals(material) && !refillUnit.equals(unit)) {
            return true;
        }
        return false;
    }

    protected String getStatusCodeAllowedQuantityChange(final long actualAllowedQuantityChange,
            final Integer maxOrderQuantity, final long quantityToAdd, final long cartLevelAfterQuantityChange,
            final boolean isQuantityForRefill) {
        // Are we able to add the quantity we requested?
        if (isMaxOrderQuantitySet(maxOrderQuantity) && (actualAllowedQuantityChange < quantityToAdd)
                && (cartLevelAfterQuantityChange == maxOrderQuantity.longValue())) {
            return CommerceCartModificationStatus.MAX_ORDER_QUANTITY_EXCEEDED;
            // FIX SOL355
        } else if (quantityToAdd > 1 && isQuantityForRefill) {
            return CommerceCartModificationStatus.MAX_ORDER_QUANTITY_EXCEEDED;
        } else if (actualAllowedQuantityChange == quantityToAdd) {
            return CommerceCartModificationStatus.SUCCESS;
        } else {
            return CommerceCartModificationStatus.LOW_STOCK;
        }
    }

    protected CalculationService getCalculationService() {
        return calculationService;
    }

    @Required
    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

}
