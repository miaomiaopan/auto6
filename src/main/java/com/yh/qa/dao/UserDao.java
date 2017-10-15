package com.yh.qa.dao;

public interface UserDao {
	//根据uid获取用户积分
	Double getCreditByUid(String uId);
}
