package com.solgroup.core.service.wamintegration.json.request;

import org.codehaus.jackson.annotate.JsonProperty;

public class WamGetAuthTokenRequest {

    @JsonProperty("Username")
    private String username;
    @JsonProperty("Password")
    private String password;
    @JsonProperty("ServiceName")
    private String serviceName;

    public WamGetAuthTokenRequest(){
        //Intentionally left empty
    }

    public WamGetAuthTokenRequest(String username, String password, String serviceName) {
        this.username = username;
        this.password = password;
        this.serviceName = serviceName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
