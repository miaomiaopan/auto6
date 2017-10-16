package com.yh.qa.testcase;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.dao.OrderDao;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.util.ValidateUtil;
import io.restassured.path.json.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author by xieweiwei
 * @date 2017年10月10日
 */

@SpringBootTest
public class Yh_13 extends BaseTestCase {
    @Autowired
    private LoginService loginService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDao orderDao;

    @Test
    public void testSuperSpeciesNormalSendingOrderFlowing() throws Exception {
        String query = "";
        String body = "";
        JsonPath jsonPath = null;
        String orderId = "";
        String pickOrderId = "";
        String accessTokenSH = "";
        String accessTokenGJ = "";
        String uid = "";
        UserInfo userInfo = null;
        //余额
        int balance;
        //积分
        Double credit;
        //订单总数
        int num;
        //待评价订单数量
        int toComment;
        //待配送订单数量
        int toDelivery;
        //订单状态
        int status;

        try {
            // 设置case名称
            testcase.setTestName("超级物种货架区商品配送单履单");

            // case开始执行
            // 登录永辉生活app
            query = "?platform=ios";
            body = "{\"phonenum\":\"13816043211\",\"securitycode\":\"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
            accessTokenSH = userInfo.getAccess_token();
            balance = userInfo.getBalance();
            credit = userInfo.getCredit();
            System.out.println("前面的" + credit);
            num = userInfo.getNum();
            toComment = userInfo.getToComment();
            toDelivery = userInfo.getToDelivery();
            uid = userInfo.getUId();

            //生成超级物种货架区商品配送订单
            query = "?channel=qa3&deviceid=0000000000000005&platform=ios&v=4.2.3.3&access_token=" + accessTokenSH + "&timestamp=" + System.currentTimeMillis();
            body = "{\"pointpayoption\":0,\"autocoupon\":0,\"uid\":\"20161\",\"texpecttime\":{\"date\":1507564800000,\"timeslots\":[{\"to\":\"\",\"slottype\":\"immediate\",\"form\":\"\"}]},\"freedeliveryoption\":1,\"device_info\":\"B30C2110-DA75-4CA4-9BE7-65969BCA97CF\",\"sellerid\":\"6\",\"comment\":\"\",\"recvinfo\":{\"isdefault\":1,\"phone\":\"13816043211\",\"alias\":\"\",\"id\":\"32322\",\"location\":{\"lat\":\"26.103511\",\"lng\":\"119.319262\"},\"address\":{\"detail\":\"鼓楼区温泉公园路8号\",\"area\":\"温泉公园\",\"city\":\"福州\",\"cityid\":4},\"scope\":0,\"name\":\"谢\"},\"products\":[{\"id\":\"S-918755\",\"num\":100}],\"pickself\":0,\"totalpayment\":0,\"balancepayoption\":1,\"storeid\":\"9I01\"}";
            jsonPath = orderService.confirm(query, body, 0);
            orderId = jsonPath.getString("orderid");


            //重新登录永辉生活app刷新用户信息
            query = "?platform=ios";
            body = "{\"phonenum\":\"13816043211\",\"securitycode\":\"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
            accessTokenSH = userInfo.getAccess_token();

            Assert.isTrue(userInfo.getBalance() + 300 == balance, "下单支付后用户余额减少数额错误");
            Assert.isTrue(userInfo.getNum() - 1 == num, "下单支付后订单总数没有加1");
            Assert.isTrue(userInfo.getToDelivery() - 1 == toDelivery, "下单后待配送订单总数没有加1");

            //使用超级物种温泉店货架区商品拣货员账号登录管家app
            query = "?platform=ios";
            body = "{\"pwd\": \"123456a\", \"username\": \"13816043315\"}";
            jsonPath = loginService.loginGJ(query, body, 0);
            accessTokenGJ = jsonPath.getString("token");

            //调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.PENDING.getIndex(), "生活APP下超级物种货架区普通商品订单余额支付后管家中查询订单状态不是待确认状态");

            //根据订单id获取拣货单id
            pickOrderId = orderDao.getPickOrderIdByOrderId(orderId);

            //拣货员开始拣货
            query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp" + System.currentTimeMillis();
            body = "{\"orderid\": \"" + pickOrderId + "\",\"action\": 7}";
            jsonPath = orderService.orderAction(query, body, 0);

            //调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.PENDING.getIndex(), "管家中开始拣货后订单状态不是开始拣货状态");

            //扫描第一个悬挂袋
            query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp" + System.currentTimeMillis();
            body = "{\"bagcode\": \"43\",\"orderid\": \"" + orderId + "\"}";
            jsonPath = orderService.mergingScan(query, body, 0);

            //再次扫描第二个悬挂袋
            query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp" + System.currentTimeMillis();
            body = "{\"bagcode\": \"46\",\"orderid\": \"" + orderId + "\"}";
            jsonPath = orderService.mergingScan(query, body, 0);

            //调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.START_PACK.getIndex(), "管家中扫描悬挂袋后订单状态不是开始拣货状态");

            //悬挂袋和订单绑定
            query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp" + System.currentTimeMillis();
            body = "{\"orderid\": \"" + orderId + "\"}";
            jsonPath = orderService.mergingBind(query, body, 0);

            //调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.START_PACK.getIndex(), "管家中悬挂袋和订单绑定后订单状态不是开始拣货状态");

            //使用超级物种温泉店合单员登录管家app
            query = "?platform=ios";
            body = "{\"pwd\": \"123456a\", \"username\": \"13585727592\"}";
            jsonPath = loginService.loginGJ(query, body, 0);
            accessTokenGJ = jsonPath.getString("token");

            //合单员扫描第一个悬挂袋
            query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp" + System.currentTimeMillis();
            body = "{\"bagcode\": \"43\"}";
            jsonPath = orderService.mergingInbox(query, body, 0);

            //调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.START_PACK.getIndex(), "管家中合单员只扫描一个悬挂袋后订单状态不是开始拣货状态");

            //合单员再次扫描第二个悬挂袋
            query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp" + System.currentTimeMillis();
            body = "{\"bagcode\": \"46\"}";
            jsonPath = orderService.mergingInbox(query, body, 0);

            Thread.sleep(3000);

            //调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.WAITING_ASSIGN.getIndex(), "管家中合单入周转箱后订单状态不是等待分配");

            //使用超级物种温泉店自营配送员登录管家app
            query = "?platform=ios";
            body = "{\"pwd\": \"123456a\", \"username\": \"13167176907\"}";
            jsonPath = loginService.loginGJ(query, body, 0);
            accessTokenGJ = jsonPath.getString("token");

            //配送员扫码提货
            query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp" + System.currentTimeMillis();
            body = "{\"orderid\": \"" + orderId + "\",\"action\": 2}";
            jsonPath = orderService.orderAction(query, body, 0);

            //调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.PICKUP.getIndex(), "管家中扫码提货后订单状态不是已提货状态");

            //配送员核销订单
            query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp" + System.currentTimeMillis();
            body = "{\"orderid\": \"" + orderId + "\",\"action\": 3,\"memo\": \"26.103511,119.319262\"}";
            jsonPath = orderService.orderAction(query, body, 0);

            //调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.COMPLETE.getIndex(), "管家中核销后订单状态不是已签收状态");

            //重新登录永辉生活app刷新用户信息
            query = "?platform=ios";
            body = "{\"phonenum\":\"13816043211\",\"securitycode\":\"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
            accessTokenSH = userInfo.getAccess_token();

            Assert.isTrue(userInfo.getToComment() - 1 == toComment, "订单完成后待评价订单总数没有加1");

            //积分校验
            Map<Double, Double> goodsArr = new HashMap<Double, Double>();
            // key为数量，value为价格
            goodsArr.put(1d, 3.00);
            Double tempCredit = ValidateUtil.calculateCredit(goodsArr);
            Assert.isTrue(userInfo.getCredit() - tempCredit == credit, "订单完成后用户积分增加不正确");

            //登出永辉生活app
            query = "?platform=Android&access_token=" + accessTokenSH;
            jsonPath = loginService.loginOutSH(query, 0);


        } catch (Exception e) {
            testcase.setStatus("FAILURE");
            testcase.setDescription(e.getMessage());
            throw e;
        } finally {

        }
    }
}
