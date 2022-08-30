
package com.solgroup.core.service.wamintegration.json.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Field",
        "Operator",
        "FilterValue"
})
public class Filter {

    @JsonProperty("Field")
    private String field;
    @JsonProperty("Operator")
    private String operator;
    @JsonProperty("FilterValue")
    private String filterValue;

    public Filter(String field, String operator, String filterValue) {
        this.field = field;
        this.operator = operator;
        this.filterValue = filterValue;
    }

    @JsonProperty("Field")
    public String getField() {
        return field;
    }

    @JsonProperty("Field")
    public void setField(String field) {
        this.field = field;
    }

    @JsonProperty("Operator")
    public String getOperator() {
        return operator;
    }

    @JsonProperty("Operator")
    public void setOperator(String operator) {
        this.operator = operator;
    }

    @JsonProperty("FilterValue")
    public String getFilterValue() {
        return filterValue;
    }

    @JsonProperty("FilterValue")
    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

}