package com.solgroup.core.strategies.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.core.service.product.SolGroupProductService;

import de.hybris.platform.acceleratorservices.process.strategies.impl.DefaultSavedCartFileUploadStrategy;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.CommerceCartModificationStatus;
import de.hybris.platform.commerceservices.price.CommercePriceService;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.jalo.order.price.PriceInformation;

public class DefaultSolGroupSavedCartFileUploadStrategy extends DefaultSavedCartFileUploadStrategy {
	
	private SolGroupProductService solGroupProductService;
	private CMSSiteService cmsSiteService;
	private CommercePriceService commercePriceService;
	
	private Logger log = Logger.getLogger(DefaultSolGroupSavedCartFileUploadStrategy.class);
	
	@Override
	protected CommerceCartParameter createCommerceCartParam(String erpCode, long quantity, CartModel cartModel)
			throws CommerceCartModificationException {

		String code = getSolGroupProductService().getHybrisProductCode(erpCode, getCmsSiteService().getCurrentSite());
		return super.createCommerceCartParam(code, quantity, cartModel);
	}
	
	@Override
	protected CommerceCartModification addLinesToCart(final String[] cartAttributes, final CartModel cartModel)
			throws CommerceCartModificationException
	{
		CMSSiteModel cmsSite = getCmsSiteService().getCurrentSite();
		// Get erpCode
		final String productErpCode = StringUtils.trim(cartAttributes[getProductCodeIndex().intValue() - 1]);
		log.debug("Product erpCode = " + productErpCode);
		
		// Get hybris productCode
		String productHybrisCode = getSolGroupProductService().getHybrisProductCode(productErpCode, cmsSite);
		log.debug("Product hybris code = " + productHybrisCode);

		// Check product
		boolean checkProductResult = checkProduct(productHybrisCode);
		if(!checkProductResult) {
			final CommerceCartModification commerceCartModification = new CommerceCartModification();
			commerceCartModification.setStatusCode(CommerceCartModificationStatus.UNAVAILABLE);
			return commerceCartModification;
		}
		
		
		final Long qty = Long.valueOf(StringUtils.trim(cartAttributes[getQtyIndex().intValue() - 1]));
		final CommerceCartParameter commerceCartParameter = createCommerceCartParam(productErpCode, qty.longValue(), cartModel);
		return getCommerceCartService().addToCart(commerceCartParameter);
	}
	
	
	private boolean checkProduct(String productCode) {
		try {
			ProductModel product = getSolGroupProductService().getProductForCode(productCode);
			
			if(product==null) {
				log.warn("No product found for code " + productCode);
				return false;
			}
			
			// Check product price
			PriceInformation priceInformation = getCommercePriceService().getWebPriceForProduct(product);
			if(priceInformation==null) {
				log.warn("No price found for product " + productCode);
				return false;
			}
			
			
		} catch(Exception exc) {
			log.warn("Exception : No product found for code " + productCode);
			return false;
		}
		
		return true;
	}

	protected SolGroupProductService getSolGroupProductService() {
		return solGroupProductService;
	}

	@Required
	public void setSolGroupProductService(SolGroupProductService solGroupProductService) {
		this.solGroupProductService = solGroupProductService;
	}

	protected CMSSiteService getCmsSiteService() {
		return cmsSiteService;
	}

	@Required
	public void setCmsSiteService(CMSSiteService cmsSiteService) {
		this.cmsSiteService = cmsSiteService;
	}

	protected CommercePriceService getCommercePriceService() {
		return commercePriceService;
	}

	@Required
	public void setCommercePriceService(CommercePriceService commercePriceService) {
		this.commercePriceService = commercePriceService;
	}
	
	
	

	
	

}
