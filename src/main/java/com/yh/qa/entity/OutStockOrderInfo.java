package com.yh.qa.entity;

/**
 * 需要打包、出库时用到的order信息
 * @author matt
 */
public class OutStockOrderInfo {
    private String orderId;
    private String skuCode;
    private Double salePrice;
    private Double qty;
    private String develiryId;
    private String develiryCode;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public Double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice = salePrice;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public String getDeveliryId() {
        return develiryId;
    }

    public void setDeveliryId(String develiryId) {
        this.develiryId = develiryId;
    }

    public String getDeveliryCode() {
        return develiryCode;
    }

    public void setDeveliryCode(String develiryCode) {
        this.develiryCode = develiryCode;
    }

    @Override
    public String toString() {
        return "OutStockOrderInfo{" +
                "orderId='" + orderId + '\'' +
                ", skuCode='" + skuCode + '\'' +
                ", salePrice=" + salePrice +
                ", qty=" + qty +
                ", develiryId='" + develiryId + '\'' +
                ", develiryCode='" + develiryCode + '\'' +
                '}';
    }
}
