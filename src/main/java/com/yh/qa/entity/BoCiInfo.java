package com.yh.qa.entity;

/**
 * @author panmiaomiao
 *
 * @date  2017年10月11日
 */
public class BoCiInfo {
	private long expecttime;
	private int itemcount;
	private int itemfreshcount;
	private int itemfrozencount;
	private int lackcount;
	private int mergeflag;
	private int ordercount;
	private int ordertype;
	private String pickdisplayno;
	private int pickwaveid;
	private long servertime;
	private int status;

	public long getExpecttime() {
		return expecttime;
	}

	public void setExpecttime(long expecttime) {
		this.expecttime = expecttime;
	}

	public int getItemcount() {
		return itemcount;
	}

	public void setItemcount(int itemcount) {
		this.itemcount = itemcount;
	}

	public int getItemfreshcount() {
		return itemfreshcount;
	}

	public void setItemfreshcount(int itemfreshcount) {
		this.itemfreshcount = itemfreshcount;
	}

	public int getItemfrozencount() {
		return itemfrozencount;
	}

	public void setItemfrozencount(int itemfrozencount) {
		this.itemfrozencount = itemfrozencount;
	}

	public int getLackcount() {
		return lackcount;
	}

	public void setLackcount(int lackcount) {
		this.lackcount = lackcount;
	}


	public int getMergeflag() {
		return mergeflag;
	}

	public void setMergeflag(int mergeflag) {
		this.mergeflag = mergeflag;
	}

	public int getOrdercount() {
		return ordercount;
	}

	public void setOrdercount(int ordercount) {
		this.ordercount = ordercount;
	}

	public int getOrdertype() {
		return ordertype;
	}

	public void setOrdertype(int ordertype) {
		this.ordertype = ordertype;
	}

	public String getPickdisplayno() {
		return pickdisplayno;
	}

	public void setPickdisplayno(String pickdisplayno) {
		this.pickdisplayno = pickdisplayno;
	}

	public int getPickwaveid() {
		return pickwaveid;
	}

	public void setPickwaveid(int pickwaveid) {
		this.pickwaveid = pickwaveid;
	}

	public long getServertime() {
		return servertime;
	}

	public void setServertime(long servertime) {
		this.servertime = servertime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
