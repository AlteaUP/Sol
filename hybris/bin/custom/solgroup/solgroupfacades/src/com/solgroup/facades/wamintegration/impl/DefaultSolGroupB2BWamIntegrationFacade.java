package com.solgroup.facades.wamintegration.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.service.wamintegration.impl.DefaultSolgroupB2BWamIntegrationService;
import com.solgroup.core.service.wamintegration.json.response.Field;
import com.solgroup.core.service.wamintegration.json.response.WamDownloadDocumentResponse;
import com.solgroup.core.service.wamintegration.json.response.WamGetDocumentsListResponse;
import com.solgroup.facades.wamintegration.SolGroupB2BWamIntegrationFacade;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commercefacades.wamdocument.data.WamDocumentData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.PaginationData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.commerceservices.search.pagedata.SortData;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class DefaultSolGroupB2BWamIntegrationFacade implements SolGroupB2BWamIntegrationFacade {

    private static final Logger LOG = Logger.getLogger(DefaultSolGroupB2BWamIntegrationFacade.class);
    private static final String SORT_BY_DOCUMENT_NUMBER = "byDocumentNumber";
    private static final String SORT_BY_DATE ="byDate";
    private static final String WAM_TOKEN = "wamToken";
    private static final String WAM_GET_DOCUMENTS_LIST_MAX_ROWS = "wam.integration.api.getdocumentslist.maxrows";

    private UserService userService;

    private DefaultSolgroupB2BWamIntegrationService solgroupB2BWamIntegrationService;

    @Override
    public SearchPageData<WamDocumentData> getPagedDocumentsList(PageableData pageableData, HttpSession session, String documentsType, LocalDate startDate, LocalDate endDate) {

        Integer maxRows = Integer.valueOf(Config.getParameter(WAM_GET_DOCUMENTS_LIST_MAX_ROWS));

        List<WamDocumentData> invoicesList = new ArrayList<>();
        final SearchPageData<WamDocumentData> result = new SearchPageData<>();

        //Check if token is present in session
        String authToken = (String) session.getAttribute(WAM_TOKEN);
        if(StringUtils.isEmpty(authToken)) {
            authToken = getWamAuthToken(session);
        }

        //Retrieve companyCode e customerCode from B2BUnit
        B2BUnitModel b2bUnitmodel = ((B2BCustomerModel) userService.getCurrentUser()).getDefaultB2BUnit();
        String b2bUnitUid = b2bUnitmodel.getUid();
        if(!StringUtils.isEmpty(b2bUnitUid) && b2bUnitUid.contains("_")) {
            //SolgroupIT_{companyCode}_{customerCode}...
            String[] splitB2BUnitCode = b2bUnitUid.split("_");
            String companyCode = splitB2BUnitCode[1];
            String customerCode = splitB2BUnitCode[2];

            Integer pageNumber = 0;
            Integer numberOfCallDone = 0;

            do{
                pageNumber++;
                ResponseEntity<String> wamResponse = solgroupB2BWamIntegrationService.getDocumentsListPOST(authToken, companyCode, customerCode, documentsType, pageNumber, maxRows, startDate, endDate);
                if (wamResponse != null && wamResponse.getStatusCode() != null
                        && wamResponse.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    //Token expired, new authentication is required
                    authToken = getWamAuthToken(session);
                    wamResponse = solgroupB2BWamIntegrationService.getDocumentsListPOST(authToken, companyCode, customerCode, documentsType, pageNumber, maxRows, startDate, endDate);
                }
                if (wamResponse != null && wamResponse.getStatusCode() != null
                        && wamResponse.getStatusCode() == HttpStatus.OK) {
                    numberOfCallDone++;
                    formatDocumentListResponse(wamResponse, invoicesList);
                }else{
                    //Safe break to avoid infinite loop
                    break;
                }
            }while(invoicesList.size() >= (maxRows * numberOfCallDone));
        }

        result.setPagination(getPaginationForDocumentData(pageableData, invoicesList.size()));
        result.setSorts(getSortsDataForDocumentData(pageableData.getSort()));

        //I sort the list manually and exclude the documents in excess
        List<WamDocumentData> invoicesListSorted = sortDocumentData(invoicesList, pageableData);
        result.setResults(invoicesListSorted);
        return result;
    }

    @Override
    public WamDownloadDocumentResponse downloadPdfDocument(HttpSession session, String documentId, String documentKey, String documentsType) {
        WamDownloadDocumentResponse response = new WamDownloadDocumentResponse();

        //Check if token is present in session
        String authToken = (String) session.getAttribute(WAM_TOKEN);
        if(StringUtils.isEmpty(authToken)) {
            authToken = getWamAuthToken(session);
        }

        ResponseEntity<String> wamResponse = solgroupB2BWamIntegrationService.downloadDocumentPOST(authToken, documentId, documentKey, documentsType);
        if (wamResponse != null && wamResponse.getStatusCode() != null
                && wamResponse.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            //Token expired, new authentication is required
            authToken = getWamAuthToken(session);
            wamResponse = solgroupB2BWamIntegrationService.downloadDocumentPOST(authToken, documentId, documentKey, documentsType);
        }
        if (wamResponse != null && wamResponse.getStatusCode() != null
                && wamResponse.getStatusCode() == HttpStatus.OK) {
            response = formatDownloadDocument(wamResponse);
        }
        return response;
    }

    private List<WamDocumentData> sortDocumentData(List<WamDocumentData> invoicesList, PageableData pageableData){
        List<WamDocumentData> invoicesListSorted = new ArrayList<>();
        int pageSize = pageableData.getPageSize();
        int currentPage = pageableData.getCurrentPage();
        int listSize = invoicesList.size();
        if(pageableData.getSort().equalsIgnoreCase(SORT_BY_DOCUMENT_NUMBER)){
            invoicesList.sort(Comparator.comparing(WamDocumentData::getDocumentNumber));
            if(invoicesList.size() > pageSize){
                invoicesListSorted = removeDocumentsInExcess(invoicesList, listSize, pageSize, currentPage);
            }else{
                invoicesListSorted.addAll(invoicesList);
            }
        }else if(pageableData.getSort().equalsIgnoreCase(SORT_BY_DATE)){
            invoicesList.sort(Comparator.comparing(WamDocumentData::getDate).reversed());
            if(invoicesList.size() > pageSize){
                invoicesListSorted = removeDocumentsInExcess(invoicesList, listSize, pageSize, currentPage);
            }else{
                invoicesListSorted.addAll(invoicesList);
            }
        }
        return invoicesListSorted;
    }

    private List<WamDocumentData> removeDocumentsInExcess(List<WamDocumentData> invoicesList, int listSize, int pageSize, int currentPage){
        //Need to remove documents in excess
        int firstPageItemIndex = pageSize * currentPage;
        int lastPageItemIndex = firstPageItemIndex + pageSize;
        if(listSize-1 < lastPageItemIndex){
            lastPageItemIndex = (pageSize * currentPage) + (listSize % pageSize);
        }

        return invoicesList.subList(firstPageItemIndex, lastPageItemIndex);
    }

    private List<SortData> getSortsDataForDocumentData(String selectedSort){
        List<SortData> sorts = new ArrayList<>();
        SortData sortById = new SortData();
        sortById.setCode(SORT_BY_DOCUMENT_NUMBER);
        sortById.setName(null);
        sortById.setSelected(!StringUtils.isEmpty(selectedSort) && selectedSort.equalsIgnoreCase(SORT_BY_DOCUMENT_NUMBER));
        sorts.add(sortById);
        SortData sortByInsertDate = new SortData();
        sortByInsertDate.setCode(SORT_BY_DATE);
        sortByInsertDate.setName(null);
        sortByInsertDate.setSelected(!StringUtils.isEmpty(selectedSort) && selectedSort.equalsIgnoreCase(SORT_BY_DATE));
        sorts.add(sortByInsertDate);
        return sorts;
    }

    private PaginationData getPaginationForDocumentData(PageableData pageableData, long totalNumberOfResults) {
        PaginationData paginationData = new PaginationData();
        paginationData.setCurrentPage(pageableData.getCurrentPage());
        paginationData.setPageSize(pageableData.getPageSize());
        paginationData.setSort(pageableData.getSort());
        paginationData.setTotalNumberOfResults(totalNumberOfResults);
        BigDecimal numberOfPages = BigDecimal.valueOf(totalNumberOfResults).divide(BigDecimal.valueOf(pageableData.getPageSize()), new MathContext(1, RoundingMode.UP));
        paginationData.setNumberOfPages(Math.max(numberOfPages.intValue(), 1));
        return paginationData;
    }


    private void formatDocumentListResponse(ResponseEntity<String> wamResponse, List<WamDocumentData> invoicesList){
        //Retrieve response body
        ObjectMapper mapper = new ObjectMapper();
        //Create a DTO List containing each document
        List<WamGetDocumentsListResponse> responseBody = new ArrayList<>();
        try {
            String responseBodyString = wamResponse.getBody();
            responseBody = mapper.readValue(responseBodyString,
                    mapper.getTypeFactory().constructCollectionType(List.class, WamGetDocumentsListResponse.class));
            if(responseBody != null){
                for (WamGetDocumentsListResponse document : responseBody) {
                    WamDocumentData documentData = populateDocumentDataForDocumentType(document);
                    invoicesList.add(documentData);
                }

            }
        } catch (IOException e) {
            LOG.error("Error reading response due to: " + e.getMessage());
        }
    }

    private WamDocumentData populateDocumentDataForDocumentType(WamGetDocumentsListResponse document) {

        final String lightDateFormat = "yyyyMMdd";
        final String fullDateFormat = "yyyyMMddhhmmss";

        WamDocumentData documentData = new WamDocumentData();
        //Id used to retrieve pdf file in subsequent call
        int documentID = document.getId();
        documentData.setId(documentID);
        //Search for specific fields to show on FE
        for (Field documentField : document.getFields()) {
            String fieldKey = documentField.getKey();
            String fieldValue = documentField.getValue();
            Date date = null;
            if(!StringUtils.isEmpty(documentField.getValue())) {
                switch (fieldKey) {
                    case SolgroupCoreConstants.WamConstants.FIELD_DOCUMENTO_NUMERO_INVOICES:
                    case SolgroupCoreConstants.WamConstants.FIELD_DOCUMENTO_NUMERO_BUSINESS_LETTERS:
                    case SolgroupCoreConstants.WamConstants.FIELD_DOCUMENTO_NUMERO_DUNNING_LETTERS:
                    case SolgroupCoreConstants.WamConstants.FIELD_DOCUMENTO_NUMERO_DDT:
                    case SolgroupCoreConstants.WamConstants.FIELD_DOCUMENTO_NUMERO_CONTRACTS:
                        documentData.setDocumentNumber(fieldValue);
                        break;
                    case SolgroupCoreConstants.WamConstants.FILTER_FIELD_DATA_INVOICES:
                    case SolgroupCoreConstants.WamConstants.FILTER_FIELD_DATA_BUSINESS_LETTERS:
                    case SolgroupCoreConstants.WamConstants.FILTER_FIELD_DATA_DUNNING_LETTERS:
                    case SolgroupCoreConstants.WamConstants.FILTER_FIELD_DATA_DN:
                    case SolgroupCoreConstants.WamConstants.FILTER_FIELD_DATA_CONTRACTS:
                        try {
                            date = new SimpleDateFormat(fullDateFormat).parse(fieldValue);
                        } catch (ParseException e) {
                            try {
                                date = new SimpleDateFormat(lightDateFormat).parse(fieldValue);
                            } catch (ParseException parseException) {
                                parseException.printStackTrace();
                            }
                        }
                        documentData.setDate(date);
                        break;
                    case SolgroupCoreConstants.WamConstants.FIELD_UPDATE_DATE:
                        try {
                            date = new SimpleDateFormat(fullDateFormat).parse(fieldValue);
                        } catch (ParseException e) {
                            try {
                                date = new SimpleDateFormat(lightDateFormat).parse(fieldValue);
                            } catch (ParseException parseException) {
                                parseException.printStackTrace();
                            }
                        }
                        documentData.setUpdatedDate(date);
                        break;
                    case SolgroupCoreConstants.WamConstants.FIELD_DOCUMENTO:
                        //Field used to retrieve document file with subsequent call
                        //Split string with # and take all characters after this symbol
                        if(fieldValue.contains("#") && fieldValue.toLowerCase().contains(".pdf")) {
                            String documentKey = fieldValue.split("#")[1];
                            documentData.setDocument(documentKey);
                        }
                        break;
                    case SolgroupCoreConstants.WamConstants.FIELD_DOCUMENTO_PA:
                        //Field used to retrieve document file with subsequent call
                        //Split string with # and take all characters after this symbol
                        if(fieldValue.contains("#") && fieldValue.toLowerCase().contains(".pdf")) {
                            String documentKey = fieldValue.split("#")[1];
                            documentData.setDocument(documentKey);
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        if(StringUtils.isEmpty(documentData.getDocumentNumber())){
            //fallback value, set ID as document number value to avoid null pointer during sort
            documentData.setDocumentNumber(String.valueOf(documentID));
        }
        if(documentData.getDate() == null){
            //fallback value, set now as date to avoid null pointer during sort
            documentData.setDate(new Date());
        }

        return documentData;
    }

    private WamDownloadDocumentResponse formatDownloadDocument(ResponseEntity<String> wamResponse){
        WamDownloadDocumentResponse responseBody = new WamDownloadDocumentResponse();
        String responseBodyString = wamResponse.getBody();
        if(responseBodyString == null){
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            responseBody = mapper.readValue(responseBodyString, WamDownloadDocumentResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseBody;
    }

    private String getWamAuthToken(HttpSession session){
        //Retrieve auth token
        String authToken = solgroupB2BWamIntegrationService.getAuthTokenPOST();
        //Set in session the new token
        if(!StringUtils.isEmpty(authToken)) {
            session.setAttribute(WAM_TOKEN, authToken);
        }
        return authToken;
    }

    public DefaultSolgroupB2BWamIntegrationService getSolgroupB2BWamIntegrationService() {
        return solgroupB2BWamIntegrationService;
    }

    public void setSolgroupB2BWamIntegrationService(DefaultSolgroupB2BWamIntegrationService solgroupB2BWamIntegrationService) {
        this.solgroupB2BWamIntegrationService = solgroupB2BWamIntegrationService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

}
