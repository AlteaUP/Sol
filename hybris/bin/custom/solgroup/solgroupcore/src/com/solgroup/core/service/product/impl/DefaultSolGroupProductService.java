package com.solgroup.core.service.product.impl;

import com.solgroup.core.product.dao.SolGroupProductDao;
import com.solgroup.core.service.product.SolGroupProductService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.impl.DefaultProductService;
import de.hybris.platform.variants.model.GenericVariantProductModel;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateIfSingleResult;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.lang.String.format;


public class DefaultSolGroupProductService extends DefaultProductService implements SolGroupProductService{

	
	private SolGroupProductDao solGroupProductDao;
	private CMSSiteService cmsSiteService;
	
	private static final Logger log = Logger.getLogger(DefaultSolGroupProductService.class);
	
	@Override
	public ProductModel getProductForErpCode(final String code)
	{
		validateParameterNotNull(code, "Parameter code must not be null");
		
		CMSSiteModel currentSite = getCmsSiteService().getCurrentSite();
		//final List<ProductModel> products = getSolGroupProductDao().findProductsByErpCode(code);
		final List<ProductModel> products = getSolGroupProductDao().findProductsByCode(getHybrisProductCode(code, currentSite));

		validateIfSingleResult(products, format("Product with code '%s' not found!", code),
				format("Product code '%s' is not unique, %d products found!", code, Integer.valueOf(products.size())));

		return products.get(0);
	}

	@Override
	public List<GenericVariantProductModel> getAllProductVariantsForDateRangeAndApprovalStatusAndCatalogVersion(int daysToCheck, ArticleApprovalStatus approvalStatus, CatalogVersionModel catalogVersion) {

		final Calendar creationDate = Calendar.getInstance();
		creationDate.set(Calendar.HOUR_OF_DAY, 0);
		creationDate.set(Calendar.MINUTE, 0);
		creationDate.set(Calendar.SECOND, 0);
		creationDate.set(Calendar.MILLISECOND, 0);
		//Sottraggo il numero di giorni da controllare
		creationDate.add(Calendar.DAY_OF_MONTH, -daysToCheck);

		return getSolGroupProductDao().getAllProductVariantsForCreationTimeAndApprovalStatusAndCatalogVersion(creationDate, approvalStatus, catalogVersion);
	}

	@Override
	public List<ProductModel> getAllBaseProductForDateRangeAndApprovalStatusAndCatalogVersion(int daysToCheck, ArticleApprovalStatus approvalStatus, CatalogVersionModel catalogVersion) {

		final Calendar creationDate = Calendar.getInstance();
		creationDate.set(Calendar.HOUR_OF_DAY, 0);
		creationDate.set(Calendar.MINUTE, 0);
		creationDate.set(Calendar.SECOND, 0);
		creationDate.set(Calendar.MILLISECOND, 0);
		//Sottraggo il numero di giorni da controllare
		creationDate.add(Calendar.DAY_OF_MONTH, -daysToCheck);

		return getSolGroupProductDao().getAllBaseProductForCreationTimeAndApprovalStatusAndCatalogVersion(creationDate, approvalStatus, catalogVersion);
	}

	@Override
	public ProductModel getProductForErpCodeAndCatalogVersion(String erpCode, CatalogVersionModel catalogVersion) {
		validateParameterNotNull(erpCode, "Parameter code must not be null");
		validateParameterNotNull(catalogVersion, "Parameter catalogVersion must not be null");
		
		CMSSiteModel currentSite = getCmsSiteService().getCurrentSite();
		
		//final List<ProductModel> products = getSolGroupProductDao().findProductsByErpCodeAndCatalogVersion(erpCode, catalogVersion);
		
		final List<ProductModel> products = getSolGroupProductDao().findProductsByCode(catalogVersion, getHybrisProductCode(erpCode, currentSite));

		validateIfSingleResult(products, format("Product with code '%s' and catalogVersion %s not found!", erpCode, catalogVersion.getCatalog().getId()+":"+catalogVersion.getVersion()),
				format("Product code '%s' is not unique, %d products found!", erpCode, Integer.valueOf(products.size())));
		
		return products.get(0);
	}
	
	@Override
	public Set<String> findCategoriesForProductByMaster(String productCode, String masterSystemCode, CatalogVersionModel catalogVersion) {
		return getSolGroupProductDao().findCategoriesForProductByMaster(productCode, masterSystemCode, catalogVersion);
	}

	@Override
	public Set<CategoryModel> findCategoriesObjectForProductByMaster(String productCode, String masterSystemCode, CatalogVersionModel catalogVersion) {
		return getSolGroupProductDao().findCategoriesObjectForProductByMaster(productCode, masterSystemCode, catalogVersion);
	}

	@Override
	public String getHybrisProductCode(String productErpCpde, BaseSiteModel site) {

		if(StringUtils.isEmpty(productErpCpde)) {
			log.error("Category code is empty");
			return "";
		}
		
		if(site == null) {
			log.error("CMSSite is null");
			return "";
		}

		return site.getUid() + "_" + productErpCpde;
		
	}
	
	protected SolGroupProductDao getSolGroupProductDao() {
		return solGroupProductDao;
	}

	@Required
	public void setSolGroupProductDao(SolGroupProductDao solGroupProductDao) {
		this.solGroupProductDao = solGroupProductDao;
	}

	protected CMSSiteService getCmsSiteService() {
		return cmsSiteService;
	}

	@Required
	public void setCmsSiteService(CMSSiteService cmsSiteService) {
		this.cmsSiteService = cmsSiteService;
	}




	
	
	
	

}
