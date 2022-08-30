package com.solgroup.core.service.wamintegration.json.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "FileName",
        "FileBase64",
        "ErrorCode",
        "ErrorMessage"
})
public class WamDownloadDocumentResponse {

    @JsonProperty("FileName")
    private String fileName;
    @JsonProperty("FileBase64")
    private String fileBase64;
    @JsonProperty("ErrorCode")
    private String errorCode;
    @JsonProperty("ErrorMessage")
    private String errorMessage;

    @JsonProperty("FileName")
    public String getFileName() {
        return fileName;
    }

    @JsonProperty("FileName")
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @JsonProperty("FileBase64")
    public String getFileBase64() {
        return fileBase64;
    }

    @JsonProperty("FileBase64")
    public void setFileBase64(String fileBase64) {
        this.fileBase64 = fileBase64;
    }

    @JsonProperty("ErrorCode")
    public String getErrorCode() {
        return errorCode;
    }

    @JsonProperty("ErrorCode")
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @JsonProperty("ErrorMessage")
    public String getErrorMessage() {
        return errorMessage;
    }

    @JsonProperty("ErrorMessage")
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}