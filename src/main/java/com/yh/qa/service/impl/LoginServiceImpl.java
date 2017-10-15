package com.yh.qa.service.impl;

import com.yh.qa.dao.UserDao;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.UserService;
import com.yh.qa.util.ResultBean;
import com.yh.qa.util.Schema;
import com.yh.qa.util.ValidateUtil;

import io.restassured.path.json.JsonPath;

import com.yh.qa.util.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("loginService")
public class LoginServiceImpl extends HttpServiceImpl implements LoginService {

	@Value("${domain-shenghuo}")
	private String domainShengHuo;
	
	@Value("${domain-guanjia}")
	private String domainGuanJia;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserDao userDao;

	@Override
	public JsonPath loginSH(String query, String body, int code) throws Exception {
		ResultBean result = post(domainShengHuo + Path.SH_LOGIN + query, body, Schema.SH_LOGIN_SCHEMA);
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath loginOutSH(String query, int code) throws Exception {
		ResultBean result = get(domainShengHuo + Path.SH_LOGINOUT + query, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath loginGJ(String query, String body, int code) throws Exception {
		ResultBean result =  post(domainGuanJia + Path.GJ_LOGIN + query, body, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public UserInfo loginSHAndGetUserInfo(String query, String body, int code) throws Exception {
		JsonPath jsonPath = loginSH(query,body, code);
		String access_token = jsonPath.getString("access_token");
		String uId = jsonPath.getString("uid");
		query = "?channel=qa3&deviceid=864854034674759&platform=Android&timestamp=1506732324550&v=4.2.2.2&access_token="+access_token;
		jsonPath = userService.info(query, code);
		UserInfo userInfo = new UserInfo();
		userInfo.setBalance(jsonPath.getInt("balance"));
		userInfo.setAccess_token(access_token);
        userInfo.setCredit(jsonPath.getDouble("credit"));
        userInfo.setCurretCouponNum(jsonPath.getInt("coupon"));
        userInfo.setMobile(jsonPath.getString("mobile"));
        userInfo.setToComment(jsonPath.getInt("toComment"));
        userInfo.setToDelivery(jsonPath.getInt("toDelivery"));
        userInfo.setToPay(jsonPath.getInt("toPay"));
        userInfo.setToPickup(jsonPath.getInt("toPickup"));
        userInfo.setNum(userInfo.getToComment()+userInfo.getToDelivery()+userInfo.getToPay()+userInfo.getToPickup());
        userInfo.setUId(uId);
        //调用接口从数据库获取精确积分
        userInfo.setCredit(userDao.getCreditByUid(uId));        
        return userInfo;
	}

}
