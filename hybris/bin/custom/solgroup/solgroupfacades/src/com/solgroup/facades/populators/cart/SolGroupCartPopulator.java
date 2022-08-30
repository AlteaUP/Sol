package com.solgroup.facades.populators.cart;

import de.hybris.platform.commercefacades.order.converters.populator.CartPopulator;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.core.model.order.CartModel;

public class SolGroupCartPopulator extends CartPopulator<CartData>{

	public void populate(final CartModel source, final CartData target) {
		super.populate(source, target);
	}
	
	
	
	/*
	@Override
	protected void addEntryGroups(final AbstractOrderModel source, final AbstractOrderData target)
	{
		if (target.getEntries() == null)
		{
			target.setEntries(Collections.emptyList());
		}
		final List<EntryGroupData> rootGroups;
		if (source.getEntryGroups() == null)
		{
			rootGroups = Collections.emptyList();
		}
		else
		{
			final List<List<EntryGroupData>> groupTrees = source.getEntryGroups().stream()
					.map(getEntryGroupService()::getNestedGroups).map(getEntryGroupConverter()::convertAll)
					.collect(Collectors.toList());
			rootGroups = groupTrees.stream().map(tree -> tree.get(0)).collect(Collectors.toList());
			final List<EntryGroup> sourceGroups = source.getEntryGroups().stream().map(getEntryGroupService()::getNestedGroups)
					.flatMap(Collection::stream).collect(Collectors.toList());
			final Map<Integer, EntryGroupData> targetGroups = groupTrees.stream().flatMap(Collection::stream)
					.collect(Collectors.toMap(x -> x.getGroupNumber(), x -> x));
			updateEntryGroupReferences(sourceGroups, targetGroups);
			assignParentGroups(targetGroups.values());
		}

		final MultivaluedMap<Integer, OrderEntryData> groupIdToEntryDataMap = mapGroupIdToEntryData(source, target);
		rootGroups.forEach(rootGroup -> assignEntriesToGroups(rootGroup, groupIdToEntryDataMap));
		final List<OrderEntryData> standaloneEntries = groupIdToEntryDataMap.get(null);
		final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		if(standaloneEntries != null){
			for (final OrderEntryData entry : standaloneEntries)
			{
				entry.setPurchaseOrderNumber(source.getEntries().get(entry.getEntryNumber()).getPurchaseOrderNumber());
				entry.setCgi(source.getEntries().get(entry.getEntryNumber()).getCgi());
				entry.setCup(source.getEntries().get(entry.getEntryNumber()).getCup());
				Date dataOrdine = source.getEntries().get(entry.getEntryNumber()).getDataOrdine();
				if(dataOrdine != null){
					entry.setDataOrdine(sdf.format(dataOrdine));
				}
			}
		}
		if (CollectionUtils.isEmpty(standaloneEntries))
		{
			target.setRootGroups(rootGroups);
		}
		else
		{
			// Wrap root groups to make the array extensible.
			final List<EntryGroupData> groups = new ArrayList<>(rootGroups);
			standaloneEntries.forEach(entry -> getVirtualEntryGroupStrategy().createGroup(groups, entry));
			groups.forEach(group -> group.setRootGroup(group));
			target.setRootGroups(groups);
		}
		sortEntryGroups(target);
	}
	*/

}
