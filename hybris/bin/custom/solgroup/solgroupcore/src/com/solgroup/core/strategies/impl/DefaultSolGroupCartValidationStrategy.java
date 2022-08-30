package com.solgroup.core.strategies.impl;

import org.apache.commons.lang.BooleanUtils;

import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType;
import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.order.CommerceCartModificationStatus;
import de.hybris.platform.commerceservices.strategies.impl.DefaultCartValidationStrategy;
import de.hybris.platform.core.model.order.CartEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

public class DefaultSolGroupCartValidationStrategy extends DefaultCartValidationStrategy{

	@Override
	protected void validateDelivery(final CartModel cartModel)
	{
		if (cartModel.getDeliveryAddress() != null)
		{
			if (!isGuestUserCart(cartModel) && cartModel.getUnit()!=null && !cartModel.getUnit().equals(cartModel.getDeliveryAddress().getOwner()))
			{
				cartModel.setDeliveryAddress(null);
				getModelService().save(cartModel);
			}
		}
	}
	
	@Override
	protected CommerceCartModification validateCartEntry(final CartModel cartModel, final CartEntryModel cartEntryModel)
	{
		// First verify that the product exists
		try
		{
			getProductService().getProductForCode(cartEntryModel.getProduct().getCode());
		}
		catch (final UnknownIdentifierException e)
		{
			final CommerceCartModification modification = new CommerceCartModification();
			modification.setStatusCode(CommerceCartModificationStatus.UNAVAILABLE);
			modification.setQuantityAdded(0);
			modification.setQuantity(0);

			final CartEntryModel entry = new CartEntryModel()
			{
				@Override
				public Double getBasePrice()
				{
					return null;
				}

				@Override
				public Double getTotalPrice()
				{
					return null;
				}
			};
			entry.setProduct(cartEntryModel.getProduct());

			modification.setEntry(entry);

			getModelService().remove(cartEntryModel);
			getModelService().refresh(cartModel);

			return modification;
		}

		// Overall availability of this product
        if (BooleanUtils.isTrue(cartEntryModel.getProduct().getStockable())) {
			final Long stockLevel = getStockLevel(cartEntryModel);
		

			// Overall stock quantity in the cart
			final long cartLevel = getCartLevel(cartEntryModel, cartModel);
	
			// Stock quantity for this cartEntry
			final long cartEntryLevel = cartEntryModel.getQuantity().longValue();
	
			// New stock quantity for this cartEntry
			final long newOrderEntryLevel;
	
			Long stockLevelForProductInBaseStore = null;
	
			if (stockLevel != null)
			{
				// this product is not available at the given point of service.
				if (isProductNotAvailableInPOS(cartEntryModel, stockLevel))
				{
	
					stockLevelForProductInBaseStore = getCommerceStockService().getStockLevelForProductAndBaseStore(
							cartEntryModel.getProduct(), getBaseStoreService().getCurrentBaseStore());
	
					if (stockLevelForProductInBaseStore != null)
					{
						newOrderEntryLevel = Math.min(cartEntryLevel, stockLevelForProductInBaseStore.longValue());
					}
					else
					{
						newOrderEntryLevel = Math.min(cartEntryLevel, cartLevel);
					}
				}
				else
				{
					// if stock is available.. get either requested quantity if its lower than available stock or maximum stock.
					newOrderEntryLevel = Math.min(cartEntryLevel, stockLevel.longValue());
				}
			}
			else
			{
				// if stock is not available.. play save.. only allow quantity that was already in cart.
				newOrderEntryLevel = Math.min(cartEntryLevel, cartLevel);
			}
	
			// this product is not available at the given point of service.
			if (stockLevelForProductInBaseStore != null && stockLevelForProductInBaseStore.longValue() != 0)
			{
				final CommerceCartModification modification = new CommerceCartModification();
				modification.setStatusCode(CommerceCartModificationStatus.MOVED_FROM_POS_TO_STORE);
				final CartEntryModel existingEntryForProduct = getExistingShipCartEntryForProduct(cartModel, cartEntryModel.getProduct());
				if (existingEntryForProduct != null)
				{
					getModelService().remove(cartEntryModel);
					final long quantityAdded = stockLevelForProductInBaseStore.longValue() >= cartLevel ? newOrderEntryLevel : cartLevel
							- stockLevelForProductInBaseStore.longValue();
					modification.setQuantityAdded(quantityAdded);
					final long updatedQuantity = (stockLevelForProductInBaseStore.longValue() <= cartLevel ? stockLevelForProductInBaseStore
							.longValue() : cartLevel);
					modification.setQuantity(updatedQuantity);
					existingEntryForProduct.setQuantity(Long.valueOf(updatedQuantity));
					getModelService().save(existingEntryForProduct);
					modification.setEntry(existingEntryForProduct);
				}
				else
				{
					modification.setQuantityAdded(newOrderEntryLevel);
					modification.setQuantity(cartEntryLevel);
					cartEntryModel.setDeliveryPointOfService(null);
					modification.setEntry(cartEntryModel);
					getModelService().save(cartEntryModel);
				}
	
				getModelService().refresh(cartModel);
	
				return modification;
			}
			else if ((stockLevel != null && stockLevel.longValue() <= 0) || newOrderEntryLevel < 0)
			{
				// If no stock is available or the cart is full for this product, remove the entry from the cart
				final CommerceCartModification modification = new CommerceCartModification();
				modification.setStatusCode(CommerceCartModificationStatus.NO_STOCK);
				modification.setQuantityAdded(0);//New quantity for this entry
				modification.setQuantity(cartEntryLevel);//Old quantity for this entry
				final CartEntryModel entry = new CartEntryModel()
				{
					@Override
					public Double getBasePrice()
					{
						return null;
					}
	
					@Override
					public Double getTotalPrice()
					{
						return null;
					}
				};
				entry.setProduct(cartEntryModel.getProduct());
				modification.setEntry(entry);
				getModelService().remove(cartEntryModel);
				getModelService().refresh(cartModel);
	
				return modification;
			}
			else if (cartEntryLevel != newOrderEntryLevel)
			{
				// If the orderLevel has changed for this cartEntry, then recalculate the quantity
				final CommerceCartModification modification = new CommerceCartModification();
				modification.setStatusCode(CommerceCartModificationStatus.LOW_STOCK);
				modification.setQuantityAdded(newOrderEntryLevel);
				modification.setQuantity(cartEntryLevel);
				modification.setEntry(cartEntryModel);
				cartEntryModel.setQuantity(Long.valueOf(newOrderEntryLevel));
				getModelService().save(cartEntryModel);
				getModelService().refresh(cartModel);
	
				return modification;
			}
			else if (hasConfigurationErrors(cartEntryModel))
			{
				final CommerceCartModification modification = new CommerceCartModification();
				modification.setStatusCode(CommerceCartModificationStatus.CONFIGURATION_ERROR);
				modification.setQuantityAdded(cartEntryLevel);
				modification.setQuantity(cartEntryLevel);
				modification.setEntry(cartEntryModel);
	
				return modification;
			}
			else
			{
				final CommerceCartModification modification = new CommerceCartModification();
				modification.setStatusCode(CommerceCartModificationStatus.SUCCESS);
				modification.setQuantityAdded(cartEntryLevel);
				modification.setQuantity(cartEntryLevel);
				modification.setEntry(cartEntryModel);
	
				return modification;
			}
		}
		else
		{
			final CommerceCartModification modification = new CommerceCartModification();
			modification.setStatusCode(CommerceCartModificationStatus.SUCCESS);
			modification.setQuantityAdded(cartEntryModel.getQuantity());
			modification.setQuantity(cartEntryModel.getQuantity());
			modification.setEntry(cartEntryModel);

			return modification;
		}
	}
}
