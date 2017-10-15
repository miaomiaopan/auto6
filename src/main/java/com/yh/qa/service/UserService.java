package com.yh.qa.service;


import com.yh.qa.entity.UserInfo;
import io.restassured.path.json.JsonPath;

/**
 * @author panmiaomiao
 *
 * @date 2017年9月30日
 */
public interface UserService {
	// 调用asset/info获取个人中心信息
	JsonPath info(String query, int code) throws Exception;

	UserInfo getInfo(String query, int code) throws Exception;
	
	//上传定位
	JsonPath location(String query, int code) throws Exception;
}
