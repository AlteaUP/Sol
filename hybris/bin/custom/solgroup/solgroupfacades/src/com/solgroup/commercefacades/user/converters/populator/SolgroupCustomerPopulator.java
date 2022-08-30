package com.solgroup.commercefacades.user.converters.populator;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;

import de.hybris.platform.b2b.model.B2BCostCenterModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2bcommercefacades.company.converters.populators.B2BCustomerPopulator;
import de.hybris.platform.b2bcommercefacades.company.data.B2BCostCenterData;
import de.hybris.platform.b2bcommercefacades.company.data.B2BUnitData;
import de.hybris.platform.commercefacades.user.data.CustomerData;

public class SolgroupCustomerPopulator extends B2BCustomerPopulator {

	@Override
    protected void populateUnit(B2BCustomerModel customer, CustomerData target) {
        B2BUnitModel parent = this.getB2bUnitService().getParent(customer);
        if (parent != null) {
            B2BUnitData b2BUnitData = new B2BUnitData();
            b2BUnitData.setUid(parent.getUid());
            b2BUnitData.setName(parent.getLocName());
            b2BUnitData.setActive(Boolean.TRUE.equals(parent.getActive()));
            // FIX SOL528
            b2BUnitData.setErpCode(parent.getErpCode());
            if (CollectionUtils.isNotEmpty(parent.getCostCenters())) {
                ArrayList<B2BCostCenterData> costCenterDataCollection = new ArrayList<B2BCostCenterData>();
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

}
