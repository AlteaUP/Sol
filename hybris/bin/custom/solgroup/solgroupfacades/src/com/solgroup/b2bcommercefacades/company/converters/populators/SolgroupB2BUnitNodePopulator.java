package com.solgroup.b2bcommercefacades.company.converters.populators;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2bcommercefacades.company.data.B2BUnitNodeData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.util.ServicesUtil;

public class SolgroupB2BUnitNodePopulator implements Populator<B2BUnitModel, B2BUnitNodeData> {

	@Override
	public void populate(B2BUnitModel source, B2BUnitNodeData target) throws ConversionException {

		ServicesUtil.validateParameterNotNull(source, "Parameter source cannot be null.");
		ServicesUtil.validateParameterNotNull(target, "Parameter target cannot be null.");
		
		target.setErpCode(source.getErpCode());
	}


}
