package com.solgroup.b2bcommercefacades.company.converters.populators;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.b2b.model.B2BCostCenterModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.b2bcommercefacades.company.data.B2BCostCenterData;
import de.hybris.platform.b2bcommercefacades.company.data.B2BUnitData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.util.ServicesUtil;

public class SolgroupB2BCustomerPopulator implements Populator<CustomerModel, CustomerData> {
	private B2BUnitService<B2BUnitModel, UserModel> b2bUnitService;

	@Override
	public void populate(CustomerModel source, CustomerData target) throws ConversionException {
		ServicesUtil.validateParameterNotNull(source, "Parameter source cannot be null.");
		ServicesUtil.validateParameterNotNull(target, "Parameter target cannot be null.");
		
		B2BCustomerModel customer = (B2BCustomerModel)source;
		
		this.populateUnit(customer, target);
	}

	protected void populateUnit(B2BCustomerModel customer, CustomerData target) {
        B2BUnitModel parent = this.getB2bUnitService().getParent(customer);
        if (parent != null) {

			B2BUnitData b2BUnitData = new B2BUnitData();
			b2BUnitData.setUid(parent.getUid());
			b2BUnitData.setName(parent.getLocName());
			b2BUnitData.setActive(Boolean.TRUE.equals(parent.getActive()));
            b2BUnitData.setErpCode(parent.getErpCode());

            if (CollectionUtils.isNotEmpty(parent.getCostCenters())) {

				ArrayList costCenterDataCollection = new ArrayList();
				Iterator arg6 = parent.getCostCenters().iterator();
				while (arg6.hasNext()) {
					B2BCostCenterModel costCenterModel = (B2BCostCenterModel) arg6.next();
					B2BCostCenterData costCenterData = new B2BCostCenterData();
					costCenterData.setCode(costCenterModel.getCode());
					costCenterData.setName(costCenterModel.getName());
					costCenterDataCollection.add(costCenterData);
				}
				b2BUnitData.setCostCenters(costCenterDataCollection);
			}
			target.setUnit(b2BUnitData);
		}
	}

	protected B2BUnitService<B2BUnitModel, UserModel> getB2bUnitService() {
		return b2bUnitService;
	}

	@Required
	public void setB2bUnitService(B2BUnitService<B2BUnitModel, UserModel> b2bUnitService) {
		this.b2bUnitService = b2bUnitService;
	}
}
