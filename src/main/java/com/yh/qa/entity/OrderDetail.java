package com.yh.qa.entity;

/**
 * @author panmiaomiao
 *
 * @date 2017年10月13日
 */
// 用于计算积分明细
public class OrderDetail {
	private Double quantity;
	private Double price;
	
	public OrderDetail(Double quantity, Double price){
		this.quantity = quantity;
		this.price = price;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

}
