package com.yh.qa.testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.dao.OrderDao;
import com.yh.qa.entity.BoCiInfo;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.OrderDetail;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.service.UserService;
import com.yh.qa.util.RandomString;
import com.yh.qa.util.ValidateUtil;

import io.restassured.path.json.JsonPath;

/**
 * @author panmiaomiao
 *
 * @date 2017年10月10日
 */
@SpringBootTest
public class Yh_18 extends BaseTestCase {
	@Autowired
	private LoginService loginService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderDao orderDao;

	@Autowired
	private UserService userService;

	@Test
	public void testHttpPost() throws Exception {
		String query = "";
		String body = "";
		JsonPath jsonPath = null;
		String accessTokenSH = "";
		String accessTokenGJ = "";
		UserInfo userInfo = null;
		List<BoCiInfo> boCiInfos = null;
		List<String> orderIds = new ArrayList<String>();
		List<String> batchIds = null;
		List<String> boCiIds = new ArrayList<String>();
		// 余额
		int balance;
		// 积分
		Double credit;
		// 订单总数
		int num;
		// 待评价订单数量
		int toComment;
		// 待配送订单数量
		int toDelivery;
		// 订单状态
		int status;

		// 测试数据 门店和商品 上海绿标店亚乐城店
		String storeId = "9475";
		String lat = "31.103499";
		String lng = "121.35695";
		String sku = "B-836955";
		Double price = 6.50d;
		Double quantity = 1d;
		Double pricetotal = 0d;

		// 下单用户
		String phonenum = "18729555529";
		String securitycode = "601933";

		// 拣货员
		String jhy = "9475jhy";
		String password = "123456a";

		// 自营配送员
		String zpsy = "9475zpsy1";

		try {
			// 设置case名称
			testcase.setTestName("Bravo绿标集波履单，自配送正常履单");

			// case开始执行
			// 登录永辉生活app
			query = "?platform=ios";
			body = "{\"phonenum\": \"" + phonenum + "\", \"securitycode\": \"" + securitycode + "\"}";
			userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
			accessTokenSH = userInfo.getAccess_token();
			balance = userInfo.getBalance();
			credit = userInfo.getCredit();
			num = userInfo.getNum();
			toComment = userInfo.getToComment();
			toDelivery = userInfo.getToDelivery();

			// 生成bravo绿标店当日达配送订单2单，波次接受订单的最大数量设置为1，生成两个波次，合并两个波次为一个波次，两个订单将会生成2个自定义批次或者一个规划批次，被唯一的自配送员进行配送流程
			query = "?channel=qa3&deviceid=000000000000000&platform=Android&v=4.2.2.1&access_token=" + accessTokenSH
					+ "&timestamp=" + System.currentTimeMillis();
			body = "{\"balancepayoption\":1,\"device_info\":\"864854034674759\",\"freedeliveryoption\":1,\"paypasswordtype\":0,\"pickself\":0,\"pointpayoption\":0,\""
					+ pricetotal + "\":0,\"products\":[{\"id\":\"" + sku + "\",\"isbulkitem\":0,\"num\":"
					+ quantity * 100
					+ ",\"pattern\":\"t\"}],\"recvinfo\":{\"address\":{\"area\":\"中国移动亚乐城\",\"city\":\"上海\",\"cityid\":\"1\",\"detail\":\"11\"},\"alias\":\"公司\",\"foodsupport\":0,\"id\":\"32312\",\"isSearch\":false,\"isdefault\":1,\"itemType\":0,\"location\":{\"lat\":\""
					+ lat + "\",\"lng\":\"" + lng
					+ "\"},\"name\":\"小潘\",\"nextdaydeliver\":0,\"phone\":\"18729552102\",\"scope\":0},\"sellerid\":3,\"storeid\":\""
					+ storeId + "\",\"texpecttime\":{\"date\":"
					+ (System.currentTimeMillis() / 86400000 * 86400000 - 28800000)
					+ ",\"timeslots\":[{\"immediatedesc\":\"60分钟达\",\"slottype\":\"immediate\"}]},\"totalpayment\":0,\"uid\":\"836248696765411806\"}";
			jsonPath = orderService.confirm(query, body, 0);
			orderIds.add(jsonPath.getString("orderid"));

			Thread.sleep(60000);

			query = "?channel=qa3&deviceid=000000000000000&platform=Android&v=4.2.2.1&access_token=" + accessTokenSH
					+ "&timestamp=" + System.currentTimeMillis();
			body = "{\"balancepayoption\":1,\"device_info\":\"864854034674759\",\"freedeliveryoption\":1,\"paypasswordtype\":0,\"pickself\":0,\"pointpayoption\":0,\""
					+ pricetotal + "\":0,\"products\":[{\"id\":\"" + sku + "\",\"isbulkitem\":0,\"num\":"
					+ quantity * 100
					+ ",\"pattern\":\"t\"}],\"recvinfo\":{\"address\":{\"area\":\"中国移动亚乐城\",\"city\":\"上海\",\"cityid\":\"1\",\"detail\":\"11\"},\"alias\":\"公司\",\"foodsupport\":0,\"id\":\"32312\",\"isSearch\":false,\"isdefault\":1,\"itemType\":0,\"location\":{\"lat\":\""
					+ lat + "\",\"lng\":\"" + lng
					+ "\"},\"name\":\"小潘\",\"nextdaydeliver\":0,\"phone\":\"18729552102\",\"scope\":0},\"sellerid\":3,\"storeid\":\""
					+ storeId + "\",\"texpecttime\":{\"date\":"
					+ (System.currentTimeMillis() / 86400000 * 86400000 - 28800000)
					+ ",\"timeslots\":[{\"immediatedesc\":\"60分钟达\",\"slottype\":\"immediate\"}]},\"totalpayment\":0,\"uid\":\"836248696765411806\"}";
			jsonPath = orderService.confirm(query, body, 0);
			orderIds.add(jsonPath.getString("orderid"));
			
			Thread.sleep(10000);

			// 重新登录永辉生活APP刷新用户信息
			query = "?platform=ios";
			body = "{\"phonenum\": \"" + phonenum + "\", \"securitycode\": \"" + securitycode + "\"}";
			userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
			accessTokenSH = userInfo.getAccess_token();

			Assert.isTrue(userInfo.getBalance() + (price * quantity + 6) * 2 * 100 == balance, "下单支付后用户余额减少数额错误");
			Assert.isTrue(userInfo.getNum() - 2 == num, "下单支付后订单总数没有加2");
			System.out.println(userInfo.getToDelivery());
//			Assert.isTrue(userInfo.getToDelivery() - 2 == toDelivery, "下单后待配送订单总数没有加2");

			// 使用拥有bravo拣货员角色的账号登录管家APP进行拣货
			query = "?platform=android";
			body = "{\"pwd\": \"" + password + "\", \"username\": \"" + jhy + "\"}";
			jsonPath = loginService.loginGJ(query, body, 0);
			accessTokenGJ = jsonPath.getString("token");

			// 调用管家的订单详情接口获取订单状态
			for (String orderId : orderIds) {
				query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id="
						+ userInfo.getUId();
				jsonPath = orderService.detailGj(query, 0);
				status = jsonPath.getInt("status");
				Assert.isTrue(status == GJOrderStatus.PENDING.getIndex(), "下单完成后订单状态不是待确认状态");
			}

			Thread.sleep(60000 * 2);

			// 获取待接单的波次号信息
			query = "?page=0&filter=0&timestamp=" + System.currentTimeMillis()
					+ "&platform=Android&channel=qa3&access_token=" + accessTokenGJ;
			jsonPath = orderService.waitingPack(query, 0);
			boCiInfos = jsonPath.getList("orders", BoCiInfo.class);
			for (BoCiInfo entity : boCiInfos) {
				boCiIds.add(String.valueOf(entity.getPickwaveid()));
			}

			// 接单
			for (String waveId : boCiIds) {
				query = "/" + waveId + "?" + "waveId=" + waveId + "&access_token=" + accessTokenGJ + "&timestamp="
						+ System.currentTimeMillis() + "&platform=ios&channel=qa3";
				body = "{\"waveId\": \"" + waveId + "\"}";
				jsonPath = orderService.startPack(query, body, 0);
			}

			// 调用管家的订单详情接口获取订单状态
			for (String orderId : orderIds) {
				query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id="
						+ userInfo.getUId();
				jsonPath = orderService.detailGj(query, 0);
				status = jsonPath.getInt("status");
				Assert.isTrue(status == GJOrderStatus.START_PACK.getIndex(), "接单后的状态不是开始拣货状态");
			}

			// 合并上面的两个波次
			query = "?access_token=" + accessTokenGJ + "&timestamp=" + System.currentTimeMillis()
					+ "&platform=ios&channel=qa3";
			body = "{\"waveids\":[" + RandomString.getStringFromList(boCiIds, false) + "]}";
			jsonPath = orderService.mergeGroup(query, body, 0);
			String mergedWaveId = jsonPath.get().toString();

			// 拣货完成
			query = "/" + mergedWaveId + "?" + "waveId=" + mergedWaveId + "&access_token=" + accessTokenGJ
					+ "&timestamp=" + System.currentTimeMillis() + "&platform=ios&channel=qa3";
			body = "{\"waveId\": \"" + mergedWaveId + "\"}";
			jsonPath = orderService.completePack(query, body, 0);
			
			Thread.sleep(5000);

			// 调用管家的订单详情接口获取订单状态
			for (String orderId : orderIds) {
				query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id="
						+ userInfo.getUId();
				jsonPath = orderService.detailGj(query, 0);
				status = jsonPath.getInt("status");
				Assert.isTrue(status == GJOrderStatus.WAITING_ASSIGN.getIndex(), "拣货完成后的状态不是等待分配的状态");
			}

			// 使用拥有自营配送员角色的账号登录管家APP进行配送
			query = "?platform=android";
			body = "{\"pwd\": \"" + password + "\", \"username\": \"" + zpsy + "\"}";
			jsonPath = loginService.loginGJ(query, body, 0);
			accessTokenGJ = jsonPath.getString("token");

			// 更新位置信息，使得此账号的配送员被派单
			query = "?platform=ios&channel=qa3&access_token=" + accessTokenGJ + "&timestamp="
					+ System.currentTimeMillis() + "&longitude=" + lng + "&latitude=" + lat + "";
			jsonPath = userService.location(query, 0);

			Thread.sleep(60000 * 7);

			// 根据订单号获取批次号
			batchIds = orderDao.getBatchIdsByOrderIds(orderIds);
			if (batchIds.isEmpty()) {
				throw new Exception("没有找到批次号，需要加长等候时间");
			}
			// 根据批次号获取整批提货需要的数据
			Map<String, List<String>> batchInfo = orderDao.getMapByBatchIds(batchIds);
			// 遍历批次，整批提货每个批次
			for (String batchId : batchInfo.keySet()) {
				query = "?platform=ios&channel=qa3&access_token=" + accessTokenGJ + "&timestamp="
						+ System.currentTimeMillis();
				body = "{\"action\":1,\"batchid\":" + batchId + ",\"orderids\":["
						+ RandomString.getStringFromList(batchInfo.get(batchId), true) + "]}";

				jsonPath = orderService.batchAction(query, body, 0);
			}

			// 调用管家的订单详情接口获取订单状态
			for (String orderId : orderIds) {
				query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id="
						+ userInfo.getUId();
				jsonPath = orderService.detailGj(query, 0);
				status = jsonPath.getInt("status");
				Assert.isTrue(status == GJOrderStatus.PICKUP.getIndex(), "提货后的状态不是提货的状态");
			}

			// 遍历订单进行核销
			for (String orderId : orderIds) {
				query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp=" + System.currentTimeMillis();
				body = "{\"orderid\": \"" + orderId + "\", \"action\": \"3\"}";
				jsonPath = orderService.orderAction(query, body, 0);
			}

			// 调用管家的订单详情接口获取订单状态
			for (String orderId : orderIds) {
				query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id="
						+ userInfo.getUId();
				jsonPath = orderService.detailGj(query, 0);
				status = jsonPath.getInt("status");
				Assert.isTrue(status == GJOrderStatus.COMPLETE.getIndex(), "核销后的状态不是已完成的状态");
			}

			// 重新登录永辉生活APP刷新用户信息
			query = "?platform=ios";
			body = "{\"phonenum\": \"" + phonenum + "\", \"securitycode\": \"" + securitycode + "\"}";
			userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
			accessTokenSH = userInfo.getAccess_token();

			Assert.isTrue(userInfo.getToComment() - 2 == toComment, "核销后待评价订单总数没有加2");

			// 积分校验
			List<OrderDetail> goodsArr = new ArrayList<OrderDetail>();
			goodsArr.add(new OrderDetail(quantity, price));
			goodsArr.add(new OrderDetail(quantity, price));
			Double tempCredit = ValidateUtil.calculateCredit2(goodsArr);
			System.out.println(tempCredit + "**" + credit);
			Assert.isTrue(userInfo.getCredit() - tempCredit == credit, "核销后用户积分增加不正确");

			// 登出永辉生活app
			query = "?platform=Android&access_token=" + accessTokenSH;
			jsonPath = loginService.loginOutSH(query, 0);

		} catch (Exception e) {
			testcase.setStatus("FAIL");
			testcase.setDescription(e.getMessage());
			throw e;
		} finally {
			// TODO 刪除测试数据
		}

	}
}
