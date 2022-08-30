package com.solgroup.core.interceptors.b2bunit;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;


import de.hybris.platform.b2b.jalo.B2BUnit;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import jersey.repackaged.com.google.common.collect.Sets;

public class SolGroupB2BUnitAddressesInterceptor implements PrepareInterceptor<B2BUnitModel> {



	private Logger LOG = Logger.getLogger(SolGroupB2BUnitAddressesInterceptor.class);
	
	
	@Override
	public void onPrepare(B2BUnitModel b2bUnit, InterceptorContext ctx) throws InterceptorException {
		
		if(CollectionUtils.isNotEmpty(b2bUnit.getAddresses())) {
			Collection<AddressModel> currentAddressCollection = b2bUnit.getAddresses();
			Set<AddressModel> newAddressSet = Sets.newHashSet();
			newAddressSet.addAll(currentAddressCollection);
			
			if(ctx.isModified(b2bUnit, B2BUnitModel.SHIPPINGADDRESS) && b2bUnit.getShippingAddress()!=null) {
				newAddressSet.add(b2bUnit.getShippingAddress());
			}
			
			if(ctx.isModified(b2bUnit, B2BUnit.BILLINGADDRESS) && b2bUnit.getBillingAddress()!=null) {
				newAddressSet.add(b2bUnit.getBillingAddress());
			}
			
			b2bUnit.setAddresses(newAddressSet);
		}
		
	}
	
	

}
