package com.yh.qa.testcase;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.service.UserService;
import com.yh.qa.util.CalculateUtil;
import com.yh.qa.util.DateUtil;
import com.yh.qa.util.ValidateUtil;
import io.restassured.path.json.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/***
 * @author  matt gong
 * 会员店非合伙人当日达自提单履单
 */
public class Yh_2 extends BaseTestCase {
    @Autowired
    private LoginService loginService;
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Test
    public void yh_2()throws Exception {
        String phoneNum = "13621952290";
        // case名称
        testcase.setTestName("会员店非合伙人当日达自提单履单");

        try{
            // 1、登陆永辉生活app，获取用户的账号余额、积分、优惠券、订单信息
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
            int toPickup = userInfo.getToPickup();

            // 2. 生成当日达自提订单
            // 开鲁路店 - 商品853743
            String orderQuery = "?channel=qa3&deviceid=867628020935276&platform=Android&timestamp="+System.currentTimeMillis()+"&v=4.2.2.1&access_token="
                    + access_token;
            String orderBody = "{\"balancepayoption\":1,\"device_info\":\"867628020935276\",\"freedeliveryoption\":1,\"paypasswordtype\":0,\"pickself\":1,\"pointpayoption\":0,\"pricetotal\":0,\"products\":[{\"id\":\"853743\",\"isbulkitem\":0,\"num\":100,\"pattern\":\"t\"}],\"recvinfo\":{\"address\":{},\"foodsupport\":0,\"isSearch\":false,\"isdefault\":0,\"itemType\":0,\"location\":{\"lat\":\"\",\"lng\":\"\"},\"name\":\""+phoneNum+"\",\"nextdaydeliver\":0,\"phone\":\""+phoneNum+"\",\"scope\":0},\"sellerid\":2,\"storeid\":\"9D52\",\"texpecttime\":{\"date\":"+DateUtil.getTodyTimeInMillis()+",\"timeslots\":[{\"immediatedesc\":\"立即自提\",\"slottype\":\"immediate\"}]},\"totalpayment\":0,\"uid\":\""+uid+"\"}";
            JsonPath result = orderService.confirm(orderQuery, orderBody,0);
            // 获取订单号
            String orderId = result.getString("orderid");

            //获取用户信息， 验证用户订单数
            String query = "?channel=qa3&deviceid=864854034674759&platform=Android&timestamp="+System.currentTimeMillis()+"&v=4.2.2.2&access_token="+access_token;
            UserInfo info =userService.getInfo(query,uid, 0);
            Assert.isTrue(info.getBalance() + 1080 == balance, "下单支付后用户余额减少数额错误");
            Assert.isTrue(info.getNum() - 1 == num, "下单支付后订单总数没有加1");
            Assert.isTrue(info.getToPickup() - 1 == toPickup, "下单后待自提订单总数没有加1");
            //获取订单信息，验证订单状态
            String order_query = "?platform=Android&orderid=" + orderId + "&access_token=" + access_token;
            JsonPath order = orderService.detail(order_query, 0);
            int status = order.getInt("status");
            Assert.isTrue(status == 4, "生活APP下当日达非合伙人自提单余额支付后订单状态不是待自提");

            //3. 使用拥有店长角色的账号登录管家APP
            String loginGJQuery = "?platform=android";
            String loginGJBody = "{\"pwd\": \"123456a\", \"username\": \"9D52\"}";
            result = loginService.loginGJ(loginGJQuery, loginGJBody,0);
            //获取管家token
            String GJ_token = result.getString("token");
            ValidateUtil.validateGJOrderStatus(GJOrderStatus.PENDING,"app下单后",orderService,orderId, GJ_token,uid,null);

            //4. 扫码核销
            String orderActionQuery = "?platform=android&timestamp="+System.currentTimeMillis()+"&channel=anything&v=2.4.10.0&access_token="+GJ_token;
            String OrderActionBody = "{\"orderid\": \"" + orderId + "\", \"action\": \"3\"}";
            orderService.orderAction(orderActionQuery, OrderActionBody,0);
            ValidateUtil.validateGJOrderStatus(GJOrderStatus.COMPLETE,"app核销后",orderService,orderId, GJ_token,uid,null);

            //核销后，验证订单状态
            order = orderService.detail(order_query, 0);
            status = order.getInt("status");
            Assert.isTrue(status == 5, "生活APP下当日达非合伙人自提单余额支付后订单状态不是待自提");
            //核销后，验证待评价订单数
            String query_after_action = "?channel=qa3&deviceid=864854034674759&platform=Android&timestamp="+System.currentTimeMillis()+"&v=4.2.2.2&access_token="+access_token;
            UserInfo info2 =userService.getInfo(query_after_action,uid, 0);
            Assert.isTrue(info2.getToComment() - 1 == toComment, "核销后待评价订单总数没有加1");

            Thread.sleep(5000);
            // 积分校验
            Map<Double, Double> goodsArr = new HashMap<>();
            // key为数量，value为价格
            goodsArr.put(1d, 10.80);
            Double tempCredit = ValidateUtil.calculateCredit(goodsArr);
            Assert.isTrue(CalculateUtil.sub(info2.getCredit(),tempCredit) == new BigDecimal(credit).doubleValue(), "核销后用户积分增加不正确，原来"+credit+",增加"+tempCredit+",现在"+info2.getCredit());

        }catch (Exception e){
            testcase.setStatus("FAILURE");
            testcase.setDescription(e.getMessage());
            throw e;
        }
    }

}
