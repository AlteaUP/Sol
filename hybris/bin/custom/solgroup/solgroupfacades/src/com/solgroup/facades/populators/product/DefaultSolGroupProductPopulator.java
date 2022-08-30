package com.solgroup.facades.populators.product;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import de.hybris.platform.commercefacades.product.converters.populator.ProductPopulator;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;

public class DefaultSolGroupProductPopulator extends ProductPopulator {

    private ConfigurationService configurationService;

	@Override
    public void populate(final ProductModel source, final ProductData target) {
        final String materialsString = configurationService.getConfiguration().getString("quantity.code.materials");

//		super.populate(source, target);
		Assert.notNull(source, "Parameter source cannot be null.");
		Assert.notNull(target, "Parameter target cannot be null.");

		target.setCode(source.getCode());
		target.setName(source.getName());
		target.setUrl(getProductModelUrlResolver().resolve(source));
		target.setErpCode(source.getErpCode());
        target.setRefill(BooleanUtils.isTrue(source.getRefill()));

        if (BooleanUtils.isTrue(source.getRefill()) && materialsString.equals(getProductMaterial(source))) {
            target.setShowQuantitySelector(false);
        } else {
            target.setShowQuantitySelector(true);
        }

		getProductBasicPopulator().populate(source, target);
		getVariantSelectedPopulator().populate(source, target);
		getProductPrimaryImagePopulator().populate(source, target);
	}

    private String getProductMaterial(ProductModel product) {
        String productMaterial = null;
        if (product.getMaterial() != null && product.getMaterial().getCode() != null) {
            productMaterial = product.getMaterial().getCode();
        }
        return productMaterial;
    }

    protected ConfigurationService getConfigurationService() {
        return configurationService;
    }

    @Required
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

	
}
