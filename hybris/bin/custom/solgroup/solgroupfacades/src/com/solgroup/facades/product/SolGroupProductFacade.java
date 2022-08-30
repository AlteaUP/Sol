package com.solgroup.facades.product;

import java.util.Collection;

import de.hybris.platform.commercefacades.product.ProductFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;

public interface SolGroupProductFacade extends ProductFacade{
	
	public ProductData getProductForErpCodeAndOptions(final String erpCode, final Collection<ProductOption> options);

}
