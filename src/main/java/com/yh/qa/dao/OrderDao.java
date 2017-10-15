package com.yh.qa.dao;

import com.yh.qa.entity.OrderDb;
import com.yh.qa.entity.OutStockOrderInfo;

import java.util.List;
import java.util.Map;

public interface OrderDao {
	// 根据orderId删除生成订单相关数据
	void deleteById(String orderId);

	// 根据父订单获取子订单的出库订单号，付订单号，商品编号
	List<OrderDb> getOutStockOrderForChildByParentOrder(String parentOrderId);

	// 根据订单获取出库号、物流、数量、价格等信息
	OutStockOrderInfo getOutStockOrderInfo(String orderId);

	// 根据波次Id获取对应的订单id
	List<String> getOrderIdsByPickWaveids(List<String> waveids);

	// 根据订单id获取批次id
	List<String> getBatchIdsByOrderIds(List<String> orderIds);

	// 根据批次id获取Map<批次id,批次id对应的订单id的list>
	Map<String, List<String>> getMapByBatchIds(List<String> batchIds);

	// 缺货拆单，根据父订单号，查询子订单ID -- Owin
	List<String> getSplitInfoByParentsOrderID(List<String> orderIds);

	// 根据orderId和sku获取itemId
	String getItemIdByOrderIdAndSku(String orderId, String sku);

	// 根据orderId获取service_order_id
	String getServiceOrderIdByOrderId(String orderId);
	
	// 缺货拆单，根据父订单号，查询子订单ID
    List<String> getSubOrderIdByParentOrderID(String orderId);
    
	// 缺货拆单，根据订单号查询订单状态
    String getStatusByOrderId(String orderId);

	//根据orderId获取波次id
	String getWaveIdByOrderId(String orderId);
}
