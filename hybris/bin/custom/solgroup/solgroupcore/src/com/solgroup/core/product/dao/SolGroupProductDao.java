package com.solgroup.core.product.dao;

import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.daos.ProductDao;
import de.hybris.platform.variants.model.GenericVariantProductModel;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

public interface SolGroupProductDao extends ProductDao{
	
	
	List<ProductModel> findProductsByErpCode(final String erpCode);
	
	List<ProductModel> findProductsByErpCodeAndCatalogVersion(final String erpCode, final CatalogVersionModel catalogVersion);
	
	Set<String> findCategoriesForProductByMaster(String productCode, String masterSystemCode, CatalogVersionModel catalogVersion);

	Set<CategoryModel> findCategoriesObjectForProductByMaster(String productCode, String masterSystemCode, CatalogVersionModel catalogVersion);

	List<GenericVariantProductModel> getAllProductVariantsForCreationTimeAndApprovalStatusAndCatalogVersion(Calendar crationTime, ArticleApprovalStatus approvalStatus, CatalogVersionModel catalogVersion);

	List<ProductModel> getAllBaseProductForCreationTimeAndApprovalStatusAndCatalogVersion(Calendar crationTime, ArticleApprovalStatus approvalStatus, CatalogVersionModel catalogVersion);

}
