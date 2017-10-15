package com.yh.qa.entity;

/**
 * @author panmiaomiao
 *
 * @date 2017年9月29日
 */

// 用户登录后相关信息，后面如果有需要其他信息再加入这个class里面
public class UserInfo {
	// 余额(单位为分)
	private int balance;
	// 积分
	private Double credit;
	// 会员id
	private String uId;
	//会员手机号
	private String mobile;
	//access_token
	private String access_token;
	//可用优惠卷数量
	private int curretCouponNum;
	//历史优惠卷数量
	private int historyCouponNum;
	//所有订单的数量
	private int num;
	// 待评价订单数量
	private int toComment;
	// 待配送订单数量
	private int toDelivery;
	// 待支付订单数量
	private int toPay;
	// 待自提订单数量
	private int toPickup;

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public String getUId() {
		return uId;
	}

	public void setUId(String uId) {
		this.uId = uId;
	}

	public int getToComment() {
		return toComment;
	}

	public void setToComment(int toComment) {
		this.toComment = toComment;
	}

	public int getToDelivery() {
		return toDelivery;
	}

	public void setToDelivery(int toDelivery) {
		this.toDelivery = toDelivery;
	}

	public int getToPay() {
		return toPay;
	}

	public void setToPay(int toPay) {
		this.toPay = toPay;
	}

	public int getToPickup() {
		return toPickup;
	}

	public void setToPickup(int toPickup) {
		this.toPickup = toPickup;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public int getCurretCouponNum() {
		return curretCouponNum;
	}

	public void setCurretCouponNum(int curretCouponNum) {
		this.curretCouponNum = curretCouponNum;
	}

	public int getHistoryCouponNum() {
		return historyCouponNum;
	}

	public void setHistoryCouponNum(int historyCouponNum) {
		this.historyCouponNum = historyCouponNum;
	}

	public void setCredit(Double credit) {
		this.credit = credit;
	}

	public Double getCredit() {
		return credit;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}
	
}