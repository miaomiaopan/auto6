package com.yh.qa.entity;

import org.apache.commons.lang3.StringUtils;

/**
 * @author panmiaomiao
 *
 * @date 2017年10月9日
 */
// 管家获取订单详情接口中status的枚举
public enum GJOrderStatus {
	PENDING(10, "待确认"), 
	WAITING_THIRDPARTY_TAKE(11, "等待第三方接单"), 
	WAITING_ASSIGN(6, "等待分配"), 
	WAITING_TAKE(1,"等待接单"), 
	TAKE(2, "接单"), 
	START_PACK(8, "开始拣货"), 
	READY_TO_PICKUP(3, "等待提货"), 
	PICKUP(4, "提货"), 
	COMPLETE(5,"已核销、已部分退款"),
	SELF_PICKUP(12, "等待自提"), 
	REFUNDING(100, "退款审核中"), 
	RETURNING(101,"退货审核中"),
	ORDER_RETURNED(7, "订单退款或退货"), 
	PENDING_SHOP_SIGN_AFTER_THIRDPARTY(9, "等待门店签收");

	int index;
	String description;

	GJOrderStatus(int index, String description) {
		this.index = index;
		this.description = description;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static GJOrderStatus getGJOrderStatusByCode(int key){
		for(GJOrderStatus s: GJOrderStatus.values()){
			if(s.getIndex() == key){
				return s;
			}
		}
		return null;
	}

}
