package com.solgroup.facades.populators;

import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.model.MaterialModel;

import de.hybris.platform.commercefacades.product.converters.populator.ProductBasicPopulator;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.product.ProductConfigurableChecker;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class SolGroupProductBasicPopulator<SOURCE extends ProductModel, TARGET extends ProductData> extends ProductBasicPopulator<SOURCE, TARGET>{
	
	
	private ProductConfigurableChecker productConfigurableChecker;
	
	@Resource(name="configurationService")
	ConfigurationService configurationService;
	
	@Override
	public void populate(final SOURCE productModel, final TARGET productData) throws ConversionException
	{
		
        super.populate(productModel, productData);
        productData.setStockable(BooleanUtils.isTrue(productModel.getStockable()));
		if (productModel.getUnit() != null)
		{
			productData.setUnit(productModel.getUnit().getCode());
		}
		
	}
	
	@Override
    protected ProductConfigurableChecker getProductConfigurableChecker()
	{
		return productConfigurableChecker;
	}

	@Override
    @Required
	public void setProductConfigurableChecker(final ProductConfigurableChecker productConfigurableChecker)
	{
		this.productConfigurableChecker = productConfigurableChecker;
	}

}
