package com.solgroup.core.service.wamintegration.json.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "TokenValue",
        "ID",
        "DataObject",
        "Key"
})
public class WamDownloadDocumentRequest {

    @JsonProperty("TokenValue")
    private String tokenValue;
    @JsonProperty("ID")
    private String id;
    @JsonProperty("DataObject")
    private String dataObject;
    @JsonProperty("Key")
    private String key;

    @JsonProperty("TokenValue")
    public String getTokenValue() {
        return tokenValue;
    }

    @JsonProperty("TokenValue")
    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    @JsonProperty("ID")
    public String getId() {
        return id;
    }

    @JsonProperty("ID")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("DataObject")
    public String getDataObject() {
        return dataObject;
    }

    @JsonProperty("DataObject")
    public void setDataObject(String dataObject) {
        this.dataObject = dataObject;
    }

    @JsonProperty("Key")
    public String getKey() {
        return key;
    }

    @JsonProperty("Key")
    public void setKey(String key) {
        this.key = key;
    }

}