package com.yh.qa.service;

import io.restassured.path.json.JsonPath;

import java.util.Map;

public interface KDSService {
    //获取加工单列表
    JsonPath getProcessOrderList(Map<String, String> queryPara, int code) throws Exception;

    //确认加工单
    JsonPath confirmOrder(Map<String, String> queryPara, String body, int code) throws Exception;

    //开始加工单
    JsonPath beginProcessOrder(Map<String, String> queryPara, String body, int code) throws Exception;

    //完成加工单
    JsonPath finishProcessOrder(Map<String, String> queryPara,String body, int code) throws Exception;

    //称重
    public JsonPath weighing(Map<String, String> queryPara, String body, int code) throws Exception;

    //完成加工单
    JsonPath pickUpOrder(Map<String, String> queryPara,String body, int code) throws Exception;
}
