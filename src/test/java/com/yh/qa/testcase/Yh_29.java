package com.yh.qa.testcase;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.dao.OrderDao;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.service.UserService;
import com.yh.qa.util.ShopAccount;
import io.restassured.path.json.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author liuwei
 *
 * @date 2017年10月13日
 */
@SpringBootTest
public class Yh_29 extends BaseTestCase {
    @Autowired
    private LoginService loginService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDao orderDao;

    @Test
    public void Yh_29() throws Exception {
        String phoneNum = "15555156675";
        JsonPath jsonPath = null;
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
            testcase.setTestName("拣货完成后，用户操作退货");

            // 1、登录永辉生活app
            String loginQuery = "?platform=ios";
            String loginBody = "{\"phonenum\": \""+phoneNum+"\", \"securitycode\": \"601933\"}";
            UserInfo userInfo = loginService.loginSHAndGetUserInfo(loginQuery, loginBody, 0);

            // 获取下个请求需要的值
            String loginTokenSH = userInfo.getAccess_token();
            balance = userInfo.getBalance();
            credit = userInfo.getCredit();
            num = userInfo.getNum();
            toComment = userInfo.getToComment();
            toDelivery = userInfo.getToDelivery();
            String uid = userInfo.getUId();

            // 2. 生成配送订单
            String orderQuery = "?channel=2&deviceid=0151EBBD-5436-48CB-A7D3-91E087F04E28&uid=" + uid + "&platform=ios&v=4.2.3.3&timestamp=" + System.currentTimeMillis() + "&access_token=" + loginTokenSH;
            String orderBody = "{\"pointpayoption\":0,\"autocoupon\":0,\"uid\":\"414736872086072799\",\"texpecttime\":{\"date\":1507824000000,\"timeslots\":[{\"to\":\"\",\"slottype\":\"immediate\",\"form\":\"\"}]},\"freedeliveryoption\":1,\"device_info\":\"FF2B0CA1-7C30-4AF8-95C1-DDD179F572E1\",\"sellerid\":\"3\",\"comment\":\"\",\"recvinfo\":{\"phone\":\"15555156675\",\"isdefault\":1,\"location\":{\"lat\":\"31.281255\",\"lng\":\"121.549872\"},\"id\":\"32363\",\"scope\":0,\"address\":{\"detail\":\"3\",\"area\":\"永辉超市(隆昌路店)\",\"city\":\"上海\",\"cityid\":1},\"name\":\"刘伟\"},\"products\":[{\"id\":\"B-689650\",\"num\":100}],\"pickself\":0,\"totalpayment\":0,\"balancepayoption\":1,\"storeid\":\"9442\"}";

            jsonPath = orderService.confirm(orderQuery, orderBody, 0);

            //获取用户信息，验证用户订单数，用户余额，待自提订单
            String userInfoQuery = "?channel=qa3&deviceid=864854034674759&platform=Android&timestamp=" + System.currentTimeMillis() + "&v=4.2.2.2&access_token=" + loginTokenSH;
            UserInfo info = userService.getInfo(userInfoQuery, 0);
            Assert.isTrue(info.getBalance() + 1990 == balance, "下单支付后用户余额减少数额错误");
            Assert.isTrue(info.getNum() - 1 == num, "下单支付后订单总数没有加1");
            Assert.isTrue(info.getToDelivery() - 1 == toDelivery, "下单后待配送订单总数没有加1");

            // 获取订单号
            String orderId = jsonPath.getString("orderid");

            //管家操作
            String loginGJQuery = "?platform=android";

            // 使用拣货员角色的账号登录管家APP
            String loginGJPickupBody = "{\"pwd\": \"123456a\", \"username\": \""+ShopAccount.BRAVO_SH_LONGCHANGLU_JIANHUOYUAN+"\"}";
            jsonPath = loginService.loginGJ(loginGJQuery, loginGJPickupBody, 0);
            //获取管家token
            String pickupTokenGJ = jsonPath.getString("token");

            //获取订单信息，验证订单状态
            orderService.validateOrderStatusGJ(orderId, pickupTokenGJ, uid, 0, GJOrderStatus.PENDING.getIndex(), "生活APP下当日达非合伙人配送单余额支付后管家中查询订单状态不是待确认状态,orderId:"+orderId);

            // 开始拣货
            String pickupQuery = "?platform=ios&access_token=" + pickupTokenGJ+"&timestamp="+System.currentTimeMillis();
            String pickupBodyBegin = "{\"orderid\": \"" + orderId + "\", \"action\": \"7\"}";
            orderService.orderAction(pickupQuery, pickupBodyBegin, 0);

            TimeUnit.SECONDS.sleep(5);

            orderService.validateOrderStatusGJ(orderId, pickupTokenGJ, uid, 0, GJOrderStatus.START_PACK.getIndex(), "管家中开始拣货后订单状态不是开始拣货状态,orderId:"+orderId);

            // 拣货完成
            //query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();
            String pickupBodyEnd = "{\"orderid\": \"" + orderId + "\", \"action\": \"8\"}";
            orderService.orderAction(pickupQuery, pickupBodyEnd, 0);

            //生活app申请退款
            String applyRefundQuery = "?channel=2&deviceid=0151EBBD-5436-48CB-A7D3-91E087F04E28&uid="+uid+"&platform=ios&v=4.2.3.3&access_token="+loginTokenSH+"&timestamp="+System.currentTimeMillis();
            String applyRefundBody = "{\"orderid\":\""+orderId+"\",\"reason\":\"商品缺货\"}";
            orderService.applyRefund(applyRefundQuery,applyRefundBody,0);

            TimeUnit.SECONDS.sleep(10);

            List<String> orderIds = new ArrayList<String>();
            orderIds.add(orderId);

            // 获取售后单id
            String serviceOrderId = orderDao.getServiceOrderIdByOrderId(orderIds.get(0));

            // 售后单退款
            String afterSaleQuery = "?access_token=" + pickupTokenGJ + "&timestamp=" + System.currentTimeMillis() + "&platform=ios&channel=qa3";
            String afterSaleBody = "{\"serviceorderid\":" + serviceOrderId + ",\"memo\":\"\",\"action\":1}";
            orderService.action(afterSaleQuery, afterSaleBody, 0);

            TimeUnit.SECONDS.sleep(20);
            orderService.validateOrderStatusGJ(orderId, pickupTokenGJ, uid, 0, GJOrderStatus.ORDER_RETURNED.getIndex(), "管家中不是 订单退款或退货的状态,orderId:"+orderId);

            // 重新登录永辉生活APP刷新用户信息
            String queryNew = "?platform=ios";
            String bodyNew = "{\"phonenum\": \""+phoneNum+"\", \"securitycode\":\"601933\"}";
            UserInfo userInfoNew = loginService.loginSHAndGetUserInfo(queryNew, bodyNew, 0);

            Assert.isTrue(userInfoNew.getBalance() == balance, "退款后用户余额增加数额错误");

        } catch (Exception e) {
            testcase.setStatus("FAIL");
            testcase.setDescription(e.getMessage());
            throw e;
        } finally {
            // TODO 刪除测试数据
        }
    }

}