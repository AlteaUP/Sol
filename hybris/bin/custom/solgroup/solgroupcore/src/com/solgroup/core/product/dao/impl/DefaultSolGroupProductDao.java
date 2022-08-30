package com.solgroup.core.product.dao.impl;

import com.solgroup.core.product.dao.SolGroupProductDao;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.daos.impl.DefaultProductDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.variants.model.GenericVariantProductModel;
import jersey.repackaged.com.google.common.collect.Maps;
import jersey.repackaged.com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

public class DefaultSolGroupProductDao extends DefaultProductDao implements SolGroupProductDao{

	public DefaultSolGroupProductDao(String typecode) {
		super(typecode);
	}
	
	@Override
	public List<ProductModel> findProductsByErpCode(final String erpCode)
	{
		validateParameterNotNull(erpCode, "Product code must not be null!");

		return find(Collections.singletonMap(ProductModel.ERPCODE, (Object) erpCode));
	}

	@Override
	public List<ProductModel> findProductsByErpCodeAndCatalogVersion(String erpCode,
			CatalogVersionModel catalogVersion) {

		validateParameterNotNull(erpCode, "Product code must not be null!");
		validateParameterNotNull(catalogVersion, "CatalogVersion must not be null!");

		Map<String,Object> params = Maps.newHashMap();
		params.put(ProductModel.ERPCODE, (Object) erpCode);
		params.put(ProductModel.CATALOGVERSION, (Object) catalogVersion);
		
		return find(params);
	
	}
	
	@Override
	public Set<String> findCategoriesForProductByMaster(String productCode, String masterSystemCode, CatalogVersionModel catalogVersion) {
		
		Set<String> result = Sets.newHashSet();
		
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select {cat.code} from {");
		queryBuilder.append("CategoryProductRelation as cpr");
		queryBuilder.append(" join Category as cat on {cat.pk}={cpr.source}");
		queryBuilder.append(" join Product as p on {p.pk}={cpr.target}");
		queryBuilder.append(" join MasterSystemEnum as ms on {ms.pk}={cat.masterOn}");
		queryBuilder.append(" join CatalogVersion as cv on {cv.pk}={cat.catalogVersion}");
		queryBuilder.append("}");
		queryBuilder.append(" where ");
		queryBuilder.append("{ms.code}=?masterSystem");
		queryBuilder.append(" and {p.code}=?productCode");
		queryBuilder.append(" and {cv.pk}=?catalogVersion");
		
		
		Map<String,Object> params = Maps.newHashMap();
		params.put("masterSystem",masterSystemCode);
		params.put("productCode",productCode);
		params.put("catalogVersion", catalogVersion);
		
		
        FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(queryBuilder.toString(), params);
        flexibleSearchQuery.setResultClassList(Arrays.asList(String.class));
        
        SearchResult searchResult = getFlexibleSearchService().search(flexibleSearchQuery);

        if (searchResult != null && searchResult.getResult() != null) {
            for (int i = 0; i < searchResult.getResult().size(); i++) {
                Object resultRow = searchResult.getResult().get(i);
                if (resultRow instanceof String) {
                    if(StringUtils.isNotEmpty(resultRow.toString())) {
                    	result.add(resultRow.toString());
                    }
                }
            }
        }

		return result;
	}

	@Override
	public Set<CategoryModel> findCategoriesObjectForProductByMaster(String productCode, String masterSystemCode, CatalogVersionModel catalogVersion) {

		Set<CategoryModel> result = new HashSet<>();

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select {cat.pk} from {");
		queryBuilder.append("CategoryProductRelation as cpr");
		queryBuilder.append(" join Category as cat on {cat.pk}={cpr.source}");
		queryBuilder.append(" join Product as p on {p.pk}={cpr.target}");
		queryBuilder.append(" join MasterSystemEnum as ms on {ms.pk}={cat.masterOn}");
		queryBuilder.append(" join CatalogVersion as cv on {cv.pk}={cat.catalogVersion}");
		queryBuilder.append("}");
		queryBuilder.append(" where ");
		queryBuilder.append("{ms.code}=?masterSystem");
		queryBuilder.append(" and {p.code}=?productCode");
		queryBuilder.append(" and {cv.pk}=?catalogVersion");


		Map<String,Object> params = Maps.newHashMap();
		params.put("masterSystem",masterSystemCode);
		params.put("productCode",productCode);
		params.put("catalogVersion", catalogVersion);


		FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(queryBuilder.toString(), params);

		SearchResult searchResult = getFlexibleSearchService().<CategoryModel>search(flexibleSearchQuery);

		if (searchResult != null && searchResult.getResult() != null) {
			List<CategoryModel> categories = searchResult.getResult();
			if(categories != null && !categories.isEmpty()) {
				result = new HashSet<>(categories);
			}
		}

		return result;
	}

	@Override
	public List<GenericVariantProductModel> getAllProductVariantsForCreationTimeAndApprovalStatusAndCatalogVersion(Calendar crationTime, ArticleApprovalStatus approvalStatus, CatalogVersionModel catalogVersion) {
		//Recupero tutte le varianti prodotto con stato check entro il range di giorni
		String queryVariants = "SELECT {" + ProductModel.PK + "} FROM {" + GenericVariantProductModel._TYPECODE  +"} " +
				"WHERE {" + ProductModel.CREATIONTIME + "} > ?dateToCheck AND {" + ProductModel.APPROVALSTATUS + "} = ?status " +
				"AND {" + ProductModel.CATALOGVERSION + "} = ?catalogVersion";

		Map params = new HashMap<>();
		params.put("dateToCheck", crationTime.getTime());
		params.put("status", approvalStatus);
		params.put("catalogVersion", catalogVersion);
		FlexibleSearchQuery fsq = new FlexibleSearchQuery(queryVariants, params);

		return getFlexibleSearchService().<GenericVariantProductModel>search(fsq).getResult();
	}

	@Override
	public List<ProductModel> getAllBaseProductForCreationTimeAndApprovalStatusAndCatalogVersion(Calendar crationTime, ArticleApprovalStatus approvalStatus, CatalogVersionModel catalogVersion) {

		//Recupero tutti i prodotti padre con stato check entro il range di giorni
		String queryProducts = "SELECT {" + ProductModel.PK + "} FROM {" + ProductModel._TYPECODE  +"} " +
				"WHERE  {" + ProductModel.PK + "} NOT IN " +
				"( {{SELECT {" + ProductModel.PK + "} FROM {" + GenericVariantProductModel._TYPECODE  +"} " +
				"WHERE {" + ProductModel.CATALOGVERSION + "} = ?innerCatalogVersion }} ) " +
				"AND {" + ProductModel.CREATIONTIME + "} > ?dateToCheck " +
				"AND {" + ProductModel.APPROVALSTATUS + "} = ?status " +
				"AND {" + ProductModel.CATALOGVERSION + "} = ?outerCatalogVersion";

		Map params = new HashMap<>();
		params.put("innerCatalogVersion", catalogVersion);
		params.put("dateToCheck", crationTime.getTime());
		params.put("status", approvalStatus);
		params.put("outerCatalogVersion", catalogVersion);

		FlexibleSearchQuery fsq = new FlexibleSearchQuery(queryProducts, params);
		return  getFlexibleSearchService().<ProductModel>search(fsq).getResult();
	}


}
