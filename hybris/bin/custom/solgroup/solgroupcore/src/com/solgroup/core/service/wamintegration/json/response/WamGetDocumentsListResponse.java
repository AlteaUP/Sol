package com.solgroup.core.service.wamintegration.json.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Id",
        "Recordcount",
        "Fields"
})
public class WamGetDocumentsListResponse {

    @JsonProperty("Id")
    private int id;
    @JsonProperty("Recordcount")
    private int recordcount;
    @JsonProperty("Fields")
    private List<Field> fields = null;

    @JsonProperty("Id")
    public int getId() {
        return id;
    }

    @JsonProperty("Id")
    public void setId(int id) {
        this.id = id;
    }

    @JsonProperty("Recordcount")
    public int getRecordcount() {
        return recordcount;
    }

    @JsonProperty("Recordcount")
    public void setRecordcount(int recordcount) {
        this.recordcount = recordcount;
    }

    @JsonProperty("Fields")
    public List<Field> getFields() {
        return fields;
    }

    @JsonProperty("Fields")
    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

}