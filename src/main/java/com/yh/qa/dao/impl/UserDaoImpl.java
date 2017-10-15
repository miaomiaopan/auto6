package com.yh.qa.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.yh.qa.dao.UserDao;
import com.yh.qa.datasource.DataSourceTemplete;

@Service("UserDao")
public class UserDaoImpl implements UserDao{
	@Autowired
	@Qualifier(DataSourceTemplete.CREDIT_CENTER)
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public Double getCreditByUid(String uId) {
		Double credit = 0d;
		try {
			credit = jdbcTemplate.queryForObject("select available_credit from yh_credit_account where member_id = " + uId, Double.class);
		}catch (Exception e){

		}
		return credit;
	}
}
