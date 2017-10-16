package com.yh.qa.entity;

public enum SHOrderStatus {
    STARTPROCESS(15, "待加工"),
    FINISHPROCESS(4, "待取餐"),
    FINISH(5, "已完成");


    int index;
    String description;

    SHOrderStatus(int index, String description) {
        this.index = index;
        this.description = description;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static SHOrderStatus getGJOrderStatusByCode(int key){
        for(SHOrderStatus s: SHOrderStatus.values()){
            if(s.getIndex() == key){
                return s;
            }
        }
        return null;
    }

}

