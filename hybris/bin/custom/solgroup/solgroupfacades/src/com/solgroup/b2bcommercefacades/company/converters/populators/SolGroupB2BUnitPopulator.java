package com.solgroup.b2bcommercefacades.company.converters.populators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Sets;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.b2bcommercefacades.company.converters.populators.B2BUnitPopulator;
import de.hybris.platform.b2bcommercefacades.company.data.B2BUnitData;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.util.ServicesUtil;

public class SolGroupB2BUnitPopulator extends B2BUnitPopulator implements Populator<B2BUnitModel, B2BUnitData> {

//	private B2BUnitService<B2BUnitModel, B2BCustomerModel> b2BUnitService;
//
//	private Converter<AddressModel, AddressData> addressConverter;

	@Override
	public void populate(B2BUnitModel source, B2BUnitData target) throws ConversionException {
		ServicesUtil.validateParameterNotNull(source, "Parameter source cannot be null.");
		ServicesUtil.validateParameterNotNull(target, "Parameter target cannot be null.");
		
		super.populate(source, target);

		target.setBillingAddress(billingAddress(source));

		target.setShippingAddress(
				source.getShippingAddress() == null ? null : getAddressConverter().convert(source.getShippingAddress()));

		target.setVatID(source.getVatID());
		target.setTaxPayerCode(source.getTaxPayerCode());
		target.setErpCode(source.getErpCode());
		B2BUnitModel parent = (B2BUnitModel) this.getB2BUnitService().getParent(source);
		if (parent != null) {

			target.setUnit(this.convertUnit(parent));
		}
		this.populateChildUnits(source, target);
		

	}

	private AddressData billingAddress(CompanyModel source) {
		if (source.getBillingAddress() == null) {

			AddressData target = billingAddress(source.getResponsibleCompany());
			if (target != null) {
				target.setInheritedFromParentUnit(true);
			}
			return target;

		} else {
			if (source.getClass() == CompanyModel.class)
				return null;
			else {
				AddressData target = getAddressConverter().convert(source.getBillingAddress());
				target.setInheritedFromParentUnit(false);
				return target;
			}

		}
	}

	protected B2BUnitData populateChildUnits(B2BUnitModel source, B2BUnitData target) {
		ArrayList childUnitData = new ArrayList();
		Collection childUnitModels = CollectionUtils.select(source.getMembers(),
				PredicateUtils.instanceofPredicate(B2BUnitModel.class));
		Iterator arg5 = childUnitModels.iterator();
		while (arg5.hasNext()) {
			B2BUnitModel unit = (B2BUnitModel) arg5.next();

			childUnitData.add(this.convertUnit(unit));
		}
		target.setChildren(childUnitData);
		return target;
	}

	protected void populateUnit(B2BUnitModel source, B2BUnitData target) {
		target.setName(source.getLocName());
		target.setUid(source.getUid());
		target.setActive(Boolean.TRUE.equals(source.getActive()));
		target.setErpCode(source.getErpCode());
	}

	protected B2BUnitData convertUnit(B2BUnitModel source) {
		B2BUnitData target = new B2BUnitData();
		this.populateUnit(source, target);
		return target;
	}
	
	protected void populateCustomers(B2BUnitModel source, B2BUnitData target) {
		Collection b2BCustomers_org = this.getB2BUnitService().getUsersOfUserGroup(source, "customergroup", false);
		Collection b2BCustomers = this.getB2BUnitService().getUsersOfUserGroup(source, "b2bcustomergroup", false);
		
		Collection b2bCustomers_total = Sets.newHashSet();
		
		if (CollectionUtils.isNotEmpty(b2BCustomers_org)) {
			b2bCustomers_total.addAll(b2BCustomers_org);
		}
		if (CollectionUtils.isNotEmpty(b2BCustomers)) {
			b2bCustomers_total.addAll(b2BCustomers);
		}
		
		
		if (CollectionUtils.isNotEmpty(b2bCustomers_total)) {
			target.setCustomers(Converters.convertAll(b2bCustomers_total, getB2BCustomerConverter()));
		}

	}


//	protected Converter<AddressModel, AddressData> getAddressConverter() {
//		return addressConverter;
//	}
//
//	@Required
//	public void setAddressConverter(Converter<AddressModel, AddressData> addressConverter) {
//		this.addressConverter = addressConverter;
//	}
//
//	protected B2BUnitService<B2BUnitModel, B2BCustomerModel> getB2BUnitService() {
//		return b2BUnitService;
//	}
//
//	@Required
//	public void setB2BUnitService(B2BUnitService<B2BUnitModel, B2BCustomerModel> b2bUnitService) {
//		b2BUnitService = b2bUnitService;
//	}

}
