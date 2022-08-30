package com.solgroup.core.interceptors.product;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.catalog.model.ProductFeatureModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.model.ModelService;

public class SolgroupProductFeaturePrepareInterceptor implements PrepareInterceptor<ProductFeatureModel> {
	
	private ModelService modelService;
	

	@Override
	public void onPrepare(ProductFeatureModel productFeature, InterceptorContext ctx) throws InterceptorException {
		
		ProductModel product = productFeature.getProduct();
		// Manage only staged product
		if (BooleanUtils.isTrue(product.getCatalogVersion().getActive())) {
			return;
		}
		
		if(ctx.isNew(productFeature)) {
			product.setApprovalStatus(ArticleApprovalStatus.CHECK);
			getModelService().save(product);
		}
		
		
	}


	protected ModelService getModelService() {
		return modelService;
	}

	@Required
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	
	
	
}
