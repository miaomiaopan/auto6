package com.yh.qa.service.impl;

import com.yh.qa.dao.UserDao;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.UserService;
import com.yh.qa.util.ResultBean;
import com.yh.qa.util.ValidateUtil;

import io.restassured.path.json.JsonPath;

import com.yh.qa.util.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("UserService")
public class UserServiceImpl extends HttpServiceImpl implements UserService {
	@Value("${domain-shenghuo}")
	private String domainShenghuo;
	
	@Value("${domain-guanjia}")
	private String domainGuanJia;

	@Autowired
	private UserDao userDao;

	@Override
	public JsonPath info(String query, int code) throws Exception {
		ResultBean result = get(domainShenghuo + Path.SH_INFO + query, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public UserInfo getInfo(String query, String uid,int code) throws Exception {
		JsonPath jsonPath = info(query, code);
		UserInfo userInfo = new UserInfo();
		userInfo.setBalance(jsonPath.getInt("balance"));
		// 界面上返回的积分数据是进行取整的，不准确，所以修改为从数据库中直接获取精确的积分值
		userInfo.setCredit(userDao.getCreditByUid(uid));
		userInfo.setCurretCouponNum(jsonPath.getInt("coupon"));
		userInfo.setMobile(jsonPath.getString("mobile"));
		userInfo.setToComment(jsonPath.getInt("toComment"));
		userInfo.setToDelivery(jsonPath.getInt("toDelivery"));
		userInfo.setToPay(jsonPath.getInt("toPay"));
		userInfo.setToPickup(jsonPath.getInt("toPickup"));
		userInfo.setNum(
				userInfo.getToComment() + userInfo.getToDelivery() + userInfo.getToPay() + userInfo.getToPickup());

		return userInfo;
	}

	@Override
	public JsonPath location(String query, int code) throws Exception {
		ResultBean result = post(domainGuanJia + Path.LOCATION + query,"", "");
		return ValidateUtil.validateCode(result, code);
	}

}
