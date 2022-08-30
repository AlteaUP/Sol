package com.solgroup.facades.populators.product;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Resource;

import com.solgroup.commercefacades.product.SolGroupPriceDataFactory;

import de.hybris.platform.acceleratorfacades.order.data.PriceRangeData;
import de.hybris.platform.commercefacades.order.converters.populator.GroupOrderEntryPopulator;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class SolGroupGroupOrderEntryPopulator<S extends AbstractOrderModel, T extends AbstractOrderData>
        extends GroupOrderEntryPopulator<S, T> {

    @Resource(name = "productService")
    private ProductService productService;

    @Resource(name = "priceDataFactory")
    private SolGroupPriceDataFactory solGroupPriceDataFactory; 

    @Override
    public void populate(final AbstractOrderModel source, final AbstractOrderData target) throws ConversionException {
        target.setEntries(groupEntries(target.getEntries(), target));

        // Se esiste almeno una entry di tipo refill il carrello/ordine è refill
        if (source != null && source.getRefillOrderEntryNumber() > 0) {
            target.setRefill(Boolean.TRUE);
        } else {
            target.setRefill(Boolean.FALSE);
        }

    }

    @Override
    protected OrderEntryData createGroupedOrderEntry(final OrderEntryData firstEntry) {
        final OrderEntryData groupedEntry = new OrderEntryData();
        groupedEntry.setEntries(new ArrayList<OrderEntryData>());
        groupedEntry.setEntryNumber(INVALID_ENTRY_NUMBER);
        groupedEntry.setQuantity(ZERO_QUANTITY);

        final ProductData baseProduct = createBaseProduct(firstEntry.getProduct());
        groupedEntry.setProduct(baseProduct);
        groupedEntry.setUpdateable(firstEntry.isUpdateable());
        groupedEntry.setBasePrice(firstEntry.getBasePrice());
        groupedEntry.setUnit(firstEntry.getUnit());
        groupedEntry.setShowPrice(firstEntry.isShowPrice());
        groupedEntry.setPurchaseOrderNumber(firstEntry.getPurchaseOrderNumber());
        groupedEntry.setCgi(firstEntry.getCgi());

        groupedEntry.setSupportedActions(firstEntry.getSupportedActions());

        return groupedEntry;
    }

    @Override
    protected ProductData createBaseProduct(final ProductData variant) {
        final ProductData productData = new ProductData();

        productData.setUrl(variant.getUrl());
        productData.setPurchasable(variant.getPurchasable());
        productData.setMultidimensional(Boolean.TRUE);
        productData.setImages(variant.getImages());

        final ProductModel productModel = productService.getProductForCode(variant.getBaseProduct());
        productData.setCode(productModel.getCode());

        productData.setName(productModel.getName());
        productData.setDescription(productModel.getDescription());
        productData.setErpCode(productModel.getErpCode());
        productData.setPriceRange(new PriceRangeData());

        return productData;
    }

    @Override
    protected void consolidateGroupedOrderEntry(final Map<String, OrderEntryData> group) {
        for (final String productCode : group.keySet()) {
            final OrderEntryData parentEntry = group.get(productCode);
            if (parentEntry.getEntries() != null) {
                final OrderEntryData orderEntryData = parentEntry.getEntries().get(0);
                final PriceData firstEntryTotalPrice = orderEntryData.getTotalPrice();
                final PriceRangeData priceRange = parentEntry.getProduct().getPriceRange();
                if (firstEntryTotalPrice != null) {
                    priceRange.setMaxPrice(solGroupPriceDataFactory.create(orderEntryData.getBasePrice().getPriceType(),
                            getMaxPrice(parentEntry, firstEntryTotalPrice).getValue(),
                            orderEntryData.getBasePrice().getCurrencyIso(), orderEntryData.getBasePrice().getUnitMeasure()));
                    priceRange.setMinPrice(solGroupPriceDataFactory.create(orderEntryData.getBasePrice().getPriceType(),
                            getMinPrice(parentEntry, firstEntryTotalPrice).getValue(),
                            orderEntryData.getBasePrice().getCurrencyIso(), orderEntryData.getBasePrice().getUnitMeasure()));

                    parentEntry.setTotalPrice(getTotalPrice(parentEntry, firstEntryTotalPrice));
                }
                parentEntry.setQuantity(getTotalQuantity(parentEntry));
            }
        }
    }
}
