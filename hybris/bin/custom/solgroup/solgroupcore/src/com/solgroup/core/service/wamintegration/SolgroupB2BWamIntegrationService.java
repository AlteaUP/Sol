package com.solgroup.core.service.wamintegration;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface SolgroupB2BWamIntegrationService {

    /**
     * WAM Integration REST API POST
     * Used to retrieve authentication token to use on subsequent calls.
     *
     * @return String
     */
    public String getAuthTokenPOST();

    /**
     * WAM Integration REST API POST
     * Used to retrieve all documents list of a certain type for current "cliente_codice"
     *
     * @param authToken
     * @param companyCode
     * @param customerCode
     * @param documentsType
     * @param pageNumber
     * @param rows
     * @param date
     * @param startDate
     * @return ResponseEntity<String>
     */
    public ResponseEntity<String> getDocumentsListPOST(String authToken, String companyCode, String customerCode, String documentsType, Integer pageNumber, Integer rows, LocalDate date, LocalDate startDate);

    /**
     * WAM Integration REST API POST
     * Used to download document by id
     *
     * @param authToken
     * @param documentId
     * @param documentKey
     * @param documentType
     * @return ResponseEntity<String>
     */
    public ResponseEntity<String> downloadDocumentPOST(String authToken, String documentId, String documentKey, String documentType);
}
