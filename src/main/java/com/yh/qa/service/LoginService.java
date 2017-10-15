package com.yh.qa.service;

import com.yh.qa.entity.UserInfo;

import io.restassured.path.json.JsonPath;

/**
 * @author 许崇英
 * @since 2017/9/1
 */
public interface LoginService {

	//生活APP登录
    JsonPath loginSH(String query,String body, int code) throws Exception;
    
    //生活APP登录后并且获取个人中心信息
    UserInfo loginSHAndGetUserInfo(String query,String body,  int code) throws Exception;
    
    //生活APP登出
    JsonPath loginOutSH(String query, int code) throws Exception;
    
    //管家APP登录
    JsonPath loginGJ(String query,String body, int code) throws Exception;

}
