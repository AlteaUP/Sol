package com.solgroup.storefront.forms;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class SolGroupOrderHistoryFiltersForm {

    private String orderCode;
    private String poNumber;
    private String orderStatus;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date orderDate;
    
    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

}
