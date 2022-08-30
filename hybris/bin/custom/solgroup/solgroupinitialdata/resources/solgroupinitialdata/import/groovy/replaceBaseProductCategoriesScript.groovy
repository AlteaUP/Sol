import de.hybris.platform.core.Registry;
import de.hybris.platform.search.restriction.SearchRestrictionService
import de.hybris.platform.servicelayer.search.FlexibleSearchService
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel
import com.solgroup.core.common.daos.CommonDao
import com.solgroup.core.ws.service.CommonWsService
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService
import com.google.common.collect.Lists;
import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.dao.tax.SolGroupTaxRowDao;
import com.solgroup.core.enums.MasterSystemEnum;
import com.solgroup.core.enums.ProductStatusEnum
import com.solgroup.core.service.categories.SolGroupCategoryService;
import com.solgroup.core.service.product.SolGroupProductService;
import com.solgroup.core.ws.services.product.xml.ProductsImportHybris;
import com.solgroup.core.ws.services.product.xml.ProductsImportHybris.BaseProducts;
import com.solgroup.core.ws.services.product.xml.ProductsImportHybris.BaseProducts.BaseProduct;
import com.solgroup.core.ws.services.product.xml.ProductsImportHybris.BaseProducts.BaseProduct.CommercialCategories.CommercialCategory;
import com.solgroup.core.ws.services.product.xml.ProductsImportHybris.BaseProducts.BaseProduct.Names.Name;
import com.solgroup.core.ws.services.product.xml.ProductsImportHybris.BaseProducts.BaseProduct.Variants.Variant;
import com.solgroup.core.ws.services.product.xml.ProductsImportHybris.BaseProducts.BaseProduct.Variants.Variant.VariantsCategories.VariantCategory
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.product.ProductModel
import de.hybris.platform.europe1.enums.ProductTaxGroup;
import de.hybris.platform.europe1.model.TaxRowModel;
import de.hybris.platform.product.daos.ProductDao;
import de.hybris.platform.servicelayer.config.ConfigurationService
import de.hybris.platform.variants.model.GenericVariantProductModel;
import de.hybris.platform.variants.model.VariantCategoryModel;
import de.hybris.platform.variants.model.VariantTypeModel;
import de.hybris.platform.variants.model.VariantValueCategoryModel;
import jersey.repackaged.com.google.common.collect.Maps;
import jersey.repackaged.com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger

ReplaceBaseProductCategories.main();

public class ReplaceBaseProductCategories{


    // ######################################################################################################
    public static final String PRODUCT_STATUS_ENABLED_CODE = "1";
    public static final String default_um = "pieces";
    public static final String visibilityCategory_suffix = "_visibility";
    public static final String IMPEX_SEPARATOR = ";";
    public static final String IMPEX_VALUES_SEPARATOR = ",";
    public static final String STRING_RETURN = "\n";
    public static final String impexHeader_baseProduct = "INSERT_UPDATE Product;	code[unique=true];	erpCode;	sourceSystem(code);	legacyName[lang=en];legacyName[lang=it];legacyName[lang=de];legacyName[lang=fr];	material(code);	stockable[default=true];	weight[default=0];	unit(code);	status(code);	europe1PriceFactory_PTG(code);	variantType;	approvalStatus(code)[default=check];	\$catalogVersion;";
    public static final String impexHeader_resetCategories = "UPDATE Product; code[unique=true];	supercategories[mode=replace];	\$catalogVersion;";
    public static final String impexHeader_replaceCategories= "INSERT_UPDATE Product; code[unique=true];	supercategories(code,\$catalogVersion)[mode=replace];	\$catalogVersion;";
    public static final String impexHeader_baseProductCategories = "INSERT_UPDATE CategoryProductRelation; source(code,\$catalogVersion)[unique=true]; target(code,\$catalogVersion)[unique=true];";
    public static final String impexHeader_genericVariantProduct = "INSERT_UPDATE GenericVariantProduct;	code[unique=true];	erpCode;	sourceSystem(code);	legacyName[lang=en];legacyName[lang=it];legacyName[lang=de];legacyName[lang=fr];	weight[default=0];	unit(code);	europe1PriceFactory_PTG(code);	material(code);	status(code);	stockable[default=true]; baseProduct(code,\$catalogVersion);	supercategories(code,\$catalogVersion)[mode=append];	approvalStatus(code)[default=check];	\$catalogVersion;";
    public static final String impexHeader_categoryRemove = "UPDATE Product; code[unique=true];	supercategories(code,\$catalogVersion)[mode=remove];	\$catalogVersion;";
    public static final String userUid = "admin";
    public static final String siteUid = "solgroupIT";
    public static final String xmlMediaCode = "solgroupIT_RAMSES_IT_products.xml";
    public static final String sourceSystemCode = "RAMSES_IT";
    // ######################################################################################################


    // ######################################################################################################
    public static final ProductDao productDao = (ProductDao) Registry.getApplicationContext().getBean("productDao");
    public static final SolGroupCategoryService solGroupCategoryService = (SolGroupCategoryService) Registry.getApplicationContext().getBean("solGroupCategoryService");
    public static final SolGroupTaxRowDao solGroupTaxRowDao = (SolGroupTaxRowDao) Registry.getApplicationContext().getBean("solGroupTaxRowDao");
    public static final SolGroupProductService solGroupProductService = (SolGroupProductService) Registry.getApplicationContext().getBean("solGroupProductService");
    public static final String IMPORT_PRODUCT_JOB_REPLACE_MODE = "com.solgroup.job.importData.jobs.replaceMode";
    public static final String IMPORT_PRODUCT_JOB_ONLY_PRODUCE_CATEGORY_RESET_IMPEX = "com.solgroup.job.importData.jobs.onlyProduceCategoryResetImpex";
    public static final ConfigurationService configurationService = (ConfigurationService) Registry.getApplicationContext().getBean("configurationService");
    public static final ModelService modelService = (ModelService) Registry.getApplicationContext().getBean("modelService");
    public static final FlexibleSearchService flexibleSearchService = (FlexibleSearchService) Registry.getApplicationContext()
                        .getBean("flexibleSearchService");
    public static final BaseSiteService baseSiteService = (BaseSiteService) Registry.getApplicationContext()
                        .getBean("baseSiteService");
    public static final SearchRestrictionService searchRestrictionService = (SearchRestrictionService) Registry
                        .getApplicationContext().getBean("searchRestrictionService");
    public static final UserService userService = (UserService) Registry.getApplicationContext().getBean("userService");
    public static final CommonWsService commonWsService = (CommonWsService) Registry.getApplicationContext().getBean("commonWsService");
    public static final MediaService mediaService = (MediaService) Registry.getApplicationContext().getBean("mediaService");
    public static final CommonDao commonDao = (CommonDao) Registry.getApplicationContext().getBean("commonDao");
    // ######################################################################################################


    static final Logger LOG = Logger.getLogger(ReplaceBaseProductCategories.class);
    static boolean replaceMode = true;

    public static void main(String[] args) {
        System.out.println("Start importProduct job. User is " + userUid);
        System.out.println("getReplaceMode: " + replaceMode);
        perform();
    }

    public static void perform() {
        userService.setCurrentUser(userService.getUserForUID(userUid));
        searchRestrictionService.enableSearchRestrictions();
        final BaseSiteModel site = baseSiteService.getBaseSiteForUID(siteUid);
        if (site == null) {
            LOG.error("No cmsSite found!");
            return;
        }

        final CatalogVersionModel catalogVersion = site.getImportProductCatalog();

        final InputStream mediaStream = getMediaData(xmlMediaCode);
        final ProductsImportHybris importData = (ProductsImportHybris) commonWsService
                .parseXmlStream(ProductsImportHybris.class, mediaStream);


        StringBuilder importImpex = new StringBuilder();
        StringBuilder impexMacro_stringBuilder = new StringBuilder();
        impexMacro_stringBuilder.append("\$solPrefix=" + site.getUid());
        impexMacro_stringBuilder.append(STRING_RETURN);
        impexMacro_stringBuilder.append("\$productCatalog=\$solPrefixProductCatalog");
        impexMacro_stringBuilder.append(STRING_RETURN);
        impexMacro_stringBuilder.append("\$catalogVersion=catalogversion(catalog(id[default=\$productCatalog]),version[default='Staged'])[unique=true,default=\$productCatalog:Staged]");
        impexMacro_stringBuilder.append(STRING_RETURN);

        StringBuilder impexReplaceCategoryHeader_stringBuilder = new StringBuilder();
        impexReplaceCategoryHeader_stringBuilder.append(impexHeader_replaceCategories);
        impexReplaceCategoryHeader_stringBuilder.append(STRING_RETURN);

        StringBuilder impexBaseProduct_stringBuilder = new StringBuilder();
        impexBaseProduct_stringBuilder.append(impexHeader_baseProduct);
        impexBaseProduct_stringBuilder.append(STRING_RETURN);

        StringBuilder impexGenericVariantProdict_stringBuilder = new StringBuilder();
        impexGenericVariantProdict_stringBuilder.append(impexHeader_genericVariantProduct);
        impexGenericVariantProdict_stringBuilder.append(STRING_RETURN);

        StringBuilder categoryProductRelation_stringBuilder = new StringBuilder();
        if (!replaceMode) {
            categoryProductRelation_stringBuilder.append(impexHeader_baseProductCategories);
            categoryProductRelation_stringBuilder.append(STRING_RETURN);
        }

        StringBuilder removeCategory_stringBuilder = new StringBuilder();
        removeCategory_stringBuilder.append(impexHeader_categoryRemove);
        removeCategory_stringBuilder.append(STRING_RETURN);

        Set<String> existingProductsCode = new HashSet<>();
        final BaseProducts baseProducts = importData.getBaseProducts();
        //Iterate over all baseProduct to retrieve existing products on the system
        for (final BaseProduct baseProduct : baseProducts.getBaseProduct()) {
            getExistingProductsCode(existingProductsCode, productDao.findProductsByCode(catalogVersion, solGroupProductService.getHybrisProductCode(baseProduct.getErpCode(), site)));

            if (baseProduct.getVariants() != null && CollectionUtils.isNotEmpty(baseProduct.getVariants().getVariant())) {
                for (final Variant variant : baseProduct.getVariants().getVariant()) {
                    getExistingProductsCode(existingProductsCode, productDao.findProductsByCode(catalogVersion, solGroupProductService.getHybrisProductCode(variant.getErpCode(), site)));
                }
            }
        }


        // Iterate over all baseProducts sent in message
        for (final BaseProduct baseProduct : baseProducts.getBaseProduct()) {

            // Create baseProduct impex row
            createBaseProductImpexRow(baseProduct, catalogVersion, sourceSystemCode, site, impexBaseProduct_stringBuilder, categoryProductRelation_stringBuilder, removeCategory_stringBuilder, existingProductsCode);

            // Iterate over all variants for current baseProduct
            if (baseProduct.getVariants() != null && CollectionUtils.isNotEmpty(baseProduct.getVariants().getVariant())) {
                for (final Variant variant : baseProduct.getVariants().getVariant()) {
                    createVariantProductImpexRow(variant, baseProduct, catalogVersion, sourceSystemCode, site, impexGenericVariantProdict_stringBuilder,removeCategory_stringBuilder, existingProductsCode);
                }
            }

        } // end for to iterate over all base products


        //building impex
        importImpex.append(impexMacro_stringBuilder.toString());
        importImpex.append(STRING_RETURN);
        importImpex.append(STRING_RETURN);
        importImpex.append(STRING_RETURN);

        if (replaceMode){
            importImpex.append(impexReplaceCategoryHeader_stringBuilder.toString());
            importImpex.append(categoryProductRelation_stringBuilder.toString());
            importImpex.append(STRING_RETURN);
            importImpex.append(STRING_RETURN);
            importImpex.append(STRING_RETURN);
            System.out.println("Impex to replace baseProduct categories: \n" + importImpex);
            return;
        }

        importImpex.append(impexBaseProduct_stringBuilder.toString());
        importImpex.append(STRING_RETURN);
        importImpex.append(STRING_RETURN);
        importImpex.append(STRING_RETURN);

        importImpex.append(categoryProductRelation_stringBuilder.toString());
        importImpex.append(STRING_RETURN);
        importImpex.append(STRING_RETURN);
        importImpex.append(STRING_RETURN);

        importImpex.append(impexGenericVariantProdict_stringBuilder.toString());
        importImpex.append(STRING_RETURN);
        importImpex.append(STRING_RETURN);
        importImpex.append(STRING_RETURN);

        importImpex.append(removeCategory_stringBuilder.toString());
        importImpex.append(STRING_RETURN);
        importImpex.append(STRING_RETURN);



        System.out.println("Impex to reimport categories: \n" + importImpex);
        return;
    }



    public static void createBaseProductImpexRow(BaseProduct baseProduct, CatalogVersionModel catalogVersion, String sourceSystemCode, CMSSiteModel site, StringBuilder stringBuilder, StringBuilder categoryProductRelationBuilder, StringBuilder categoryRemoveBuilder, Set<String> existingProductsCode) {

        boolean baseProductExists = existingProductsCode.contains(solGroupProductService.getHybrisProductCode(baseProduct.getErpCode(), site));

        // Code and erpCode
        stringBuilder.append(IMPEX_SEPARATOR);
        stringBuilder.append( solGroupProductService.getHybrisProductCode(baseProduct.getErpCode(), site) );
        stringBuilder.append(IMPEX_SEPARATOR);
        stringBuilder.append(baseProduct.getErpCode());
        stringBuilder.append(IMPEX_SEPARATOR);

        // Legacy system
        stringBuilder.append(sourceSystemCode);
        stringBuilder.append(IMPEX_SEPARATOR);

        // Legacy names
        Map<String,String> localizedNames = Maps.newHashMap();
        for (final Name name : baseProduct.getNames().getName()) {
            localizedNames.put(name.getLang(), name.getValue());
        }
        if(StringUtils.isNotEmpty(localizedNames.get("en"))) {
            stringBuilder.append(localizedNames.get("en"));
        }
        stringBuilder.append(IMPEX_SEPARATOR);
        if(StringUtils.isNotEmpty(localizedNames.get("it"))) {
            stringBuilder.append(localizedNames.get("it"));
        }
        stringBuilder.append(IMPEX_SEPARATOR);
        if(StringUtils.isNotEmpty(localizedNames.get("de"))) {
            stringBuilder.append(localizedNames.get("de"));
        }
        stringBuilder.append(IMPEX_SEPARATOR);
        if(StringUtils.isNotEmpty(localizedNames.get("fr"))) {
            stringBuilder.append(localizedNames.get("fr"));
        }
        stringBuilder.append(IMPEX_SEPARATOR);

        // Material
        stringBuilder.append(baseProduct.getMaterial());
        stringBuilder.append(IMPEX_SEPARATOR);

        // Stockable
        if(BooleanUtils.isFalse(baseProduct.isStockable())) {
            stringBuilder.append("false");
        }
        stringBuilder.append(IMPEX_SEPARATOR);

        // Weight
        if(baseProduct.getWeight()!=null) {
            stringBuilder.append(baseProduct.getWeight().doubleValue());
        }
        stringBuilder.append(IMPEX_SEPARATOR);

        // Unit measure
        if(StringUtils.isNotEmpty(baseProduct.getUm())) {
            stringBuilder.append(baseProduct.getUm());
        }
        else {
            stringBuilder.append(default_um);
        }
        stringBuilder.append(IMPEX_SEPARATOR);

        // Status
        final String productStatusCode = baseProduct.getStatus();
        if (StringUtils.isNotEmpty(productStatusCode) && productStatusCode.equals(PRODUCT_STATUS_ENABLED_CODE)) {
            stringBuilder.append(ProductStatusEnum.ENABLED.getCode());
        } else {
            stringBuilder.append(ProductStatusEnum.DISABLED.getCode());
        }
        stringBuilder.append(IMPEX_SEPARATOR);

        // Tax group
        ProductTaxGroup productTaxGroup = getTaxGroup(baseProduct.getTaxCode(), catalogVersion);
        if(productTaxGroup!=null) {
            stringBuilder.append(productTaxGroup.getCode());
        }
        stringBuilder.append(IMPEX_SEPARATOR);

        // Variant type
        if(baseProduct.getVariants() != null && CollectionUtils.isNotEmpty(baseProduct.getVariants().getVariant())) {
            final VariantTypeModel variantType = commonDao.findItemByUniqueCode(VariantTypeModel._TYPECODE,
                    VariantTypeModel.CODE, GenericVariantProductModel._TYPECODE, VariantTypeModel.class);
            stringBuilder.append(variantType.getPk().toString());
        }
        stringBuilder.append(IMPEX_SEPARATOR);


        //If base product already exists on system, skip this step
        if(!baseProductExists || replaceMode) {

            // Categories
            Set<String> categoriesToAdd = Sets.newHashSet();
            Set<String> categoriesToRemove = Sets.newHashSet();

            Set<String> currentLegacyCategories = replaceMode?Sets.newHashSet():solGroupProductService.findCategoriesForProductByMaster(solGroupProductService.getHybrisProductCode(baseProduct.getErpCode(), site), MasterSystemEnum.LEGACY.getCode(), catalogVersion);
            Set<String> newLegacyCategories = Sets.newHashSet();

            boolean hasVisibility = false;
            if (baseProduct.getCommercialCategories() != null
                    && CollectionUtils.isNotEmpty(baseProduct.getCommercialCategories().getCommercialCategory())) {
                for (final CommercialCategory commercialCategory : baseProduct.getCommercialCategories().getCommercialCategory()) {
                    if (StringUtils.isNotEmpty(commercialCategory.getCode())) {
                        String hybrisCategoryCode = solGroupCategoryService.getHybrisCategoryCode(commercialCategory.getCode(), site);
                        newLegacyCategories.add(hybrisCategoryCode);
                        if (hybrisCategoryCode.endsWith(visibilityCategory_suffix)) {
                            hasVisibility = true;
                        }
                        if (!currentLegacyCategories.contains(hybrisCategoryCode)) {
                            categoriesToAdd.add(hybrisCategoryCode);
                        }
                    }
                }
            }

            if (hasVisibility == false) {
                String visibilityCategory_all = solGroupCategoryService.getHybrisCategoryCode(configurationService.getConfiguration().getString(SolgroupCoreConstants.PROPERTY_NAME_CATEGORY_VISIBILITY_ALL), site);
                newLegacyCategories.add(visibilityCategory_all);
                if (!currentLegacyCategories.contains(visibilityCategory_all)) {
                    categoriesToAdd.add(visibilityCategory_all);
                }
            }

            if (baseProduct.getVariants() != null && CollectionUtils.isNotEmpty(baseProduct.getVariants().getVariant())) {
                for (Variant variant : baseProduct.getVariants().getVariant()) {
                    if (variant.getVariantsCategories() != null && CollectionUtils.isNotEmpty(variant.getVariantsCategories().getVariantCategory())) {
                        for (VariantCategory variantCategory : variant.getVariantsCategories().getVariantCategory()) {
                            if (StringUtils.isNotEmpty(variantCategory.getVariantCategory())) {
                                String hybrisCategoryCode = solGroupCategoryService.getHybrisCategoryCode(variantCategory.getVariantCategory(), site);
                                newLegacyCategories.add(hybrisCategoryCode);
                                if (!currentLegacyCategories.contains(hybrisCategoryCode)) {
                                    categoriesToAdd.add(hybrisCategoryCode);
                                }
                            }
                        }
                    }
                }
            }


            for (String currentCategoryCode : currentLegacyCategories) {
                if (!newLegacyCategories.contains(currentCategoryCode)) {
                    categoriesToRemove.add(currentCategoryCode);
                }
            }


            if (CollectionUtils.isNotEmpty(categoriesToAdd)){
                if(replaceMode) {
                    categoryProductRelationBuilder.append(IMPEX_SEPARATOR);
                    categoryProductRelationBuilder.append(solGroupProductService.getHybrisProductCode(baseProduct.getErpCode(), site));
                    categoryProductRelationBuilder.append(IMPEX_SEPARATOR);
                    for (String categoryCode : categoriesToAdd) {
                        categoryProductRelationBuilder.append(categoryCode);
                        categoryProductRelationBuilder.append(IMPEX_VALUES_SEPARATOR);
                    }
                    categoryProductRelationBuilder.append(STRING_RETURN);
                }else {
                    for (String categoryCode : categoriesToAdd) {
                        categoryProductRelationBuilder.append(IMPEX_SEPARATOR);
                        categoryProductRelationBuilder.append(categoryCode);
                        categoryProductRelationBuilder.append(IMPEX_SEPARATOR);
                        categoryProductRelationBuilder.append(solGroupProductService.getHybrisProductCode(baseProduct.getErpCode(), site));
                        categoryProductRelationBuilder.append(IMPEX_SEPARATOR);
                        categoryProductRelationBuilder.append(STRING_RETURN);
                    }
                }
            }

            //Probably never used because existing product will not follow this flow
            if (CollectionUtils.isNotEmpty(categoriesToRemove)) {
                String categoryStr = "";
                for (String categoryCode : categoriesToRemove) {
                    categoryStr = categoryStr + categoryCode + ",";
                }
                if (categoryStr.endsWith(",")) {
                    categoryStr = categoryStr.substring(0, categoryStr.length() - 1);
                }
                if (!categoryStr.isEmpty()) {
                    categoryRemoveBuilder.append(IMPEX_SEPARATOR);
                    categoryRemoveBuilder.append(solGroupProductService.getHybrisProductCode(baseProduct.getErpCode(), site));
                    categoryRemoveBuilder.append(IMPEX_SEPARATOR);
                    categoryRemoveBuilder.append(categoryStr);
                    categoryRemoveBuilder.append(IMPEX_SEPARATOR);
                    categoryRemoveBuilder.append(IMPEX_SEPARATOR);
                    categoryRemoveBuilder.append(STRING_RETURN);
                }
            }
        }

        stringBuilder.append(IMPEX_SEPARATOR);
        stringBuilder.append(IMPEX_SEPARATOR);
        stringBuilder.append(STRING_RETURN);

    }

    public static void createVariantProductImpexRow(Variant variant, BaseProduct baseProduct, CatalogVersionModel catalogVersion, String sourceSystemCode, CMSSiteModel site, StringBuilder stringBuilder, StringBuilder categoryRemoveBuilder, Set<String> existingProductsCode) {

        boolean baseProductExists = existingProductsCode.contains(solGroupProductService.getHybrisProductCode(baseProduct.getErpCode(), site));

        boolean variantExists = existingProductsCode.contains(solGroupProductService.getHybrisProductCode(variant.getErpCode(), site));

        // Code and erpCode
        stringBuilder.append(IMPEX_SEPARATOR);
        stringBuilder.append(solGroupProductService.getHybrisProductCode(variant.getErpCode(), site));
        stringBuilder.append(IMPEX_SEPARATOR);
        stringBuilder.append(variant.getErpCode());
        stringBuilder.append(IMPEX_SEPARATOR);

        // Legacy system
        stringBuilder.append(sourceSystemCode);
        stringBuilder.append(IMPEX_SEPARATOR);

        // Localized legacy names
        Map<String,String> localizedNames = Maps.newHashMap();
        for (final com.solgroup.core.ws.services.product.xml.ProductsImportHybris.BaseProducts.BaseProduct.Variants.Variant.Names.Name name : variant.getNames().getName()) {
            localizedNames.put(name.getLang(), name.getValue());
        }
        if(StringUtils.isNotEmpty(localizedNames.get("en"))) {
            stringBuilder.append(localizedNames.get("en"));
        }
        stringBuilder.append(IMPEX_SEPARATOR);
        if(StringUtils.isNotEmpty(localizedNames.get("it"))) {
            stringBuilder.append(localizedNames.get("it"));
        }
        stringBuilder.append(IMPEX_SEPARATOR);
        if(StringUtils.isNotEmpty(localizedNames.get("de"))) {
            stringBuilder.append(localizedNames.get("de"));
        }
        stringBuilder.append(IMPEX_SEPARATOR);
        if(StringUtils.isNotEmpty(localizedNames.get("fr"))) {
            stringBuilder.append(localizedNames.get("fr"));
        }
        stringBuilder.append(IMPEX_SEPARATOR);

        // Weight
        if(variant.getWeight()!=null) {
            stringBuilder.append(variant.getWeight().doubleValue());
        }
        stringBuilder.append(IMPEX_SEPARATOR);

        // Unit measure
        if(StringUtils.isNotEmpty(variant.getUm())) {
            stringBuilder.append(variant.getUm());
        }
        else if (StringUtils.isNotEmpty(baseProduct.getUm())) {
            stringBuilder.append(baseProduct.getUm());
        }
        else {
            stringBuilder.append(default_um);
        }
        stringBuilder.append(IMPEX_SEPARATOR);

        // Tax group
        ProductTaxGroup productTaxGroup = getTaxGroup(variant.getTaxCode(), catalogVersion);
        if(productTaxGroup!=null) {
            stringBuilder.append(productTaxGroup.getCode());
        }
        stringBuilder.append(IMPEX_SEPARATOR);

        // Material
        stringBuilder.append(baseProduct.getMaterial());
        stringBuilder.append(IMPEX_SEPARATOR);


        // Status
        // Base product is disabled --> all variants all disabled
        if (!baseProduct.getStatus().equals(PRODUCT_STATUS_ENABLED_CODE)) {
            stringBuilder.append(ProductStatusEnum.DISABLED.getCode());
        }
        // Base product is enabled --> variant status read from legacy
        else {
            final String productStatusCode = variant.getStatus();
            if (StringUtils.isNotEmpty(productStatusCode) && productStatusCode.equals(PRODUCT_STATUS_ENABLED_CODE)) {
                stringBuilder.append(ProductStatusEnum.ENABLED.getCode());
            } else {
                stringBuilder.append(ProductStatusEnum.DISABLED.getCode());
            }
        }
        stringBuilder.append(IMPEX_SEPARATOR);

        // Stockable
        if(BooleanUtils.isFalse(baseProduct.isStockable())) {
            stringBuilder.append("false");
        }
        stringBuilder.append(IMPEX_SEPARATOR);

        // Base product
        stringBuilder.append(solGroupProductService.getHybrisProductCode(baseProduct.getErpCode(), site));
        stringBuilder.append(IMPEX_SEPARATOR);

        if((!baseProductExists && !variantExists) || replaceMode) {

            // Variant value categories
            List<String> categoriesToAdd = Lists.newArrayList();
            List<String> categoriesToRemove = Lists.newArrayList();

            Set<String> currentLegacyCategories = replaceMode?Sets.newHashSet():solGroupProductService.findCategoriesForProductByMaster(solGroupProductService.getHybrisProductCode(variant.getErpCode(), site), MasterSystemEnum.LEGACY.getCode(), catalogVersion);
            Set<String> newLegacyCategories = Sets.newHashSet();

            for (VariantCategory variantCategory : variant.getVariantsCategories().getVariantCategory()) {
                if (StringUtils.isNotEmpty(variantCategory.getVariantValueCategory())) {
                    String hybrisCategoryCode = solGroupCategoryService.getHybrisCategoryCode(variantCategory.getVariantValueCategory(), site);
                    newLegacyCategories.add(hybrisCategoryCode);
                    if (!currentLegacyCategories.contains(hybrisCategoryCode)) {
                        categoriesToAdd.add(hybrisCategoryCode);
                    }
                }
            }

            for (String currentCategoryCode : currentLegacyCategories) {
                if (!newLegacyCategories.contains(currentCategoryCode)) {
                    categoriesToRemove.add(currentCategoryCode);
                }
            }

            if (CollectionUtils.isNotEmpty(categoriesToAdd)) {
                String categoryStr = "";
                for (String categoryCode : categoriesToAdd) {
                    categoryStr = categoryStr + categoryCode + ",";
                }
                if (categoryStr.endsWith(",")) {
                    categoryStr = categoryStr.substring(0, categoryStr.length() - 1);
                }
                stringBuilder.append(categoryStr);
            }
            stringBuilder.append(IMPEX_SEPARATOR);

            if (CollectionUtils.isNotEmpty(categoriesToRemove)) {
                String categoryStr = "";
                for (String categoryCode : categoriesToRemove) {
                    categoryStr = categoryStr + categoryCode + ",";
                }
                if (categoryStr.endsWith(",")) {
                    categoryStr = categoryStr.substring(0, categoryStr.length() - 1);
                }

                if (!categoryStr.isEmpty()) {
                    categoryRemoveBuilder.append(IMPEX_SEPARATOR);
                    categoryRemoveBuilder.append(solGroupProductService.getHybrisProductCode(variant.getErpCode(), site));
                    categoryRemoveBuilder.append(IMPEX_SEPARATOR);
                    categoryRemoveBuilder.append(categoryStr);
                    categoryRemoveBuilder.append(IMPEX_SEPARATOR);
                    categoryRemoveBuilder.append(IMPEX_SEPARATOR);
                    categoryRemoveBuilder.append(STRING_RETURN);
                }

            }
        }else if(baseProductExists && !variantExists){
            //In this case we have to manage variant value category assignment
            // Variant value categories
            List<String> categoriesToAdd = Lists.newArrayList();

            Set<CategoryModel> baseProductCurrentCategories = solGroupProductService.findCategoriesObjectForProductByMaster(solGroupProductService.getHybrisProductCode(baseProduct.getErpCode(), site), MasterSystemEnum.LEGACY.getCode(), catalogVersion);

            boolean variantValueCategoryFound = false;
            //For each variant category a random variant value category is set
            for (CategoryModel category : baseProductCurrentCategories) {
                if (category instanceof VariantCategoryModel) {
                    variantValueCategoryFound = false;
                    //Retrieve a default VariantCategoryValue for current VariantCategory
                    Collection<CategoryModel> categorySubCategories = category.getAllSubcategories();
                    if(categorySubCategories != null && !categorySubCategories.isEmpty()){
                        Iterator<CategoryModel> categorySubCategoriesIterator = categorySubCategories.iterator();
                        while(categorySubCategoriesIterator.hasNext() && !variantValueCategoryFound){
                            CategoryModel categorySubCategory =  categorySubCategoriesIterator.next();
                            if (categorySubCategory instanceof VariantValueCategoryModel) {
                                //The first VariantValueCategory found is inserted as variant superclass
                                categoriesToAdd.add(categorySubCategory.getCode());
                                variantValueCategoryFound = true;
                            }

                        }
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(categoriesToAdd)) {
                String categoryStr = "";
                for (String categoryCode : categoriesToAdd) {
                    categoryStr = categoryStr + categoryCode + ",";
                }
                if (categoryStr.endsWith(",")) {
                    categoryStr = categoryStr.substring(0, categoryStr.length() - 1);
                }
                stringBuilder.append(categoryStr);
            }
            stringBuilder.append(IMPEX_SEPARATOR);
        }

        stringBuilder.append(IMPEX_SEPARATOR);
        stringBuilder.append(IMPEX_SEPARATOR);
        stringBuilder.append(STRING_RETURN);

    }


    public static ProductTaxGroup getTaxGroup(final String taxCode, final CatalogVersionModel catalogVersion) {
        ProductTaxGroup productTaxGroup = null;

        if (StringUtils.isNotEmpty(taxCode)) {
            final List<TaxRowModel> taxRows = solGroupTaxRowDao.findTaxRowsByCode(taxCode, catalogVersion);

            if (CollectionUtils.isNotEmpty(taxRows) && taxRows.get(0).getPg() != null) {
                productTaxGroup = (ProductTaxGroup) taxRows.get(0).getPg();
            }
        } else {
            final List<TaxRowModel> taxRows = solGroupTaxRowDao.findTaxRowsByCode(null, catalogVersion);
            for (TaxRowModel taxRowModel : taxRows) {
                if (BooleanUtils.isTrue(taxRowModel.getDefaultTax())) {
                    productTaxGroup = (ProductTaxGroup)taxRowModel.getPg();
                    break;
                }
            }
        }

        if(productTaxGroup==null) {
            LOG.warn(String.format("No TAX ROW found for code [%s]",taxCode));
        }

        return productTaxGroup;

    }

    public static InputStream getMediaData(String mediaCode) {
        MediaModel mediaModel = mediaService.getMedia(mediaCode)
        InputStream inputStream = mediaService.getStreamFromMedia(mediaModel)
        return inputStream
    }

    public static void getExistingProductsCode(Set<String> existingProductsCode, List<ProductModel> foundProducts) {
        if(foundProducts != null && !foundProducts.isEmpty()) {
            for (ProductModel prod : foundProducts){
                existingProductsCode.add(prod.getCode());
            }
        }
    }
}

