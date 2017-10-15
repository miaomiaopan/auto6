package com.yh.qa.entity;

/**
 * @author liuwei
 *
 * @date 2017年10月14日
 */

// 管家 包裹详情接口中status
public enum GJPackageStatus {
	WAITING_TAKE(1,"待接"),
    READY_TO_PICKUP(3, "待提"),
    WAITING_CANCEL_DEBT(4, "待核销"),
    COMPLETE(5,"已核销"),
    WAITING_ASSIGN(6, "等待分配");

	int index;
	String description;

	GJPackageStatus(int index, String description) {
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

}
