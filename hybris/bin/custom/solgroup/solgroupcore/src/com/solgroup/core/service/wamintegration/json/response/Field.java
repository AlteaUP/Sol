package com.solgroup.core.service.wamintegration.json.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Key",
        "Value",
        "Fields"
})
@Generated("jsonschema2pojo")
public class Field {

    @JsonProperty("Key")
    private String key;
    @JsonProperty("Value")
    private String value;
    @JsonProperty("Fields")
    private Object fields;

    @JsonProperty("Key")
    public String getKey() {
        return key;
    }

    @JsonProperty("Key")
    public void setKey(String key) {
        this.key = key;
    }

    @JsonProperty("Value")
    public String getValue() {
        return value;
    }

    @JsonProperty("Value")
    public void setValue(String value) {
        this.value = value;
    }

    @JsonProperty("Fields")
    public Object getFields() {
        return fields;
    }

    @JsonProperty("Fields")
    public void setFields(Object fields) {
        this.fields = fields;
    }

}