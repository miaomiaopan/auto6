package com.yh.qa.entity;

public class OrderDb {
    private String outStockOrderId;//出库订单号
    private String parentOrderId;//父订单号
    private String childOrderId;//子订单号
    private String goodsId;//带商家标识
    private String skuCode;//不带商家标识

    public String getOutStockOrderId() {
        return outStockOrderId;
    }

    public void setOutStockOrderId(String outStockOrderId) {
        this.outStockOrderId = outStockOrderId;
    }

    public String getParentOrderId() {
        return parentOrderId;
    }

    public void setParentOrderId(String parentOrderId) {
        this.parentOrderId = parentOrderId;
    }

    public String getChildOrderId() {
        return childOrderId;
    }

    public void setChildOrderId(String childOrderId) {
        this.childOrderId = childOrderId;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

}
