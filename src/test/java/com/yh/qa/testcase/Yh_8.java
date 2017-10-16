package com.yh.qa.testcase;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.dao.OrderDao;
import com.yh.qa.datasource.DataSourceTemplete;
import com.yh.qa.entity.Case;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.repository.CaseRepository;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.util.ResultBean;
import com.yh.qa.util.ValidateUtil;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import javax.validation.constraints.Null;
import javax.xml.crypto.Data;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/***
 *
 * @author 范文华
 * @data   2017-10-11
 * @desc   Bravo 订单登记缺货后拆单
 *
 */

@SpringBootTest
public class Yh_8 extends BaseTestCase {

    private JsonPath jsonPath = null;
    private String accessTokenSH = "";
    private String accessTokenGJ = "";
    private String orderNumber = "";
    private String uid = "";
    private UserInfo userInfo = null;

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
    // 配送员UUID
    private static final String ReceiverID = "50012035";

    @Autowired
    private LoginService loginService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    @Qualifier(DataSourceTemplete.ORDER_DB)
    private JdbcTemplate jdbcTemplateOrderDb;

    @Test(description = "Bravo 订单登记缺货后拆单")
    public void testBravoOutOfStockSpiltOrderFlowing() throws Exception{
        String uri = "";
        String body = "";

        try{
            testcase.setTestName("Bravo 订单登记缺货后拆单");
            //1. 调用永辉生活登录接口

            uri = "?platform=ios";
            body = "{\"phonenum\": \"13661629813\", \"securitycode\": \"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(uri, body, 0);
            accessTokenSH = userInfo.getAccess_token();
            balance = userInfo.getBalance();
            credit = userInfo.getCredit();
            System.out.println("前面的" + credit);
            orderTotalCount = userInfo.getNum();
            toComment = userInfo.getToComment();
            toDelivery = userInfo.getToDelivery();
            uid = userInfo.getUId();

            //2. 调用永辉生活下单接口，余额支付成功后登出。 上海隆昌路店 9442

            uri = "?uid=21286&channel=2&deviceid=B6B8ACE9-1167-4550-9672-EF9BA9C6AF55&platform=ios&v=4.2.3.3&timestamp=1506667289293&sign=6732c96163dcc5aae62c01942c08f648&access_token=" + accessTokenSH;
            body = "{\n" +
                    "\t\"pointpayoption\": 0,\n" +
                    "\t\"autocoupon\": 0,\n" +
                    "\t\"uid\": \"21286\",\n" +
                    "\t\"texpecttime\": {\n" +
                    "\t\t\"date\": 1507564800000,\n" +
                    "\t\t\"timeslots\": [{\n" +
                    "\t\t\t\"to\": \"\",\n" +
                    "\t\t\t\"slottype\": \"immediate\",\n" +
                    "\t\t\t\"form\": \"\"\n" +
                    "\t\t}]\n" +
                    "\t},\n" +
                    "\t\"freedeliveryoption\": 1,\n" +
                    "\t\"device_info\": \"2975B689-9EB0-45C5-9BE1-A673CF8FF255\",\n" +
                    "\t\"sellerid\": \"3\",\n" +
                    "\t\"recvinfo\": {\n" +
                    "\t\t\"isdefault\": 1,\n" +
                    "\t\t\"phone\": \"13661621389\",\n" +
                    "\t\t\"alias\": \"公司\",\n" +
                    "\t\t\"id\": \"32257\",\n" +
                    "\t\t\"location\": {\n" +
                    "\t\t\t\"lat\": \"31.281255\",\n" +
                    "\t\t\t\"lng\": \"121.549872\"\n" +
                    "\t\t},\n" +
                    "\t\t\"address\": {\n" +
                    "\t\t\t\"detail\": \"123号\",\n" +
                    "\t\t\t\"area\": \"永辉超市(隆昌路店)\",\n" +
                    "\t\t\t\"city\": \"上海\",\n" +
                    "\t\t\t\"cityid\": 1\n" +
                    "\t\t},\n" +
                    "\t\t\"scope\": 0,\n" +
                    "\t\t\"name\": \"隆昌路\"\n" +
                    "\t},\n" +
                    "\t\"products\": [{\n" +
                    "\t\t\"id\": \"B-777586\",\n" +
                    "\t\t\"num\": 100\n" +
                    "\t}, {\n" +
                    "\t\t\"id\": \"B-14045\",\n" +
                    "\t\t\"num\": 100\n" +
                    "\t}],\n" +
                    "\t\"pickself\": 0,\n" +
                    "\t\"totalpayment\": 0,\n" +
                    "\t\"balancepayoption\": 1,\n" +
                    "\t\"storeid\": \"9442\"\n" +
                    "}";
            jsonPath = orderService.confirm(uri, body, 0);
            orderNumber = jsonPath.getString("orderid");
            System.out.println("order id is : " + orderNumber);

            // 登出永辉生活app
            uri = "?platform=Android&access_token=" + accessTokenSH;
            loginService.loginOutSH(uri, 0);

            //3. 拣货员登录APP，调用接口，开始拣货（校验订单状态） ==> 登记一个商品缺货后完成拣货，预期拆单

            uri = "?platform=android";
            body = "{\"pwd\": \"123456a\", \"username\": \"9442100\"}";
            jsonPath = loginService.loginGJ(uri, body, 0);
            accessTokenGJ = jsonPath.getString("token");
            // 调用管家的订单详情接口获取订单状态
            uri = "?platform=Android&orderid=" + orderNumber + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(uri, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.PENDING.getIndex(), "生活下单后，管家显示的不是Pending状态");
            // 开始拣货
            uri = "?platform=ios&access_token=" + accessTokenGJ;
            body = "{\"orderid\": \"" + this.orderNumber + "\", \"action\": \"7\"}";
            jsonPath = orderService.orderAction(uri, body, 0);
            // 调用管家的订单详情接口获取订单状态
            uri = "?platform=Android&orderid=" + orderNumber + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(uri, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.START_PACK.getIndex(), "开始拣货后，订单不是待完成拣货状态");
            // 登记一个商品缺货
            uri = "?platform=ios&access_token=" + accessTokenGJ;
            body = "{\"orderLackList\":[{\"orderid\":" + this.orderNumber + ",\"stockleft\":0,\"skucode\":\"B-777586\",\"itemid\":" + this.orderNumber + "001" + "}]}";
            jsonPath = orderService.registerLackSku(uri, body, 0);

            //完成拣货
            body = "{\"orderid\": \"" + this.orderNumber + "\", \"action\": \"8\"}";
            jsonPath = orderService.orderAction(uri, body, 0);

            // 校验原订单已拆单
            Thread.sleep(2000);
            List<String> orderIds = new ArrayList<>();
            orderIds.add(this.orderNumber);
            List<String> orderStatus = null;
            orderStatus = orderDao.getSplitInfoByParentsOrderID(orderIds);

            for (String status : orderStatus){
                Assert.isTrue(orderStatus.get(0)!="os.split", "部分缺货后，没有拆单");
            }

            // 获取子订单ID
            String childStr = "";
            String refundOrder = "";
            String normalOrder = "";
            List<String> childOrders = null;
            childOrders = jdbcTemplateOrderDb.queryForList("SELECT children_order_ids FROM t_trade_order_header where id =" + this.orderNumber, String.class);
            System.out.println(childOrders.isEmpty());
            System.out.println(childOrders.size());
            for (String child : childOrders){
                childStr = child;
            }
            refundOrder = StringUtils.substringBefore(childStr, ";");
            normalOrder = StringUtils.substringBefore(StringUtils.substringAfter(childStr, ";"), ";");
            System.out.println("refund order is : " + refundOrder);
            System.out.println("normal order is : " + normalOrder);

            // 校验缺货子订单是退款状态
            List<String> refundOrders = null;
            String refundStatus = "";
            refundOrders = jdbcTemplateOrderDb.queryForList("SELECT order_status FROM t_trade_order_header where id =" + refundOrder, String.class);
            System.out.println(refundOrders.isEmpty());
            System.out.println(refundOrders.size());
            for (String refund : refundOrders){
                Assert.isTrue(refundOrders.get(0)!="os.refund.completed", "缺货商品没有退款");
                refundStatus = refund;
            }

            // 有效子订单继续履单流程
            Thread.sleep(2000);
            // 调用管家的订单详情接口获取订单状态
            uri = "?platform=Android&orderid=" + normalOrder + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(uri, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.WAITING_ASSIGN.getIndex(), "拆单后的正常订单，不是等待派单的状态");
            // 开始派单
            uri = "?platform=ios&access_token=" + accessTokenGJ;
            body = "{\"orderid\": \"" + normalOrder + "\", \"action\": \"5\", \"assignto\":" + ReceiverID + "}";
            System.out.println("body is : " + body);
            jsonPath = orderService.orderAction(uri, body, 0);

            // 4. 配送员登录APP，完成配送并核销订单，校验订单状态，成功后登出

            uri = "?platform=android";
            body = "{\"pwd\": \"123456a\", \"username\": \"9442200\"}";
            jsonPath = loginService.loginGJ(uri, body, 0);
            accessTokenGJ = jsonPath.getString("token");
            // 调用管家的订单详情接口获取订单状态
            uri = "?platform=Android&orderid=" + normalOrder + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(uri, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.WAITING_TAKE.getIndex(), "派单完成后，订单状态不是等待接单状态");
            // 开始接单
            uri = "?platform=ios&access_token=" + accessTokenGJ;
            body = "{\"orderid\": \"" + normalOrder + "\", \"action\": \"1\"}";
            jsonPath = orderService.orderAction(uri, body, 0);
            // 调用管家的订单详情接口获取订单状态
            uri = "?platform=Android&orderid=" + normalOrder + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(uri, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.READY_TO_PICKUP.getIndex(), "接单完成后，订单状态不是等待提货状态");
            // 开始提货
            uri = "?platform=ios&access_token=" + accessTokenGJ;
            body = "{\"orderid\": \"" + normalOrder + "\", \"action\": \"2\"}";
            jsonPath = orderService.orderAction(uri, body, 0);
            // 调用管家的订单详情接口获取订单状态
            uri = "?platform=Android&orderid=" + normalOrder + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(uri, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.PICKUP.getIndex(), "提货完成后，订单状态不是已经提货状态");
            // 开始核销
            uri = "?platform=ios&access_token=" + accessTokenGJ;
            body = "{\"orderid\": \"" + normalOrder + "\", \"action\": \"3\"}";
            jsonPath = orderService.orderAction(uri, body, 0);
            // 调用管家的订单详情接口获取订单状态
            uri = "?platform=Android&orderid=" + normalOrder + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(uri, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.COMPLETE.getIndex(), "订单不是已经核销状态");

            // 重新登录永辉生活APP刷新用户信息
            uri = "?platform=ios";
            body = "{\"phonenum\": \"13661629813\", \"securitycode\": \"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(uri, body, 0);
            accessTokenSH = userInfo.getAccess_token();
            Assert.isTrue(userInfo.getToComment() - 1 == toComment, "核销后待评价订单总数没有加1");
        }catch (Exception e){
            testcase.setStatus("FAIL");
            testcase.setDescription(e.getMessage());
            throw e;
        }
        finally {
            ;//TODO
        }
    }
}
