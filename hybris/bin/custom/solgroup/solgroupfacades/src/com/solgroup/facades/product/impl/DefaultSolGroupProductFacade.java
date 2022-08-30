package com.solgroup.facades.product.impl;

import java.util.Collection;

import javax.annotation.Resource;

import com.solgroup.core.service.product.SolGroupProductService;
import com.solgroup.facades.product.SolGroupProductFacade;


import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.product.impl.DefaultProductFacade;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

public class DefaultSolGroupProductFacade<REF_TARGET> extends DefaultProductFacade implements SolGroupProductFacade{

	@Resource(name="commonI18NService")
	private CommonI18NService commonI18NService;
	
	@Resource(name="solGroupProductService")
	private SolGroupProductService solGroupProductService;
	
	
	@Override
	public ProductData getProductForErpCodeAndOptions(final String erpCode, final Collection<ProductOption> options)
	{
		final ProductModel productModel = solGroupProductService.getProductForErpCode(erpCode);

		return getProductForOptions(productModel, options);
	}
}
