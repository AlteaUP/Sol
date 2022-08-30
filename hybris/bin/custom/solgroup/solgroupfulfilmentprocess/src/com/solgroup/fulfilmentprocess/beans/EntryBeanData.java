package com.solgroup.fulfilmentprocess.beans;

public class EntryBeanData {

    private String productCode;
    private int orderQty;
    private int deliveredQty;
    private int cancelledQty;
    private boolean qtyCompleted;

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getOrderQty() {
        return orderQty;
    }

    public void setOrderQty(int orderQty) {
        this.orderQty = orderQty;
    }

    public int getDeliveredQty() {
        return deliveredQty;
    }

    public void setDeliveredQty(int deliveredQty) {
        this.deliveredQty = deliveredQty;
        qtyCompleted = (deliveredQty+cancelledQty)==orderQty;
    }

    public int getCancelledQty() {
        return cancelledQty;
    }

    public void setCancelledQty(int cancelledQty) {
        this.cancelledQty = cancelledQty;
        qtyCompleted = (deliveredQty+cancelledQty)==orderQty;
    }

    public boolean isQtyCompleted() {
        return qtyCompleted;
    }
}
