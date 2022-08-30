package com.solgroup.core.service.wamintegration.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solgroup.core.constants.SolgroupCoreConstants;
import com.solgroup.core.service.wamintegration.SolgroupB2BWamIntegrationService;
import com.solgroup.core.service.wamintegration.WamIntegrationErrorHandler;
import com.solgroup.core.service.wamintegration.json.request.Filter;
import com.solgroup.core.service.wamintegration.json.request.WamDownloadDocumentRequest;
import com.solgroup.core.service.wamintegration.json.request.WamGetAuthTokenRequest;
import com.solgroup.core.service.wamintegration.json.request.WamGetDocumentsListRequest;
import com.solgroup.core.service.wamintegration.json.response.WamGetAuthTokenResponse;
import de.hybris.platform.util.Config;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpRetryException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DefaultSolgroupB2BWamIntegrationService implements SolgroupB2BWamIntegrationService {

    public static final Logger LOG = LogManager.getLogger(DefaultSolgroupB2BWamIntegrationService.class);
    private static final String WAM_GET_TOKEN_URL = "wam.integration.api.getauthtoken.endpoint";
    private static final String WAM_AUTHENTICATION_USER = "wam.integration.api.getauthtoken.username";
    private static final String WAM_AUTHENTICATION_PSW = "wam.integration.api.getauthtoken.password";
    private static final String WAM_GET_DOCUMENTS_LIST_URL = "wam.integration.api.getdocumentslist.endpoint";
    private static final String WAM_DOWNLOAD_DOCUMENT_URL = "wam.integration.api.downloaddocument.endpoint";
    private static final String WAM_SERVICE_NAME = "wam.integration.api.servicename";

    private RestTemplate restClient;

    @Override
    public String getAuthTokenPOST() {
        String authToken = "";
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<String> response;
        String url = Config.getParameter(WAM_GET_TOKEN_URL);
        //Build requestBody using username, password and environment
        WamGetAuthTokenRequest request = new WamGetAuthTokenRequest(Config.getParameter(WAM_AUTHENTICATION_USER),
                Config.getParameter(WAM_AUTHENTICATION_PSW), Config.getParameter(WAM_SERVICE_NAME));
        if(!StringUtils.isEmpty(url)) {
            try {
                URI uri = new URI(url);
                HttpEntity<WamGetAuthTokenRequest> requestEntity = new HttpEntity<>(request, headers);
                response = restClient.exchange(uri, HttpMethod.POST, requestEntity, String.class);
                if (response != null && response.getStatusCode() != null && response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    String responseBodyString = response.getBody();
                    WamGetAuthTokenResponse responseBody =  mapper.readValue(responseBodyString, WamGetAuthTokenResponse.class);
                    authToken = responseBody.getToken();
                    return authToken;
                }
            } catch (URISyntaxException e) {
                LOG.error("Unable to build Uri from wam url due to: " + e.getMessage());
            } catch (RestClientException e) {
                LOG.error("Error on get request to WAM due to: " + e.getMessage());
            } catch (IOException e) {
                LOG.error("Error reading response due to: " + e.getMessage());
            }
        }

        return authToken;
    }

    @Override
    public ResponseEntity<String> getDocumentsListPOST(String authToken, String companyCode, String customerCode, String documentsType, Integer pageNumber, Integer rows, LocalDate startDate, LocalDate endDate) {

        restClient.setErrorHandler(new WamIntegrationErrorHandler());

        ResponseEntity<String> response = null;
        if(!StringUtils.isEmpty(authToken)) {
            //Retrieve documents list from wam REST Api
            String url = Config.getParameter(WAM_GET_DOCUMENTS_LIST_URL);
            if (!StringUtils.isEmpty(url)) {
                try {
                    URI uri = new URI(url);
                    HttpHeaders headers = new HttpHeaders();
                    //Set auth token in headers
                    headers.add(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_VALUE);
                    HttpEntity<WamGetDocumentsListRequest> requestEntity = new HttpEntity<>(
                            buildGetDocumentListRequestBody(authToken, documentsType, companyCode, customerCode, pageNumber, rows, startDate, endDate), headers);
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String requestBodyString = "";
                        requestBodyString = mapper.writeValueAsString(requestEntity.getBody());
                        LOG.debug(requestBodyString);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    response = restClient.exchange(uri, HttpMethod.POST, requestEntity, String.class);
                } catch (URISyntaxException e) {
                    LOG.error("Unable to build Uri from wam url due to: " + e.getMessage());
                } catch (RestClientException e) {
                    LOG.error("Error on post request to WAM due to: " + e.getMessage());
                    if(e.getCause() instanceof HttpRetryException) {
                        return new ResponseEntity<>("", HttpStatus.valueOf(((HttpRetryException)e.getCause()).responseCode()));
                    }
                }
            }
        }
        if(response == null){
            return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (StringUtils.isEmpty(response.getBody())) {
            return new ResponseEntity<>("", response.getStatusCode());
        } else {
            return new ResponseEntity<>(response.getBody(), response.getStatusCode());
        }
    }

    @Override
    public ResponseEntity<String> downloadDocumentPOST(String authToken, String documentId, String documentKey, String documentType) {
        restClient.setErrorHandler(new WamIntegrationErrorHandler());

        ResponseEntity<String> response = null;
        if(!StringUtils.isEmpty(authToken)) {
            //Retrieve documents list from wam REST Api
            String url = Config.getParameter(WAM_DOWNLOAD_DOCUMENT_URL);
            if (!StringUtils.isEmpty(url)) {
                try {
                    URI uri = new URI(url);
                    HttpHeaders headers = new HttpHeaders();
                    //Set auth token in headers
                    headers.add(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON_VALUE);
                    HttpEntity<WamDownloadDocumentRequest> requestEntity = new HttpEntity<>(
                            buildDownloadDocumentRequestBody(authToken, documentId, documentKey, documentType), headers);
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String requestBodyString = "";
                        requestBodyString = mapper.writeValueAsString(requestEntity.getBody());
                        LOG.debug(requestBodyString);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    response = restClient.exchange(uri, HttpMethod.POST, requestEntity, String.class);
                } catch (URISyntaxException e) {
                    LOG.error("Unable to build Uri from wam url due to: " + e.getMessage());
                } catch (RestClientException e) {
                    LOG.error("Error on post request to WAM due to: " + e.getMessage());
                    if(e.getCause() instanceof HttpRetryException) {
                        return new ResponseEntity<>("", HttpStatus.valueOf(((HttpRetryException)e.getCause()).responseCode()));
                    }
                }
            }
        }

        if(response == null){
            return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (StringUtils.isEmpty(response.getBody())) {
            return new ResponseEntity<>("", response.getStatusCode());
        } else {
            return new ResponseEntity<>(response.getBody(), response.getStatusCode());
        }
    }

    private WamGetDocumentsListRequest buildGetDocumentListRequestBody(String authToken, String documentsType, String companyCode, String customerCode, Integer pageNumber, Integer rows, LocalDate startDate, LocalDate endDate){

        final String WAM_GREATER_OPERATOR= "gt";
        final String WAM_LESSER_OPERATOR= "lt";
        final String WAM_EQUALS_OPERATOR= "eq";

        WamGetDocumentsListRequest request = new WamGetDocumentsListRequest();
        request.setTokenValue(authToken);
        request.setServiceName(Config.getParameter(WAM_SERVICE_NAME));
        request.setDataObject(documentsType);
        request.setPage(pageNumber);
        request.setRows(rows);
        List<Filter> filterList = new ArrayList<>();
        //Add search filter for "Codice Societa"
        Filter companyCodeFilter = new Filter(SolgroupCoreConstants.WamConstants.FILTER_FIELD_WAM_T_SOCIETA_CODICE, WAM_EQUALS_OPERATOR, companyCode);
        //Add search filter for "Codice Cliente"
        Filter customerCodeFilter = null;
        if(documentsType.equalsIgnoreCase(SolgroupCoreConstants.WamConstants.WAM_DATA_OBJECT_CONTRACTS)) {
            customerCodeFilter = new Filter(SolgroupCoreConstants.WamConstants.FILTER_FIELD_CLIENTE_CODICE_CONTRACTS, WAM_EQUALS_OPERATOR, customerCode);
        }else{
            customerCodeFilter = new Filter(SolgroupCoreConstants.WamConstants.FILTER_FIELD_CLIENTE_CODICE_NO_CONTRACTS, WAM_EQUALS_OPERATOR, customerCode);
        }
        //Add search filter for "Data"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDateString = startDate.format(formatter);
        String endDateString = endDate.format(formatter);

        Filter startDateFilter = null;
        Filter endDateFilter = null;
        if(documentsType.equalsIgnoreCase(SolgroupCoreConstants.WamConstants.WAM_DATA_OBJECT_INVOICES)){
            startDateFilter = new Filter(SolgroupCoreConstants.WamConstants.FILTER_FIELD_DATA_INVOICES, WAM_GREATER_OPERATOR, startDateString);
            endDateFilter = new Filter(SolgroupCoreConstants.WamConstants.FILTER_FIELD_DATA_INVOICES, WAM_LESSER_OPERATOR, endDateString);
        }else if(documentsType.equalsIgnoreCase(SolgroupCoreConstants.WamConstants.WAM_DATA_OBJECT_BUSINESS_LETTERS)){
            startDateFilter = new Filter(SolgroupCoreConstants.WamConstants.FILTER_FIELD_DATA_BUSINESS_LETTERS, WAM_GREATER_OPERATOR, startDateString);
            endDateFilter = new Filter(SolgroupCoreConstants.WamConstants.FILTER_FIELD_DATA_BUSINESS_LETTERS, WAM_LESSER_OPERATOR, endDateString);
        }else if(documentsType.equalsIgnoreCase(SolgroupCoreConstants.WamConstants.WAM_DATA_OBJECT_DUNNING_LETTERS)){
            startDateFilter = new Filter(SolgroupCoreConstants.WamConstants.FILTER_FIELD_DATA_DUNNING_LETTERS, WAM_GREATER_OPERATOR, startDateString);
            endDateFilter = new Filter(SolgroupCoreConstants.WamConstants.FILTER_FIELD_DATA_DUNNING_LETTERS, WAM_LESSER_OPERATOR, endDateString);
        }else if(documentsType.equalsIgnoreCase(SolgroupCoreConstants.WamConstants.WAM_DATA_OBJECT_DN)){
            startDateFilter = new Filter(SolgroupCoreConstants.WamConstants.FILTER_FIELD_DATA_DN, WAM_GREATER_OPERATOR, startDateString);
            endDateFilter = new Filter(SolgroupCoreConstants.WamConstants.FILTER_FIELD_DATA_DN, WAM_LESSER_OPERATOR, endDateString);
        }else if(documentsType.equalsIgnoreCase(SolgroupCoreConstants.WamConstants.WAM_DATA_OBJECT_CONTRACTS)){
            startDateFilter = new Filter(SolgroupCoreConstants.WamConstants.FILTER_FIELD_DATA_CONTRACTS, WAM_GREATER_OPERATOR, startDateString);
            endDateFilter = new Filter(SolgroupCoreConstants.WamConstants.FILTER_FIELD_DATA_CONTRACTS, WAM_LESSER_OPERATOR, endDateString);
        }

        filterList.add(companyCodeFilter);
        filterList.add(customerCodeFilter);
        filterList.add(startDateFilter);
        filterList.add(endDateFilter);
        request.setFilters(filterList);
        return request;
    }

    private WamDownloadDocumentRequest buildDownloadDocumentRequestBody(String authToken, String documentId, String documentKey, String documentType){
        WamDownloadDocumentRequest request = new WamDownloadDocumentRequest();
        request.setId(documentId);
        request.setKey(documentKey);
        request.setDataObject(documentType);
        request.setTokenValue(authToken);
        return request;
    }

    public RestTemplate getRestClient() {
        return restClient;
    }

    public void setRestClient(RestTemplate restClient) {
        this.restClient = restClient;
    }
}
