package com.yh.qa.service;

import com.yh.qa.util.ResultBean;

public interface HttpService {


    ResultBean post(String url, String paramBody, String verifyJsonResultSchema) throws Exception;

    ResultBean get(String url, String verifyJsonResultSchema) throws Exception;
}