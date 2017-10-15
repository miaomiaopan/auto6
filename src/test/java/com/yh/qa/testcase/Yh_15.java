package com.yh.qa.testcase;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.service.UserService;
import com.yh.qa.util.DateUtil;
import com.yh.qa.util.ValidateUtil;
import io.restassured.path.json.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  matt gong
 */
public class Yh_15 extends BaseTestCase {
    @Autowired
    private LoginService loginService;
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Test
    public void yh_15()throws Exception{
        String phoneNum = "13621952292";
        testcase.setTestName("会员店合伙人当日达履单");

        try{
            // 1、登录永辉生活app
            String loginQuery = "?platform=ios";
            String loginBody = "{\"phonenum\": \""+phoneNum+"\", \"securitycode\": \"601933\"}";
            UserInfo userInfo = loginService.loginSHAndGetUserInfo(loginQuery, loginBody, 0);
            // 获取下个请求需要的值
            String access_token = userInfo.getAccess_token();
            String uid = userInfo.getUId();
            int balance = userInfo.getBalance();  //余额
            Double credit = userInfo.getCredit(); //积分
            int num = userInfo.getNum();  //订单总数
            int toComment = userInfo.getToComment(); // 待评价订单数量
            int toDelivery = userInfo.getToDelivery();

            // 2. 生成合伙人当日达订单
            //上海市光路1128号临附近-开鲁店-商品256216
            String orderQuery = "?channel=qa3&deviceid=867628020935276&platform=Android&timestamp="+System.currentTimeMillis()+"&v=4.2.2.1&access_token="
                    + access_token;
            String orderBody = "{\"balancepayoption\":1,\"device_info\":\"867628020935276\",\"freedeliveryoption\":1,\"paypasswordtype\":0,\"pickself\":0,\"pointpayoption\":0,\"pricetotal\":0,\"products\":[{\"id\":\"256216\",\"isbulkitem\":0,\"num\":200,\"pattern\":\"t\"}],\"recvinfo\":{\"address\":{\"area\":\"工农三村\",\"city\":\"上海\",\"cityid\":\"1\",\"detail\":\"5号301\"},\"alias\":\"家\",\"foodsupport\":0,\"id\":\"32333\",\"isSearch\":false,\"isdefault\":1,\"itemType\":0,\"location\":{\"lat\":\"31.32927633609778\",\"lng\":\"121.54173259931338\"},\"name\":\"龚\",\"nextdaydeliver\":0,\"phone\":\"13621952292\",\"scope\":0},\"sellerid\":2,\"storeid\":\"9D52\",\"texpecttime\":{\"date\":"+ DateUtil.getTodyTimeInMillis()+",\"timeslots\":[{\"immediatedesc\":\"最快30分钟达\",\"slottype\":\"immediate\"}]},\"totalpayment\":0,\"uid\":\""+uid+"\"}";
            JsonPath result = orderService.confirm(orderQuery, orderBody,0);
            // 获取订单号
            String orderId = result.getString("orderid");

            //获取用户信息， 验证用户订单数
            String query = "?channel=qa3&deviceid=864854034674759&platform=Android&timestamp="+System.currentTimeMillis()+"&v=4.2.2.2&access_token="+access_token;
            UserInfo info =userService.getInfo(query, 0);
            Assert.isTrue(info.getBalance() + 1940 == balance, "下单支付后用户余额减少数额错误");
            Assert.isTrue(info.getNum() - 1 == num, "下单支付后订单总数没有加1");
            Assert.isTrue(info.getToDelivery() - 1 == toDelivery, "下单后待自提订单总数没有加1");


            // 使用店长9D52角色的账号登录管家APP
            String loginGJQuery = "?platform=android";
            String loginGJBody = "{\"pwd\": \"123456a\", \"username\": \"9D52\"}";
            JsonPath loginGJResult = loginService.loginGJ(loginGJQuery, loginGJBody, 0);
            String accessTokenGJ = loginGJResult.getString("token");
            //获取订单信息，验证订单状态
            validateOrderStatus(orderId,accessTokenGJ, uid, GJOrderStatus.PENDING.getIndex(),"生活APP下当日达合伙人配送单余额支付后管家中查询订单状态不是待确认状态");

            // 开始拣货
            String pickQuery = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();;
            String pickBody = "{\"orderid\": \"" + orderId + "\", \"action\": \"7\"}";
            orderService.orderAction(pickQuery, pickBody, 0);

            // 调用管家的订单详情接口获取订单状态
            validateOrderStatus(orderId,accessTokenGJ, uid, GJOrderStatus.START_PACK.getIndex(),"管家中开始拣货后订单状态不是开始拣货状态");

            // 拣货完成
            String pickFinishQuery = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();;
            String pickFinishBody = "{\"orderid\": \"" + orderId + "\", \"action\": \"8\"}";
            orderService.orderAction(pickFinishQuery, pickFinishBody, 0);

            //合伙人登录管家App
            // 配送员角色的账号登录管家APP
            String partnerQuery = "?platform=android";
            String partnerBody = "{\"pwd\": \"123456a\", \"username\": \"13621952282\"}";
            JsonPath partnerResult = loginService.loginGJ(partnerQuery, partnerBody, 0);
            String partnerToken = partnerResult.getString("token");
            Thread.sleep(2000);
            // 调用管家的订单详情接口获取订单状态
            validateOrderStatus(orderId,accessTokenGJ, uid, GJOrderStatus.WAITING_TAKE.getIndex(),"管家中分配派送后订单状态不是待接单状态");

            // 合伙人接单
            partnerQuery = "?platform=ios&access_token=" + partnerToken+"&timestamp="+System.currentTimeMillis();;
            partnerBody = "{\"orderid\": \"" + orderId + "\", \"action\": \"1\"}";
            orderService.orderAction(partnerQuery, partnerBody, 0);
            // 调用管家的订单详情接口获取订单状态
            validateOrderStatus(orderId,accessTokenGJ, uid, GJOrderStatus.READY_TO_PICKUP.getIndex(),"管家中分配派送员完成后订单状态不是待提状态");

            // 合伙人提单
            partnerQuery = "?platform=ios&access_token=" + partnerToken+"&timestamp="+System.currentTimeMillis();;
            partnerBody = "{\"orderid\": \"" + orderId + "\", \"action\": \"2\"}";
            orderService.orderAction(partnerQuery, partnerBody, 0);
            // 调用管家的订单详情接口获取订单状态
            validateOrderStatus(orderId,accessTokenGJ, uid, GJOrderStatus.PICKUP.getIndex(),"管家派送员提单完成后订单状态不是待核销状态");

            // 核销
            partnerQuery = "?platform=ios&access_token=" + partnerToken+"&timestamp="+System.currentTimeMillis();;
            partnerBody = "{\"orderid\": \"" + orderId + "\", \"action\": \"3\"}";
            orderService.orderAction(partnerQuery, partnerBody, 0);
            // 调用管家的订单详情接口获取订单状态
            validateOrderStatus(orderId,accessTokenGJ, uid, GJOrderStatus.COMPLETE.getIndex(),"管家派送员提单完成后订单状态不是核销状态");


            // 重新登录永辉生活APP刷新用户信息
            String queryNew = "?platform=ios";
            String bodyNew = "{\"phonenum\": \""+phoneNum+"\", \"securitycode\": \"601933\"}";
            UserInfo userInfoNew = loginService.loginSHAndGetUserInfo(queryNew, bodyNew, 0);
            Assert.isTrue(userInfoNew.getToComment() - 1 == toComment, "核销后待评价订单总数没有加1");

            // 积分校验
            Map<Double, Double> goodsArr = new HashMap<Double, Double>();
            // key为数量，value为价格
            goodsArr.put(1d, 19.40);
            Double tempCredit = ValidateUtil.calculateCredit(goodsArr);
            Assert.isTrue(userInfoNew.getCredit() - tempCredit == credit, "核销后用户积分增加不正确");

        }catch(Exception e){
            testcase.setStatus("FAIL");
            testcase.setDescription(e.getMessage());
            throw e;
        }

    }

    // 调用管家的订单详情接口获取订单状态
    private void validateOrderStatus(String orderId, String accessTokenGJ, String uid, int index, String message) throws Exception {
        String query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
        JsonPath jsonPath = orderService.detailGj(query, 0);
        int status = jsonPath.getInt("status");
        Assert.isTrue(status == index, message);
    }
}
