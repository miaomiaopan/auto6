package com.yh.qa.testcase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yh.qa.dao.OrderDao;
import com.yh.qa.datasource.DataSourceTemplete;
import com.yh.qa.service.UserService;
import com.yh.qa.util.RandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.entity.BoCiInfo;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.util.ValidateUtil;

import io.restassured.path.json.JsonPath;

/**
 * @author 范文华
 * @data   2017-10-12
 * @desc   集波：合波次后继续履单
 */

@SpringBootTest
public class Yh_11 extends BaseTestCase {
    private JsonPath jsonPath = null;
    private String accessTokenSH = "";
    private String accessTokenGJ = "";
    private String orderNumber = "";
    private String uid = "";
    private UserInfo userInfo = null;
    private static final String skuCode = "M-152239";
    private static final String manager = "9167";
    private static final String picker = "9167100";
    private static final String sender = "9167200";
    private static final String receiver = "9167300";   //签收员
    // 配送员UUID
    private static final String ReceiverID = "50012031";
    // 余额
    private int balance;
    // 积分
    private Double credit;
    // 订单总数
    private int orderTotalCount;
    // 待评价订单数量
    private int toComment;
    // 待配送订单数量
    private int toDelivery;
    // 订单状态
    private int status;
    // 订单总数
    int num;
    private static Logger logger = LoggerFactory.getLogger(Yh_11.class);
    List<String> orderIds = new ArrayList<String>();
    List<String> boCiIds = new ArrayList<String>();

    @Autowired
    private LoginService loginService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    @Qualifier(DataSourceTemplete.ORDER_DB)
    private JdbcTemplate jdbcTemplateOrderDb;

    @Test(description = "集波：合波次后继续履单")
    public void testHttpPost() throws Exception {
        String uri = "";
        String body = "";
        JsonPath jsonPath = null;
        String accessTokenSH = "";
        String accessTokenGJ = "";
        UserInfo userInfo = null;
        String uidSH = "";
        List<BoCiInfo> boCiInfos = null;

        try {
            // 设置case名称
            testcase.setTestName("集波：合波次后继续履单");

            // 登录永辉生活app
            uri = "?platform=ios";
            body = "{\"phonenum\": \"13661629814\", \"securitycode\": \"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(uri, body, 0);
            accessTokenSH = userInfo.getAccess_token();
            balance = userInfo.getBalance();
            credit = userInfo.getCredit();
            num = userInfo.getNum();
            toComment = userInfo.getToComment();
            toDelivery = userInfo.getToDelivery();

            // 生成bravo绿标店当日达配送订单
            uri = "?channel=qa3&deviceid=000000000000000&platform=Android&v=4.2.2.1&access_token=" + accessTokenSH
                    + "&timestamp=" + System.currentTimeMillis();
            body = "{\"balancepayoption\":1,\"device_info\":\"864854034674759\",\"freedeliveryoption\":1,\"paypasswordtype\":0,\"pickself\":0,\"pointpayoption\":0,\"pricetotal\":0,\"products\":[{\"id\":\"B-587728\",\"isbulkitem\":0,\"num\":100,\"pattern\":\"t\"}],\"recvinfo\":{\"address\":{\"area\":\"汉泰中泰融合餐厅(嘉定大融城店)\",\"city\":\"上海\",\"cityid\":\"1\",\"detail\":\"111\"},\"alias\":\"家\",\"foodsupport\":0,\"id\":\"32395\",\"isSearch\":false,\"isdefault\":1,\"itemType\":0,\"location\":{\"lat\":\"31.336332018030166\",\"lng\":\"121.28687434307753\"},\"name\":\"owin\",\"nextdaydeliver\":0,\"phone\":\"13661628181\",\"scope\":0},\"sellerid\":3,\"storeid\":\"9485\",\"texpecttime\":{\"date\":"
                    + (System.currentTimeMillis() / 86400000 * 86400000 - 28800000)
                    + ",\"timeslots\":[{\"immediatedesc\":\"60分钟达\",\"slottype\":\"immediate\"}]},\"totalpayment\":0,\"uid\":\"21840\"}";
            for (int i = 0; i < 2; i++) {
                jsonPath = orderService.confirm(uri, body, 0);
                orderIds.add(jsonPath.getString("orderid"));
                Thread.sleep(100000);
            }

//            // 重新登录永辉生活APP刷新用户信息
//            uri = "?platform=ios";
//            body = "{\"phonenum\": \"13661629813\", \"securitycode\": \"601933\"}";
//            userInfo = loginService.loginSHAndGetUserInfo(uri, body, 0);
//            accessTokenSH = userInfo.getAccess_token();

//            Assert.isTrue(userInfo.getBalance() + 1350 == balance, "下单支付后用户余额减少数额错误");
//            Assert.isTrue(userInfo.getNum() - 1 == num, "下单支付后订单总数没有加1");
//            Assert.isTrue(userInfo.getToDelivery() - 1 == toDelivery, "下单后待配送订单总数没有加1");

            // 拣货员登录管家APP进行拣货
            uri = "?platform=android";
            body = "{\"pwd\": \"123456a\", \"username\": \"9485100\"}";
            jsonPath = loginService.loginGJ(uri, body, 0);
            accessTokenGJ = jsonPath.getString("token");
            // 调用管家的订单详情接口获取订单状态
            for (String orderId : orderIds) {
                uri = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uidSH;
                jsonPath = orderService.detailGj(uri, 0);
                status = jsonPath.getInt("status");
                Assert.isTrue(status == GJOrderStatus.PENDING.getIndex(), "下单完成后订单状态不是待确认状态");
            }

            Thread.sleep(10000);

            // 获取待接单的波次号信息
            uri = "?page=0&filter=0&timestamp=" + System.currentTimeMillis()
                    + "&platform=Android&channel=qa3&access_token=" + accessTokenGJ;
            jsonPath = orderService.waitingPack(uri, 0);
            boCiInfos = jsonPath.getList("orders", BoCiInfo.class);
            for (BoCiInfo entity : boCiInfos) {
                boCiIds.add(String.valueOf(entity.getPickwaveid()));
            }

            // 接单
            for (String waveId : boCiIds) {
                uri = "/" + waveId + "?" + "waveId=" + waveId + "&access_token=" + accessTokenGJ + "&timestamp="
                        + System.currentTimeMillis() + "&platform=ios&channel=qa3";
                body = "{\"waveId\": \"" + waveId + "\"}";
                jsonPath = orderService.startPack(uri, body, 0);
            }

            // 调用管家的订单详情接口获取订单状态
            for (String orderId : orderIds) {
                uri = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uidSH;
                jsonPath = orderService.detailGj(uri, 0);
                status = jsonPath.getInt("status");
                Assert.isTrue(status == GJOrderStatus.START_PACK.getIndex(), "接单后的状态不是开始拣货状态");
            }

            // 合并存在的波次
            uri = "?access_token=" + accessTokenGJ + "&timestamp=" + System.currentTimeMillis()
                    + "&platform=ios&channel=qa3";
            body = "{\"waveids\":[" + RandomString.getStringFromList(boCiIds, false) + "]}";
            System.out.println("合并存在的波次 body: " + body);
            jsonPath = orderService.mergeGroup(uri, body, 0);
            String mergedWaveId = jsonPath.get().toString();

            // 拣货完成

            uri = "/" + mergedWaveId + "?" + "waveId=" + mergedWaveId + "&access_token=" + accessTokenGJ + "&timestamp="
                    + System.currentTimeMillis() + "&platform=ios&channel=qa3";
            body = "{\"waveId\": \"" + mergedWaveId + "\"}";
            System.out.println("拣货完成 body: " + body);
            jsonPath = orderService.completePack(uri, body, 0);


            Thread.sleep(3000);

            // 调用管家的订单详情接口获取订单状态
            for (String orderId : orderIds) {
                uri = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uidSH;
                jsonPath = orderService.detailGj(uri, 0);
                status = jsonPath.getInt("status");
                Assert.isTrue(status == GJOrderStatus.WAITING_ASSIGN.getIndex(), "拣货完成后的状态不是等待分配的状态");
            }

            // 拣货员派单
            uri = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp=" + System.currentTimeMillis();
            body = "{\"orderid\":\"" + orderIds.get(0) + "\",\"assignto\":50012079,\"action\":5}";
            jsonPath = orderService.orderAction(uri, body, 0);

            Thread.sleep(5000);

            // 调用管家的订单详情接口获取订单状态
            uri = "?platform=Android&orderid=" + orderIds.get(0) + "&access_token=" + accessTokenGJ + "&id=" + uidSH;
            jsonPath = orderService.detailGj(uri, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.WAITING_TAKE.getIndex(), "派单后的状态不是等待接单的状态");

            // 配送员登录管家APP进行配送
            uri = "?platform=android";
            body = "{\"pwd\": \"123456a\", \"username\": \"9485201\"}";
            jsonPath = loginService.loginGJ(uri, body, 0);
            accessTokenGJ = jsonPath.getString("token");

            // 接单
            uri = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp=" + System.currentTimeMillis();
            body = "{\"orderid\": \"" + orderIds.get(0) + "\", \"action\": \"1\"}";
            jsonPath = orderService.orderAction(uri, body, 0);

            Thread.sleep(5000);

            // 调用管家的订单详情接口获取订单状态
            uri = "?platform=Android&orderid=" + orderIds.get(0) + "&access_token=" + accessTokenGJ + "&id=" + uidSH;
            jsonPath = orderService.detailGj(uri, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.READY_TO_PICKUP.getIndex(), "接单后的状态不是待提的状态");

            // 提货
            uri = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp=" + System.currentTimeMillis();
            body = "{\"orderid\": \"" + orderIds.get(0) + "\", \"action\": \"2\"}";
            jsonPath = orderService.orderAction(uri, body, 0);

            Thread.sleep(5000);

            // 调用管家的订单详情接口获取订单状态
            uri = "?platform=Android&orderid=" + orderIds.get(0) + "&access_token=" + accessTokenGJ + "&id=" + uidSH;
            jsonPath = orderService.detailGj(uri, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.PICKUP.getIndex(), "提货后的状态不是提货状态");

            // 核销
            uri = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp=" + System.currentTimeMillis();
            body = "{\"orderid\": \"" + orderIds.get(0) + "\", \"action\": \"3\"}";
            jsonPath = orderService.orderAction(uri, body, 0);

            Thread.sleep(5000);

            // 调用管家的订单详情接口获取订单状态
//            for (String orderId : orderIds) {
//                uri = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uidSH;
//                jsonPath = orderService.detailGj(uri, 0);
//                status = jsonPath.getInt("status");
//                Assert.isTrue(status == GJOrderStatus.COMPLETE.getIndex(), "核销后的状态不是已完成的状态");
//            }

            // 重新登录永辉生活APP刷新用户信息
            uri = "?platform=ios";
            body = "{\"phonenum\": \"18729555529\", \"securitycode\":\"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(uri, body, 0);
            accessTokenSH = userInfo.getAccess_token();

//            Assert.isTrue(userInfo.getToComment() - 2 == toComment, "核销后待评价订单总数没有加2");

            // 积分校验
            Map<Double, Double> goodsArr = new HashMap<Double, Double>();
            // key为数量，value为价格
            goodsArr.put(2d, 15.50);
            Double tempCredit = ValidateUtil.calculateCredit(goodsArr);
            System.out.println("产生的积分" + tempCredit);
//            Assert.isTrue(userInfo.getCredit() - tempCredit == credit, "核销后用户积分增加不正确");

            // 登出永辉生活app
            uri = "?platform=Android&access_token=" + accessTokenSH;
            jsonPath = loginService.loginOutSH(uri, 0);

        } catch (Exception e) {
            testcase.setStatus("FAIL");
            testcase.setDescription(e.getMessage());
            throw e;
        } finally {
            // TODO
        }
    }
}
