package com.solgroup.core.ticket.converter.populator;

import de.hybris.platform.ticket.converters.populator.CsTicketPopulator;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticketsystem.data.CsTicketParameter;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class DefaultSolGroupCsTicketPopulator<SOURCE extends CsTicketParameter, TARGET extends CsTicketModel> extends CsTicketPopulator<SOURCE,TARGET> {
	
	@Override
	public void populate(SOURCE source, TARGET target) throws ConversionException {
		
		if(source.getB2bUnit()!=null) {
			target.setB2bUnit(source.getB2bUnit());
		}
		
		super.populate(source, target);
	}


}
