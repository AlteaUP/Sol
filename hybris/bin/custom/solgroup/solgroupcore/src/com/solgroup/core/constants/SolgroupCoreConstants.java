/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.solgroup.core.constants;

import java.util.Arrays;

/**
 * Global class for all SolgroupCore constants. You can add global constants for your extension into this class.
 */
public final class SolgroupCoreConstants extends GeneratedSolgroupCoreConstants
{
	public static final String EXTENSIONNAME = "solgroupcore";


	private SolgroupCoreConstants()
	{
		//empty
	}

	// implement here constants used by this extension
	public static final String QUOTE_BUYER_PROCESS = "quote-buyer-process";
	public static final String QUOTE_SALES_REP_PROCESS = "quote-salesrep-process";
	public static final String QUOTE_USER_TYPE = "QUOTE_USER_TYPE";
	public static final String QUOTE_SELLER_APPROVER_PROCESS = "quote-seller-approval-process";
	public static final String QUOTE_TO_EXPIRE_SOON_EMAIL_PROCESS = "quote-to-expire-soon-email-process";
	public static final String QUOTE_EXPIRED_EMAIL_PROCESS = "quote-expired-email-process";
	public static final String QUOTE_POST_CANCELLATION_PROCESS = "quote-post-cancellation-process";

	public static final String APPLICATION_CATEGORY_PREFIX = "APP";
	public static final String TOP_BAR_CATEGORY_PREFIX = "TBC";
	public static final String APPLICATION_CATEGORY_LEVEL1_INDEXED_PROPERTY = "applicationCategory_lvl1";
	public static final String APPLICATION_CATEGORY_LEVEL2_INDEXED_PROPERTY = "applicationCategory_lvl2";
	public static final String TOP_BAR_CATEGORY_LEVEL1_INDEXED_PROPERTY = "topBarCategory_lvl1";
	public static final String TOP_BAR_CATEGORY_LEVEL2_INDEXED_PROPERTY = "topBarCategory_lvl2";

    // B2BUnit branch
    public static final String SESSION_PARAM_B2BUNITS_BRANCH_SET = "b2bUnitsBranchSet";
    // Price list code
    public static final String SESSION_PARAM_PRICE_LIST_CODE = "priceListCode";
    // Visibility categories
    public static final String SESSION_PARAM_VSIBILITY_CATEGORIES = "visibilityCategories";

    // B2BCustomer list for SolGroup
    public static final String SOLGROUP_B2B_CUSTOMER_LIST_UID = "solGroupB2BCustomers";
    

    
    // AS Customer group id
    //public static final String AS_CUSTOMER_GROUP_ID = "solOrganizationGroup";
    
    // Customer group (not to purchase)
    public static final String CUSTOMER_GROUP_ID = "customergroup";
    
    // Organization user group
    public static final String ORGANIZATION_GROUP_ID = "solOrganizationGroup";
    
    // Order entry solGroup properties
    public static final String ORDERENTRY_PURCHASE_ORDER_NUMER = "purchaseOrderNumber";
    public static final String ORDERENTRY_ORDER_DATA = "orderData";
    public static final String ORDERENTRY_CIG = "cgi";
    public static final String ORDERENTRY_CUP = "cup";
    
    public static final int PURCHASE_ORDER_NUMBER_MAX_LENGTH = 15;
    public static final int CIG_LENGTH = 10;
    public static final int CUP_LENGTH = 15;
    
    // Order entry orderData format
    public static final String ORDERENTRY_ORDER_DATA_FORMAT = "dd/MM/yyyy";
    
    
    // Default unit measure
    public static final String DEFAULT_UNIT_MEASURE = "pieces";
    
    // Unit measure separator
    public static final String UNIT_MEASURE_SEPARATOR = "/";
    
    // CronJob for impex import : code pattern (es : solgroupIT___RAMSES_IT___jobCode_messageId_timeStamp)
    public static final String IMPEX_IMPORT_CRONJOB_CODE_PATTERN = "%s___%s___%s_%s_%s";
    
    // Category visibiloty all property name
    public static final String PROPERTY_NAME_CATEGORY_VISIBILITY_ALL = "category.visibility.all";
    
    public static final String PROPERTY_NAME_CURRENT_ENV = "solgroupEnv";
    public static final String PROPERTY_DEFAULT_ENV = "local";

    public static class WamConstants {

        public static final String FILTER_FIELD_WAM_T_SOCIETA_CODICE = "WAM_T_SOCIETA_Codice";
        public static final String FILTER_FIELD_CLIENTE_CODICE_NO_CONTRACTS = "cliente_codice";
        public static final String FILTER_FIELD_CLIENTE_CODICE_CONTRACTS = "WAM_T_CONTATTO_Codice";
        public static final String FILTER_FIELD_DATA_INVOICES = "Data";
        public static final String FILTER_FIELD_DATA_BUSINESS_LETTERS = "Data_Documento";
        public static final String FILTER_FIELD_DATA_DUNNING_LETTERS = "Data_Sollecito";
        public static final String FILTER_FIELD_DATA_DN = "DDT_Data";
        public static final String FILTER_FIELD_DATA_CONTRACTS = "DataStipulaContratto";
        public static final String FILTER_FIELD_CLIENT_NAME_INVOICES = "Pagatore_Ragione_Sociale";
        public static final String FILTER_FIELD_CLIENT_NAME_BUSINESS_LETTERS = "Cliente_Ragione_Sociale";
        public static final String FILTER_FIELD_CLIENT_NAME_DUNNING_LETTERS = "WAM_T_SOCIETA_Descrizione";
        public static final String FILTER_FIELD_CLIENT_NAME_DN = "S_Customer_Payer_Name";
        public static final String FILTER_FIELD_CLIENT_NAME_CONTRACTS = "WAM_T_CONTATTO_Descrizione";
        public static final String FIELD_DOCUMENTO = "Documento";
        public static final String FIELD_DOCUMENTO_PA = "Documento_PA";
        public static final String FIELD_UPDATE_DATE = "UPDATE_DATE";
        public static final String FIELD_DOCUMENTO_NUMERO_INVOICES = "Numero";
        public static final String FIELD_DOCUMENTO_NUMERO_BUSINESS_LETTERS = "Sios_ID";
        public static final String FIELD_DOCUMENTO_NUMERO_DUNNING_LETTERS = "Identificativo_Proposta";
        public static final String FIELD_DOCUMENTO_NUMERO_DDT = "DDT_Numero";
        public static final String FIELD_DOCUMENTO_NUMERO_CONTRACTS = "WAM_T_CONTATTO_Codice";
        public static final String WAM_DATA_OBJECT_INVOICES = "WAM_V_FATTURE_ATTIVE_REST";
        public static final String WAM_DATA_OBJECT_BUSINESS_LETTERS = "WAM_V_SOL_LETTERE_COMMERCIALI_CRM";
        public static final String WAM_DATA_OBJECT_DUNNING_LETTERS = "WAM_V_SOL_LETTERE_SOLLECITO_CMR";
        public static final String WAM_DATA_OBJECT_DN = "WAM_V_SOL_DDT_ATTIVI_CRM";
        public static final String WAM_DATA_OBJECT_CONTRACTS = "WAM_V_SOL_CONTRATTI_UI";

        public enum WamDocumentsTypeEnum {
            INVOICES(WamConstants.WAM_DATA_OBJECT_INVOICES, "invoices", "text.account.documents.breadcrumb.invoices"),
            BUSINESS_LETTER(WamConstants.WAM_DATA_OBJECT_BUSINESS_LETTERS, "businessLetters", "text.account.documents.breadcrumb.businessletter"),
            DUNNING_LETTER(WamConstants.WAM_DATA_OBJECT_DUNNING_LETTERS, "dunningLetters", "text.account.documents.breadcrumb.dunningletter"),
            DN(WamConstants.WAM_DATA_OBJECT_DN, "dn", "text.account.documents.breadcrumb.dn"),
            CONTRACTS(WamConstants.WAM_DATA_OBJECT_CONTRACTS, "contracts", "text.account.documents.breadcrumb.contracts");

            private final String wamCode;
            private final String feCode;
            private final String breadcrumbCode;

            private WamDocumentsTypeEnum(String wamCode, String feCode, String breadcrumbCode) {
                this.wamCode = wamCode;
                this.feCode = feCode;
                this.breadcrumbCode = breadcrumbCode;
            }

            public String getWamCode() {
                return wamCode;
            }

            public String getFeCode() {
                return feCode;
            }

            public String getBreadcrumbCode() {
                return breadcrumbCode;
            }

            public static String getWamCodeFromFeCode(String feCode) {
                return Arrays.stream(WamDocumentsTypeEnum.values())
                        .filter(wamDocumentsTypeEnum -> feCode.equalsIgnoreCase(wamDocumentsTypeEnum.getFeCode()))
                        .map(WamDocumentsTypeEnum::getWamCode).findFirst().orElse(WamDocumentsTypeEnum.INVOICES.getWamCode());
            }

            public static String getBreadcrumbCodeFromFeCode(String feCode) {
                return Arrays.stream(WamDocumentsTypeEnum.values())
                        .filter(wamDocumentsTypeEnum -> feCode.equalsIgnoreCase(wamDocumentsTypeEnum.getFeCode()))
                        .map(WamDocumentsTypeEnum::getBreadcrumbCode).findFirst().orElse(WamDocumentsTypeEnum.INVOICES.getBreadcrumbCode());
            }
        }
    }

    
}
