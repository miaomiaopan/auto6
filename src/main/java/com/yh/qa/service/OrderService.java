package com.yh.qa.service;

import io.restassured.path.json.JsonPath;

public interface OrderService {
	// 永辉生活APP确认订单
	JsonPath confirm(String query, String body, int code) throws Exception;

	// 管家履单
	JsonPath orderAction(String query, String body, int code) throws Exception;

	// 管家 签收包裹
	JsonPath signPackage(String query, String body, int code) throws Exception;

	// 管家 店长派单
	JsonPath packageAction(String query, String body, int code) throws Exception;

	// 永辉生活查看订单详情接口
	JsonPath detail(String query, int code) throws Exception;

	// 永辉生活查看订单详情接口
	JsonPath orderList(String query, int code) throws Exception;

	// 永辉管家查看订单详情接口
	JsonPath detailGj(String query, int code) throws Exception;

    // 永辉管家查看包裹详情接口
    JsonPath packageDetailGj(String query, int code) throws Exception;

    // 出库
	JsonPath outstock(String body, Boolean flag) throws Exception;

	// 打包
	JsonPath pack(String body, Boolean flag) throws Exception;

	// 获取管家集波拣货待拣货波次
	JsonPath waitingPack(String query, int code) throws Exception;

	// 管家集波拣货开始拣货
	JsonPath startPack(String query, String body, int code) throws Exception;

	// 管家集波拣货完成拣货
	JsonPath completePack(String query, String body, int code) throws Exception;

	// 整批提货
	JsonPath batchAction(String query, String body, int code) throws Exception;

	// 合并波次
	JsonPath mergeGroup(String query, String body, int code) throws Exception;

	// 登记缺货
	JsonPath registerLackSku(String uri, String body, int code) throws Exception;

	// 扫描悬挂袋
	JsonPath mergingScan(String query, String body, int code) throws Exception;

	// 绑定悬挂袋
	JsonPath mergingBind(String query, String body, int code) throws Exception;

	//合单入周转箱
	JsonPath mergingInbox(String query, String body, int code) throws Exception;

	// 售后单操作（1：退款，2：继续履单 ）
	JsonPath action(String query, String body, int code) throws Exception;
	
	//用户拒收订单
	JsonPath partialReturn(String query, String body, int code) throws Exception;

	// 生活app 申请退款
	JsonPath applyRefund(String query, String body, int code) throws Exception;

	//根据orderId获取波次id， 最大等待n分钟
	String getDelayedWaveIdByOrderId(String orderId, int n) throws Exception;

	// 调用管家的订单详情接口获取订单状态
	void validateOrderStatusGJ(String orderId, String accessTokenGJ, String uid, int code, int index, String message) throws Exception;

    // 调用管家的包裹详情接口获取包裹状态
	void validatePackageStatusGJ(String packageCode, String accessTokenGJ, int code, int index, String message) throws Exception;

	// 管家超级物种核销拣货单
	JsonPath pickOrderComplete(String query, String body, int code) throws Exception;

}
