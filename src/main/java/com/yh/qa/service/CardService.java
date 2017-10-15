package com.yh.qa.service;

import io.restassured.path.json.JsonPath;

public interface CardService {
	/*
	 * 获取永辉生活会员卡信息
	 */
	JsonPath cardInfo(String query, int code) throws Exception;
}
