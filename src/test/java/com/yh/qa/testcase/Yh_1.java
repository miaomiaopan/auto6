package com.yh.qa.testcase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.yh.qa.util.CalculateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.OrderDetail;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.util.ValidateUtil;

import io.restassured.path.json.JsonPath;

/**
 * @author panmiaomiao
 *
 * @date 2017年9月29日
 */
@SpringBootTest
public class Yh_1 extends BaseTestCase {
	@Autowired
	private LoginService loginService;

	@Autowired
	private OrderService orderService;

	@Test
	public void testHttpPost() throws Exception {
		String query = "";
		String body = "";
		JsonPath jsonPath = null;
		String orderId = "";
		String accessTokenSH = "";
		String accessTokenGJ = "";
		UserInfo userInfo = null;
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

		// 测试数据 门店和商品  上海会员店开鲁路店
		String storeId = "9D52"; 
		String lat = "31.330927";
		String lng = "121.536935";
		String sku = "853743";
		Double price = 10.80d;
		Double quantity = 1d;
		Double pricetotal = 0d;

		// 下单用户
		String phonenum = "18729555529";
		String securitycode = "601933";

		// 店长
		String dz = "9D52";
		String password = "123456a";

		try {
			// 设置case名称
			testcase.setTestName("会员店非合伙人当日达自提单履单");

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

			// 生成会员店当日达配送订单
			query = "?channel=qa3&deviceid=000000000000000&platform=Android&v=4.2.2.1&access_token=" + accessTokenSH
					+ "&timestamp=" + System.currentTimeMillis();
			body = "{\"balancepayoption\":1,\"device_info\":\"000000000000000\",\"freedeliveryoption\":1,\"paypasswordtype\":0,\"pickself\":0,\"pointpayoption\":0,\"pricetotal\":"
					+ pricetotal + ",\"products\":[{\"id\":\"" + sku + "\",\"isbulkitem\":0,\"num\":" + quantity * 100
					+ ",\"pattern\":\"t\"}],\"recvinfo\":{\"address\":{\"area\":\"恒隆广场\",\"city\":\"上海\",\"cityid\":\"1\",\"detail\":\"一号\"},\"alias\":\"公司\",\"foodsupport\":0,\"id\":\"32266\",\"isSearch\":false,\"isdefault\":1,\"itemType\":0,\"location\":{\"lat\":\""
					+ lat + "\",\"lng\":\"" + lng
					+ "\"},\"name\":\"小潘\",\"nextdaydeliver\":0,\"phone\":\"18729552102\",\"scope\":0},\"sellerid\":2,\"storeid\":\""
					+ storeId
					+ "\",\"texpecttime\":{\"date\":"+(System.currentTimeMillis() / 86400000 * 86400000 - 28800000)+",\"timeslots\":[{\"immediatedesc\":\"最快30分钟达\",\"slottype\":\"immediate\"}]},\"totalpayment\":0,\"uid\":\""
					+ userInfo.getUId() + "\"}";
			jsonPath = orderService.confirm(query, body, 0);
			orderId = jsonPath.getString("orderid");

			// 重新登录永辉生活APP刷新用户信息
			query = "?platform=ios";
			body = "{\"phonenum\": \"18729555529\", \"securitycode\": \"601933\"}";
			userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
			accessTokenSH = userInfo.getAccess_token();

			Assert.isTrue(userInfo.getBalance() + (quantity * price+6d) * 100 == balance, "下单支付后用户余额减少数额错误");
			Assert.isTrue(userInfo.getNum() - 1 == num, "下单支付后订单总数没有加1");
			Assert.isTrue(userInfo.getToDelivery() - 1 == toDelivery, "下单后待配送订单总数没有加1");

			// 使用拥有店长角色的账号登录管家APP
			query = "?platform=android";
			body = "{\"pwd\": \"" + password + "\", \"username\": \"" + dz + "\"}";
			jsonPath = loginService.loginGJ(query, body, 0);
			accessTokenGJ = jsonPath.getString("token");

			// 调用管家的订单详情接口获取订单状态
			query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + userInfo.getUId();
			jsonPath = orderService.detailGj(query, 0);
			status = jsonPath.getInt("status");
			Assert.isTrue(status == GJOrderStatus.PENDING.getIndex(), "生活APP下当日达非合伙人配送单余额支付后管家中查询订单状态不是待确认状态");

			// 开始拣货
			query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp=" + System.currentTimeMillis();
			body = "{\"orderid\": \"" + orderId + "\", \"action\": \"7\"}";
			jsonPath = orderService.orderAction(query, body, 0);

			Thread.sleep(3000);

			// 调用管家的订单详情接口获取订单状态
			query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + userInfo.getUId();
			jsonPath = orderService.detailGj(query, 0);
			status = jsonPath.getInt("status");
			Assert.isTrue(status == GJOrderStatus.START_PACK.getIndex(), "管家中开始拣货后订单状态不是开始拣货状态");

			// 拣货完成
			query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp=" + System.currentTimeMillis();
			body = "{\"orderid\": \"" + orderId + "\", \"action\": \"8\"}";
			jsonPath = orderService.orderAction(query, body, 0);

			Thread.sleep(3000);

			// 调用管家的订单详情接口获取订单状态
			query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + userInfo.getUId();
			jsonPath = orderService.detailGj(query, 0);
			status = jsonPath.getInt("status");
			Assert.isTrue(status == GJOrderStatus.READY_TO_PICKUP.getIndex(), "管家中拣货完成后订单状态不是等待提货状态");

			// 提货
			query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp=" + System.currentTimeMillis();
			body = "{\"orderid\": \"" + orderId + "\", \"action\": \"2\"}";
			jsonPath = orderService.orderAction(query, body, 0);

			Thread.sleep(3000);

			// 调用管家的订单详情接口获取订单状态
			query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + userInfo.getUId();
			jsonPath = orderService.detailGj(query, 0);
			status = jsonPath.getInt("status");
			Assert.isTrue(status == GJOrderStatus.PICKUP.getIndex(), "管家中提货完成后订单状态不是提货状态");

			// 核销
			query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp=" + System.currentTimeMillis();
			body = "{\"orderid\": \"" + orderId + "\", \"action\": \"3\"}";
			jsonPath = orderService.orderAction(query, body, 0);

			Thread.sleep(3000);

			// 调用管家的订单详情接口获取订单状态
			query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + userInfo.getUId();
			jsonPath = orderService.detailGj(query, 0);
			status = jsonPath.getInt("status");
			Assert.isTrue(status == GJOrderStatus.COMPLETE.getIndex(), "管家中核销完成后订单状态不是已核销状态");

			// 重新登录永辉生活APP刷新用户信息
			query = "?platform=ios";
			body = "{\"phonenum\": \""+phonenum+"\", \"securitycode\": \""+securitycode+"\"}";
			userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
			accessTokenSH = userInfo.getAccess_token();

			Assert.isTrue(userInfo.getToComment() - 1 == toComment, "核销后待评价订单总数没有加1" );

			// 积分校验
			List<OrderDetail> goodsArr = new ArrayList<OrderDetail>();
			goodsArr.add(new OrderDetail(quantity, price));
			Double tempCredit = ValidateUtil.calculateCredit2(goodsArr);
			System.out.println(tempCredit+"**"+credit);
			Assert.isTrue(CalculateUtil.sub(userInfo.getCredit(),tempCredit) == new BigDecimal(credit).doubleValue(), "核销后用户积分增加不正确，原来"+credit+",增加"+tempCredit+",现在"+userInfo.getCredit());

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
