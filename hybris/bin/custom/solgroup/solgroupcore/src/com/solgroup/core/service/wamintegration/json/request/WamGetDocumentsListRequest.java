package com.solgroup.core.service.wamintegration.json.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.Generated;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "TokenValue",
        "ServiceName",
        "DataObject",
        "Filters"
})
@Generated("jsonschema2pojo")
public class WamGetDocumentsListRequest {

    @JsonProperty("TokenValue")
    private String tokenValue;
    @JsonProperty("ServiceName")
    private String serviceName;
    @JsonProperty("DataObject")
    private String dataObject;
    @JsonProperty("Page")
    private Integer page;
    @JsonProperty("Rows")
    private Integer rows;
    @JsonProperty("Filters")
    private List<Filter> filters;

    @JsonProperty("TokenValue")
    public String getTokenValue() {
        return tokenValue;
    }

    @JsonProperty("TokenValue")
    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    @JsonProperty("ServiceName")
    public String getServiceName() {
        return serviceName;
    }

    @JsonProperty("ServiceName")
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @JsonProperty("DataObject")
    public String getDataObject() {
        return dataObject;
    }

    @JsonProperty("DataObject")
    public void setDataObject(String dataObject) {
        this.dataObject = dataObject;
    }

    @JsonProperty("Filters")
    public List<Filter> getFilters() {
        return filters;
    }

    @JsonProperty("Filters")
    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    @JsonProperty("Page")
    public Integer getPage() {
        return page;
    }

    @JsonProperty("Page")
    public void setPage(Integer page) {
        this.page = page;
    }

    @JsonProperty("Rows")
    public Integer getRows() {
        return rows;
    }

    @JsonProperty("Rows")
    public void setRows(Integer rows) {
        this.rows = rows;
    }
}