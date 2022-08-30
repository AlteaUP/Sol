package com.solgroup.facades.ordergridform.impl;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import com.solgroup.facades.ordergridform.SolGroupOrderGridFormFacade;

import de.hybris.platform.acceleratorfacades.ordergridform.impl.DefaultOrderGridFormFacade;
import de.hybris.platform.acceleratorfacades.product.data.LeafDimensionData;
import de.hybris.platform.acceleratorfacades.product.data.ReadOnlyOrderGridData;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.ConsignmentEntryData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.ProductFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.CategoryData;
import de.hybris.platform.commercefacades.product.data.ProductData;

public class DefaultSolGroupOrderGridFormFacade extends DefaultOrderGridFormFacade
        implements SolGroupOrderGridFormFacade {

    @Resource(name = "productVariantFacade")
    private ProductFacade productFacade;

    @Override
    public Map<String, ReadOnlyOrderGridData> getReadOnlyOrderGrid(final List<OrderEntryData> orderEntryDataList) {
        final Map<String, ReadOnlyOrderGridData> readOnlyMultiDMap = new LinkedHashMap<>();

        for (final OrderEntryData dimensionEntry : orderEntryDataList) {
            final Collection<CategoryData> categoryDataCollection = dimensionEntry.getProduct().getCategories();

            final Optional<AbstractMap.SimpleImmutableEntry<String, ReadOnlyOrderGridData>> readOnlyMultiDMapEntry = populateDataFor1DGrid(
                    categoryDataCollection.iterator().next(), dimensionEntry, categoryDataCollection);

            if (readOnlyMultiDMapEntry.isPresent()) {
                final AbstractMap.SimpleImmutableEntry<String, ReadOnlyOrderGridData> entry = readOnlyMultiDMapEntry
                        .get();
                readOnlyMultiDMap.put(entry.getKey(), entry.getValue());
            }
        }

        return readOnlyMultiDMap;
    }

    public Map<String, ReadOnlyOrderGridData> getReadOnlyConsignmentGrid(
            final List<ConsignmentEntryData> consignmentEntryDataList) {
        final Map<String, ReadOnlyOrderGridData> readOnlyMultiDMap = new LinkedHashMap<>();

        for (final ConsignmentEntryData dimensionEntry : consignmentEntryDataList) {
            final Collection<CategoryData> categoryDataCollection = dimensionEntry.getOrderEntry().getProduct()
                    .getCategories();

            final Optional<AbstractMap.SimpleImmutableEntry<String, ReadOnlyOrderGridData>> readOnlyMultiDMapEntry = populateDataFor1DGrid(
                    categoryDataCollection.iterator().next(), dimensionEntry);

            if (readOnlyMultiDMapEntry.isPresent()) {
                final AbstractMap.SimpleImmutableEntry<String, ReadOnlyOrderGridData> entry = readOnlyMultiDMapEntry
                        .get();
                readOnlyMultiDMap.put(entry.getKey(), entry.getValue());
            }
        }

        return readOnlyMultiDMap;
    }

    @Override
    protected Optional<AbstractMap.SimpleImmutableEntry<String, ReadOnlyOrderGridData>> populateDataFor1DGrid(
            final CategoryData categoryData, final OrderEntryData dimensionEntry) {
        final Map<String, String> dimensionHeaderMap = new LinkedHashMap<>();
        final LeafDimensionData leafDimensionData = new LeafDimensionData();
        dimensionHeaderMap.put(categoryData.getParentCategoryName(), categoryData.getName());
        leafDimensionData.setLeafDimensionData(dimensionEntry.getQuantity().toString());
        leafDimensionData.setPrice(dimensionEntry.getTotalPrice());
        leafDimensionData.setSequence(categoryData.getSequence());
        leafDimensionData.setUnit(dimensionEntry.getUnit());
        leafDimensionData.setPurchaseOrderNumber(dimensionEntry.getPurchaseOrderNumber());
        leafDimensionData.setCgi(dimensionEntry.getCgi());
        leafDimensionData.setCup(dimensionEntry.getCup());
        leafDimensionData.setDataOrdine(dimensionEntry.getDataOrdine());
        leafDimensionData.setBasePrice(dimensionEntry.getBasePrice());

        return Optional.of(new AbstractMap.SimpleImmutableEntry<>(dimensionEntry.getProduct().getCode(),
                populateAndSortReadonlyOrderGridData(dimensionEntry, leafDimensionData, dimensionHeaderMap)));

        // return Optional.of(new AbstractMap.SimpleImmutableEntry<>(categoryData.getCode(),
        // populateAndSortReadonlyOrderGridData(dimensionEntry, leafDimensionData, dimensionHeaderMap)));
    }

    protected Optional<AbstractMap.SimpleImmutableEntry<String, ReadOnlyOrderGridData>> populateDataFor1DGrid(
            final CategoryData categoryData, final OrderEntryData dimensionEntry,
            final Collection<CategoryData> categoryDataCollection) {
        final Map<String, String> dimensionHeaderMap = new LinkedHashMap<>();
        // dimensionHeaderMap.put(categoryData.getParentCategoryName(), categoryData.getName());

        for (CategoryData categoryData2 : categoryDataCollection) {
            dimensionHeaderMap.put(categoryData2.getParentCategoryName(), categoryData2.getName());
        }

        final LeafDimensionData leafDimensionData = new LeafDimensionData();
        leafDimensionData.setLeafDimensionData(dimensionEntry.getQuantity().toString());
        leafDimensionData.setPrice(dimensionEntry.getTotalPrice());
        leafDimensionData.setSequence(categoryData.getSequence());
        leafDimensionData.setUnit(dimensionEntry.getUnit());
        leafDimensionData.setPurchaseOrderNumber(dimensionEntry.getPurchaseOrderNumber());
        leafDimensionData.setCgi(dimensionEntry.getCgi());
        leafDimensionData.setCup(dimensionEntry.getCup());
        leafDimensionData.setDataOrdine(dimensionEntry.getDataOrdine());
        leafDimensionData.setBasePrice(dimensionEntry.getBasePrice());

        return Optional.of(new AbstractMap.SimpleImmutableEntry<>(dimensionEntry.getProduct().getCode(),
                populateAndSortReadonlyOrderGridData(dimensionEntry, leafDimensionData, dimensionHeaderMap)));
    }

    protected Optional<AbstractMap.SimpleImmutableEntry<String, ReadOnlyOrderGridData>> populateDataFor1DGrid(
            final CategoryData categoryData, final ConsignmentEntryData dimensionEntry) {
        final Map<String, String> dimensionHeaderMap = new LinkedHashMap<>();
        final LeafDimensionData leafDimensionData = new LeafDimensionData();
        dimensionHeaderMap.put(categoryData.getParentCategoryName(), categoryData.getName());
        leafDimensionData.setLeafDimensionData(dimensionEntry.getQuantity().toString());
        leafDimensionData.setPrice(dimensionEntry.getOrderEntry().getTotalPrice());
        leafDimensionData.setSequence(categoryData.getSequence());
        leafDimensionData.setUnit(dimensionEntry.getOrderEntry().getUnit());
        leafDimensionData.setPurchaseOrderNumber(dimensionEntry.getOrderEntry().getPurchaseOrderNumber());
        leafDimensionData.setCgi(dimensionEntry.getOrderEntry().getCgi());
        leafDimensionData.setCup(dimensionEntry.getOrderEntry().getCup());
        leafDimensionData.setDataOrdine(dimensionEntry.getOrderEntry().getDataOrdine());
        leafDimensionData.setBasePrice(dimensionEntry.getOrderEntry().getBasePrice());

        return Optional.of(new AbstractMap.SimpleImmutableEntry<>(dimensionEntry.getOrderEntry().getProduct().getCode(),
                populateAndSortReadonlyConsignmentGridData(dimensionEntry, leafDimensionData, dimensionHeaderMap)));
    }

    protected ReadOnlyOrderGridData populateAndSortReadonlyConsignmentGridData(
            final ConsignmentEntryData dimensionEntry, final LeafDimensionData leafDimensionData,
            final Map<String, String> dimensionHeaderMap) {
        final ReadOnlyOrderGridData readOnlyOrderGridData = new ReadOnlyOrderGridData();
        readOnlyOrderGridData.setProduct(dimensionEntry.getOrderEntry().getProduct());
        readOnlyOrderGridData.setDimensionHeaderMap(dimensionHeaderMap);
        readOnlyOrderGridData.setLeafDimensionDataSet(populateLeafDimensionData(leafDimensionData));

        return readOnlyOrderGridData;
    }

    @Override
    public Map<String, ReadOnlyOrderGridData> getReadOnlyOrderGridForProductInOrder(final String productCode,
            final Collection<ProductOption> productOptions, final AbstractOrderData abstractOrderData) {
        final ProductData productData = productFacade.getProductForCodeAndOptions(productCode, productOptions);
        final String baseProductCode = productData.getBaseProduct();

        final Optional<OrderEntryData> orderEntryDataOptional = abstractOrderData.getEntries().stream()
                .filter(orderEntryData -> orderEntryData.getProduct().getCode().equals(baseProductCode)).findFirst();

        return getReadOnlyOrderGrid(
                orderEntryDataOptional.map(entry -> entry.getEntries()).orElse(Collections.EMPTY_LIST));
    }
}
