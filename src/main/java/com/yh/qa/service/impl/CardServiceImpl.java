package com.yh.qa.service.impl;

import com.yh.qa.service.CardService;
import com.yh.qa.util.ResultBean;
import com.yh.qa.util.ValidateUtil;

import io.restassured.path.json.JsonPath;

import com.yh.qa.util.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("cardService")
public class CardServiceImpl extends HttpServiceImpl implements CardService {
	
	@Value("${domain-shenghuo}")
	private String domainShengHuo;
	
	@Override
	public JsonPath cardInfo(String query, int code) throws Exception {
		ResultBean result = get(domainShengHuo + Path.SH_CARDINFO + query, "");
		return ValidateUtil.validateCode(result, code);
	}

}
