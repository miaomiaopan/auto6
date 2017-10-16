package com.yh.qa.testcase;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.dao.OrderDao;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.service.UserService;
import com.yh.qa.util.ValidateUtil;
import io.restassured.path.json.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

/**
 * @author matt Gong on 2017/10/13
 */
public class Yh_27 extends BaseTestCase{
    @Autowired
    private LoginService loginService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDao orderDao;

    @Test
    public void yh_27() throws Exception {
        String phoneNum = "13621952297";
        String productId = "B-466920";

        // 余额
        int balance;
        // 积分
        Double credit;
        // 订单总数
        int num;
        // 待评价订单数量
        int toComment;
        // 待配送订单数量
        int toPick;

        // case名称
        testcase.setTestName("Bravo集波履单，非自配送，自提单拒收");

        try{
            // 1、登录永辉生活app
            String loginQuery = "?platform=ios";
            String loginBody = "{\"phonenum\": \"" + phoneNum + "\", \"securitycode\": \"601933\"}";
            UserInfo userInfo = loginService.loginSHAndGetUserInfo(loginQuery, loginBody, 0);
            // 获取下个请求需要的值
            String access_token = userInfo.getAccess_token();
            balance = userInfo.getBalance();
            credit = userInfo.getCredit();
            num = userInfo.getNum();
            toComment = userInfo.getToComment();
            toPick = userInfo.getToPickup();
            String uid = userInfo.getUId();

            // 2.生成bravo绿标店当日达自提订单
            //上海市嘉定大融城店-商品M-
            String orderQuery = "?channel=2&deviceid=0151EBBD-5436-48CB-A7D3-91E087F04E28&uid=" + uid + "&platform=ios&v=4.2.3.3&timestamp=" + System.currentTimeMillis() + "&access_token=" + access_token;
            String orderBody = "{\"balancepayoption\":1,\"device_info\":\"867628020935276\",\"freedeliveryoption\":1,\"paypasswordtype\":0,\"pickself\":1,\"pointpayoption\":0,\"pricetotal\":0,\"products\":[{\"id\":\""+productId+"\",\"isbulkitem\":0,\"num\":100,\"pattern\":\"t\"}],\"recvinfo\":{\"address\":{},\"foodsupport\":0,\"isSearch\":false,\"isdefault\":0,\"itemType\":0,\"location\":{\"lat\":\"\",\"lng\":\"\"},\"name\":\"\",\"nextdaydeliver\":0,\"phone\":\""+phoneNum+"\",\"scope\":0},\"sellerid\":3,\"storeid\":\"9485\",\"texpecttime\":{\"date\":1507824000000,\"timeslots\":[{\"immediatedesc\":\"立即自提\",\"slottype\":\"immediate\"}]},\"totalpayment\":0,\"uid\":\""+uid+"\"}";
            JsonPath jsonPath = orderService.confirm(orderQuery, orderBody, 0);
            Thread.sleep(1000);

            //获取用户信息，验证用户订单数，用户余额
            String query = "?channel=qa3&deviceid=864854034674759&platform=Android&timestamp=" + System.currentTimeMillis() + "&v=4.2.2.2&access_token=" + access_token;
            UserInfo info = userService.getInfo(query,uid, 0);
            Assert.isTrue(info.getBalance() + 1980 == balance, "下单支付后用户余额减少数额错误");
            Assert.isTrue(info.getNum() - 1 == num, "下单支付后订单总数没有加1");
            Assert.isTrue(info.getToPickup() - 1 == toPick, "下单后配送订单没有加1");

            // 获取订单号
            String orderId = jsonPath.getString("orderid");

            // 使用拥有bravo拣货员角色的账号登录管家APP进行拣货
            String loginGJQuery = "?platform=android";
            String loginGJBody = "{\"pwd\": \"123456a\", \"username\": \"9485100\"}";
            JsonPath loginGJResult = loginService.loginGJ(loginGJQuery, loginGJBody, 0);
            String accessTokenGJ = loginGJResult.getString("token");
            //获取订单信息，验证订单状态
            ValidateUtil.validateGJOrderStatus(GJOrderStatus.PENDING,"下单后",orderService,orderId, accessTokenGJ,uid,null);

            // 获取待接单的波次号信息，最大等待1分钟
            String waveId = orderService.getDelayedWaveIdByOrderId(orderId,1);

            //开始集波拣货
            String packQuery = "/" + waveId + "?" + "waveId=" + waveId + "&access_token=" + accessTokenGJ + "&timestamp="
                    + System.currentTimeMillis() + "&platform=ios&channel=qa3";
            String packBody = "{\"waveId\": \"" + waveId + "\"}";
            orderService.startPack(packQuery, packBody, 0);
            //获取订单信息，验证订单状态
            ValidateUtil.validateGJOrderStatus(GJOrderStatus.START_PACK,"开始拣货后",orderService,orderId, accessTokenGJ,uid,null);

            //完成拣货
            orderService.completePack(packQuery, packBody, 0);
            Thread.sleep(5000);
            //获取订单信息，验证订单状态
            ValidateUtil.validateGJOrderStatus(GJOrderStatus.READY_TO_PICKUP,"完成拣货后",orderService,orderId, accessTokenGJ,uid,null);
            Thread.sleep(20000);

            // 使用拥有bravo自提员角色的账号登录管家APP进行操作
            String loginGJPickQuery = "?platform=android";
            String loginGJPickBody = "{\"pwd\": \"123456a\", \"username\": \"9485400\"}";
            JsonPath loginGJPickResult = loginService.loginGJ(loginGJPickQuery, loginGJPickBody, 0);
            String accessTokenGJPick = loginGJPickResult.getString("token");


            //获取订单信息，验证订单状态,并获取itmeId
            String itmeId = validateOrderStatus(orderId, accessTokenGJPick, uid, GJOrderStatus.READY_TO_PICKUP.getIndex(), "拣货完成后订单状态不是等待提货状态");

            //自提员登记拒收
            String pickQuery = "?platform=android&timestamp="+System.currentTimeMillis()+"&channel=anything&v=2.4.10.0&access_token="+accessTokenGJPick ;
            String pickBody = "{\"orderid\":\""+orderId+"\",\"items\":{\""+itmeId+"\":{\"id\":\""+productId+"\",\"num\":100}},\"comment\":\"不要了\"}";
            orderService.partialReturn(pickQuery,pickBody,0);
            //获取订单信息，验证订单状态
            ValidateUtil.validateGJOrderStatus(GJOrderStatus.REFUNDING,"自提员登记拒收后",orderService,orderId, accessTokenGJ,uid,null);

            //拣货员售后单，退款
            String serviceId = orderDao.getServiceOrderIdByOrderId(orderId);
            String returnQuery = "?platform=android&timestamp="+System.currentTimeMillis()+"&channel=anything&v=2.4.10.0&access_token="+accessTokenGJ ;
            String returnBody = "{\"serviceorderid\":"+serviceId+",\"memo\":\"\",\"action\":1}";
            orderService.action(returnQuery,returnBody,0);
            //获取订单信息，验证订单状态
            ValidateUtil.validateGJOrderStatus(GJOrderStatus.ORDER_RETURNED,"退款后",orderService,orderId, accessTokenGJ,uid,null);

            Thread.sleep(5000);
            // 重新登录永辉生活APP刷新用户信息
            String queryNew = "?platform=ios";
            String bodyNew = "{\"phonenum\": \""+phoneNum+"\", \"securitycode\": \"601933\"}";
            UserInfo userInfoNew = loginService.loginSHAndGetUserInfo(queryNew, bodyNew, 0);
            Assert.isTrue(userInfoNew.getBalance()  == balance, "退款后余额和原来不一样");


        }catch(Exception e){
            testcase.setStatus("FAILURE");
            testcase.setDescription(e.getMessage());
            throw e;
        }
    }

    // 调用管家的订单详情接口获取订单状态
    private String validateOrderStatus(String orderId, String accessTokenGJ, String uid, int index, String message) throws Exception {
        Thread.sleep(2000);
        String query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
        JsonPath jsonPath = orderService.detailGj(query, 0);
        int status = jsonPath.getInt("status");
        Assert.isTrue(status == index, message);
        String itemId = jsonPath.getString("products[0].itemid");
        return itemId;
    }
}
