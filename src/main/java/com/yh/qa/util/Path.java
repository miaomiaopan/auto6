package com.yh.qa.util;

public class Path {
	// 生活登录
	public static String SH_LOGIN = "/api/member/signIn";

	// 生活登出
	public static String SH_LOGINOUT = "/api/member/loginout";

	// 生活APP下单
	public static String SH_ORDER_CONFIRM = "/api/order/confirm";

	// 生活APP获取订单详情
	public static String SH_ORDER_LIST = "/api/order/list";

	// 生活APP获取订单详情
	public static String SH_ORDER_DETAIL = "/api/order/detail";

	// 管家APP获取订单详情
	public static String GJ_ORDER_DETAIL = "/orderdetail";

	// 获取永辉会员卡信息
	public static String SH_CARDINFO = "/api/asset/cardinfo";

	// 上传定位
	public static String LOCATION = "/user/location";

	// 永辉生活app获取个人中心信息
	public static String SH_INFO = "/api/asset/info";

	// 管家登录
	public static String GJ_LOGIN = "/login";

	// 管家登出(抓包没有登出接口)
	// public static String SH_LOGINOUT = "api/member/loginout";

	// 履单
	public static String ORDERACTION = "/orderaction";

	// 管家 签收包裹
	public static String SIGNPACKAGE = "/batchsignpackage";

	// 管家 店长派单 配送员：接包裹，提包裹，核销包裹
	public static String PACKAGEACTION = "/packageaction";

	// 获取管家集波拣货待拣货波次
	public static String WAITINGPACK = "/pickwave/waitingpack";

	// 获取管家集波拣货待拣货波次
	public static String MERGEGROUP = "/pickwave/mergegroup";

	// 管家集波拣货开始拣货
	public static String STSRTPACK = "/pickwave/startpack";

	// 管家集波拣货完成拣货
	public static String COMPLETEPACK = "/pickwave/completePack";

	// 整批提货
	public static String BATCHACTION = "/order/batchaction";

	// 打包
	public static String Packing = "/api/yunqing/outstock/feedback";

	// 出库， 生成物流单
	public static String OUTSTACK = "/adapter-rest/api/distributionFeedback";

	// 登记缺货
	public static String OUTOFSTOCK = "/order/lack2";

	// 售后单退款
	public static String ACTION = "/serviceorders/action";

	// 管家中配送员登记用户拒收订单
	public static String PARTIALRETURN = "/order/partialreturn";

	// 扫描悬挂袋
	public static String MERGINGSCAN = "/merging/scan";

	// 绑定悬挂袋
	public static String MERGINGBIND = "/merging/bind";

	// 合单入周转箱
	public static String MERGINGINBOX = "/merging/inbox";

	//生活端申请退款
	public static String APPLYREFUND = "/api/order/applyrefund";

	//包裹详情
	public static String PACKAGEDETAIL = "/packagedetail";

	//KDS 获取批次
	public static String GETPROCESSORDERLIST = "/kds/getWaitProcessListBatch";

	//KDS 确定批次
	public static String CONFIRMORDER = "/kds/confirmWaitProcessListBatch";

	//KDS 开始加工
	public static String BEGINPROCESSORDER = "/kds/startProcess";

	//KDS 完成加工
	public static String FINISHPROCESSORDER = "/kds/finishProcess";

	//KDS 确定批次
	public static String WEIGHING = "/kds/confirmWeightG";

	//KDS 自提
	public static String SELFPICK = "/kds/selfPick";

	// 管家核销拣货单
	public static String PICKORDERCOMPLETE = "/order/pickorder/complete";

}
