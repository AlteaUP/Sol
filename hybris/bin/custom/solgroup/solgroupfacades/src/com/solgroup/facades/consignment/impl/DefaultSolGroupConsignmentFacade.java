package com.solgroup.facades.consignment.impl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.consignment.SolGroupConsignmentService;
import com.solgroup.facades.consignment.SolGroupConsignmentFacade;

import de.hybris.platform.acceleratorfacades.product.data.LeafDimensionData;
import de.hybris.platform.acceleratorfacades.product.data.ReadOnlyOrderGridData;
import de.hybris.platform.commercefacades.order.data.ConsignmentData;
import de.hybris.platform.commercefacades.order.data.ConsignmentEntryData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.CategoryData;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

/**
 * 
 * @author fmilazzo
 *
 */
public class DefaultSolGroupConsignmentFacade implements SolGroupConsignmentFacade {

    private SolGroupConsignmentService solGroupConsignmentService;
    private Converter<ConsignmentModel, ConsignmentData> consignmentConverter;

    @Override
    public ConsignmentData getConsignmentDetailsForCodeWithoutUser(final String consignmentCode,
            final String orderCode) {
        final ConsignmentModel consignmentModel = getSolGroupConsignmentService().getConsignmentForCode(consignmentCode,
                orderCode);
        if (consignmentModel == null) {
            throw new UnknownIdentifierException("Consignment with orderCode " + orderCode + " and consignmentCode "
                    + consignmentCode + " not found!");
        }
        return getConsignmentConverter().convert(consignmentModel);
    }

    @Override
    public Map<String, ReadOnlyOrderGridData> getReadOnlyConsignmentGridForProductInOrder(
            final ConsignmentData consignmentData) {
        List<OrderEntryData> orderEntryDataList = new ArrayList<OrderEntryData>();
        for (ConsignmentEntryData consignmentEntryData : consignmentData.getEntries()) {
            OrderEntryData orderEntryData = consignmentEntryData.getOrderEntry();
            orderEntryData.setQuantity(consignmentEntryData.getShippedQuantity());
            if (StringUtils.isNotEmpty(orderEntryData.getProduct().getBaseProduct())) {
                orderEntryDataList.add(orderEntryData);
            }
        }
        return getReadOnlyConsignmentGrid(orderEntryDataList);
    }

    public Map<String, ReadOnlyOrderGridData> getReadOnlyConsignmentGrid(
            final List<OrderEntryData> orderEntryDataList) {
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

    protected ReadOnlyOrderGridData populateAndSortReadonlyOrderGridData(final OrderEntryData dimensionEntry,
            final LeafDimensionData leafDimensionData, final Map<String, String> dimensionHeaderMap) {
        final ReadOnlyOrderGridData readOnlyOrderGridData = new ReadOnlyOrderGridData();
        readOnlyOrderGridData.setProduct(dimensionEntry.getProduct());
        readOnlyOrderGridData.setDimensionHeaderMap(dimensionHeaderMap);
        readOnlyOrderGridData.setLeafDimensionDataSet(populateLeafDimensionData(leafDimensionData));

        return readOnlyOrderGridData;
    }

    protected Set<LeafDimensionData> populateLeafDimensionData(final LeafDimensionData leafDimensionData) {
        final Set<LeafDimensionData> leafDimensionDataSet = new TreeSet<>(
                (obj1, obj2) -> obj1.getSequence() - obj2.getSequence());
        leafDimensionDataSet.add(leafDimensionData);

        return leafDimensionDataSet;
    }

    protected SolGroupConsignmentService getSolGroupConsignmentService() {
        return solGroupConsignmentService;
    }

    @Required
    public void setSolGroupConsignmentService(SolGroupConsignmentService solGroupConsignmentService) {
        this.solGroupConsignmentService = solGroupConsignmentService;
    }

    protected Converter<ConsignmentModel, ConsignmentData> getConsignmentConverter() {
        return consignmentConverter;
    }

    @Required
    public void setConsignmentConverter(Converter<ConsignmentModel, ConsignmentData> consignmentConverter) {
        this.consignmentConverter = consignmentConverter;
    }

}
