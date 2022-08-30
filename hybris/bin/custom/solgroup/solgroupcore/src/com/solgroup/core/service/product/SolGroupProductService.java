package com.solgroup.core.service.product;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.variants.model.GenericVariantProductModel;

import java.util.List;
import java.util.Set;

public interface SolGroupProductService extends ProductService{
	
	ProductModel getProductForErpCode(final String erpCode);

	List<GenericVariantProductModel> getAllProductVariantsForDateRangeAndApprovalStatusAndCatalogVersion(int daysToCheck, ArticleApprovalStatus approvalStatus, CatalogVersionModel catalogVersion);

	List<ProductModel> getAllBaseProductForDateRangeAndApprovalStatusAndCatalogVersion(int daysToCheck, ArticleApprovalStatus approvalStatus, CatalogVersionModel catalogVersion);
	
	ProductModel getProductForErpCodeAndCatalogVersion(final String erpCode, CatalogVersionModel catalogVersion);
	
	Set<String> findCategoriesForProductByMaster(String productCode, String masterSystemCode, CatalogVersionModel catalogVersion);

	Set<CategoryModel> findCategoriesObjectForProductByMaster(String productCode, String masterSystemCode, CatalogVersionModel catalogVersion);

	String getHybrisProductCode(String productErpCpde, BaseSiteModel site);
	
	
}
