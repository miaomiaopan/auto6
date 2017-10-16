package com.yh.qa.testcase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.yh.qa.entity.OrderDetail;
import com.yh.qa.util.CalculateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.util.ValidateUtil;

import io.restassured.path.json.JsonPath;

/**
 * @author luqiankun
 *
 * @date 2017年10月10日
 */
@SpringBootTest
public class Yh_3 extends BaseTestCase {
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
        String uid = "";
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

        try {
            // 设置case名称
            testcase.setTestName("北京极速达下单（不拆单）和履单流程");

            // case开始执行
            // 登录永辉生活app
            query = "?platform=ios";
            body = "{\"phonenum\": \"17316312358\", \"securitycode\": \"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
            accessTokenSH = userInfo.getAccess_token();
            balance = userInfo.getBalance();
            credit = userInfo.getCredit();
            System.out.println("前面的" + credit);
            num = userInfo.getNum();
            toComment = userInfo.getToComment();
            toDelivery = userInfo.getToDelivery();
            uid = userInfo.getUId();

            // 生成会员店当日达配送订单
            query = "?channel=qa3&deviceid=000000000000000&platform=Android&v=4.2.2.1&access_token="
                    + accessTokenSH+"&timestamp="+System.currentTimeMillis();
            long date = System.currentTimeMillis()/86400000*86400000-28800000;
            body = "{\"pointpayoption\": 0,\"autocoupon\": 0,\"uid\": \"" + uid + "\",\"texpecttime\": {\"date\": "
                    + date + ",\"timeslots\": [{\"to\": \"\",\"slottype\": \"immediate\",\"form\": \"\" }] },\"freedeliveryoption\": 1,\"device_info\": \"688890AE-10B2-432C-B0FC-686CB0F9271F\",\"sellerid\": \"11\",\"comment\": \"\",\"recvinfo\": {\"isdefault\": 1,\"phone\": \"17316312358\",\"alias\": \"公司\",\"id\": \"32319\",\"location\": {\"lat\": \"40.036690\",\"lng\": \"116.360986\" },\"address\": {\"detail\": \"5号楼301室\",\"area\": \"清润家园\",\"city\": \"北京\",\"cityid\": 2 },\"scope\": 0,\"name\": \"卢\" },\"products\": [{\"id\": \"X-251250\",\"num\": 100 }],\"pickself\": 0,\"totalpayment\": 0,\"balancepayoption\": 1,\"storeid\": \"9230\" }";
            jsonPath = orderService.confirm(query, body, 0);
            orderId = jsonPath.getString("orderid");

            // 重新登录永辉生活APP刷新用户信息
            query = "?platform=ios";
            body = "{\"phonenum\": \"17316312358\", \"securitycode\": \"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
            accessTokenSH = userInfo.getAccess_token();

            Assert.isTrue(userInfo.getBalance() + 550 == balance, "下单支付后用户余额减少数额错误");
            Assert.isTrue(userInfo.getNum() - 1 == num, "下单支付后订单总数没有加1");
            Assert.isTrue(userInfo.getToDelivery() - 1 == toDelivery, "下单后待配送订单总数没有加1");

            // 使用拣货员角色的账号登录管家APP
            query = "?platform=android";
            body = "{\"pwd\": \"123456a\", \"username\": \"9230-1\"}";
            jsonPath = loginService.loginGJ(query, body, 0);
            accessTokenGJ = jsonPath.getString("token");

            // 调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.PENDING.getIndex(), "生活APP下当日达非合伙人配送单余额支付后管家中查询订单状态不是待确认状态");

            // 开始拣货
            query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();;
            body = "{\"orderid\": \"" + orderId + "\", \"action\": \"7\"}";
            jsonPath = orderService.orderAction(query, body, 0);

            // 调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.START_PACK.getIndex(), "管家中开始拣货后订单状态不是开始拣货状态");

            // 拣货完成
            query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();;
            body = "{\"orderid\": \"" + orderId + "\", \"action\": \"8\"}";
            jsonPath = orderService.orderAction(query, body, 0);

            // 调用管家的订单详情接口获取订单状态
            TimeUnit.SECONDS.sleep(4);
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.WAITING_ASSIGN.getIndex(), "管家中拣货完成后订单状态不是等待派送状态");

            // 分配派送员
            query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();;
            body = "{\"orderid\": \"" + orderId + "\",\"assignto\": \"50012059\", \"action\": \"5\"}";
            jsonPath = orderService.orderAction(query, body, 0);

            // 配送员角色的账号登录管家APP
            query = "?platform=android";
            body = "{\"pwd\": \"123456a\", \"username\": \"9230-2\"}";
            jsonPath = loginService.loginGJ(query, body, 0);
            accessTokenGJ = jsonPath.getString("token");

            // 调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.WAITING_TAKE.getIndex(), "管家中分配派送后订单状态不是待派状态");

            // 派送员接单
            query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();;
            body = "{\"orderid\": \"" + orderId + "\", \"action\": \"1\"}";
            jsonPath = orderService.orderAction(query, body, 0);

            // 调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.READY_TO_PICKUP.getIndex(), "管家中分配派送员完成后订单状态不是待提状态");

            // 派送员提单
            query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();;
            body = "{\"orderid\": \"" + orderId + "\", \"action\": \"2\"}";
            jsonPath = orderService.orderAction(query, body, 0);

            // 调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.PICKUP.getIndex(), "管家派送员提单完成后订单状态不是待核销状态");

            // 核销
            query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();;
            body = "{\"orderid\": \"" + orderId + "\", \"action\": \"3\"}";
            jsonPath = orderService.orderAction(query, body, 0);

            // 调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.COMPLETE.getIndex(), "管家中核销完成后订单状态不是已核销状态");

            // 重新登录永辉生活APP刷新用户信息
            query = "?platform=ios";
            body = "{\"phonenum\": \"17316312358\", \"securitycode\": \"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
            accessTokenSH = userInfo.getAccess_token();

            Assert.isTrue(userInfo.getToComment() - 1 == toComment, "核销后待评价订单总数没有加1");

            // 积分校验
            List<OrderDetail> goodsArr = new ArrayList<OrderDetail>();
            goodsArr.add(new OrderDetail(1d, 5.50d));
            Double tempCredit = ValidateUtil.calculateCredit2(goodsArr);
            System.out.println(tempCredit + "**" + credit);
            Assert.isTrue(CalculateUtil.sub(userInfo.getCredit(),tempCredit) == new BigDecimal(credit).doubleValue(), "核销后用户积分增加不正确，原来"+credit+",增加"+tempCredit+",现在"+userInfo.getCredit());

            // 登出永辉生活app
            query = "?platform=Android&access_token=" + accessTokenSH;
            jsonPath = loginService.loginOutSH(query, 0);

        } catch (Exception e) {
            testcase.setStatus("FAILURE");
            testcase.setDescription(e.getMessage());
            throw e;
        } finally {
            // TODO 刪除测试数据
        }

    }
}