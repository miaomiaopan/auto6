package com.yh.qa.testcase;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.dao.OrderDao;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.OutStockOrderInfo;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.service.UserService;
import com.yh.qa.util.DateUtil;
import com.yh.qa.util.RandomString;
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
public class Yh_16 extends BaseTestCase {
    @Autowired
    private LoginService loginService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private UserService userService;

    @Test
    public void yh_16()throws Exception{
        String phoneNum = "13621952294";
        testcase.setTestName("会员店合伙人次日达履单");

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

            // 2. 生成次日达合伙人次日达订单
            //上海市光路1128号临附近-开鲁店-商品256216 # 32346
            String orderQuery = "?channel=qa3&deviceid=867628020935276&platform=Android&timestamp="+System.currentTimeMillis()+"&v=4.2.2.1&access_token="
                    + access_token;
            String orderBody = "{\"balancepayoption\":1,\"device_info\":\"867628020935276\",\"freedeliveryoption\":1,\"paypasswordtype\":0,\"pickself\":0,\"pointpayoption\":0,\"pricetotal\":0,\"products\":[{\"id\":\"M-469690\",\"isbulkitem\":0,\"num\":100,\"pattern\":\"n\"}],\"recvinfo\":{\"address\":{\"area\":\"工农三村\",\"city\":\"上海\",\"cityid\":\"1\",\"detail\":\"401号\"},\"alias\":\"家\",\"foodsupport\":0,\"id\":\"32350\",\"isSearch\":false,\"isdefault\":1,\"itemType\":0,\"location\":{\"lat\":\"31.32927633609778\",\"lng\":\"121.54173259931338\"},\"name\":\"龚\",\"nextdaydeliver\":0,\"phone\":\"13621952293\",\"scope\":0},\"sellerid\":1,\"storeid\":\"9D52\",\"texpecttime\":{\"date\":"+ DateUtil.getTomorrowTimeInMillis()+",\"timeslots\":[{\"from\":\"09:00\",\"slottype\":\"expectTime\",\"to\":\"20:00\"}]},\"totalpayment\":0,\"uid\":\""+uid+"\"}";
            JsonPath result = orderService.confirm(orderQuery, orderBody,0);
            // 获取订单号
            String orderId = result.getString("orderid");

            Thread.sleep(1000);
            //获取用户信息， 验证用户订单数
            String query = "?channel=qa3&deviceid=864854034674759&platform=Android&timestamp="+System.currentTimeMillis()+"&v=4.2.2.2&access_token="+access_token;
            UserInfo info =userService.getInfo(query, 0);
            Assert.isTrue(info.getBalance() + 2390 == balance, "下单支付后用户余额减少数额错误");
            Assert.isTrue(info.getNum() - 1 == num, "下单支付后订单总数没有加1");
            Assert.isTrue(info.getToDelivery() - 1 == toDelivery, "下单后待配送订单总数没有加1");

            // 使用店长9D52角色的账号登录管家APP
            String loginGJQuery = "?platform=android";
            String loginGJBody = "{\"pwd\": \"123456a\", \"username\": \"9D52\"}";
            JsonPath loginGJResult = loginService.loginGJ(loginGJQuery, loginGJBody, 0);
            String accessTokenGJ = loginGJResult.getString("token");
            //获取订单信息，验证订单状态
            validateOrderStatus(orderId,accessTokenGJ, uid, GJOrderStatus.PENDING.getIndex(),"生活APP下次日达合伙人订单付款完成， 管家中订单状态不是待确认状态");

            //得到出库订单信息
            OutStockOrderInfo stockInfo = orderDao.getOutStockOrderInfo(orderId);
            String packageCode = RandomString.getRandomString(20);

            //生成包裹号
            String body = "method=cn.c-scm.wms.feedback.do&data={\"dos\":[{\"feedback\":311,\"code\":\""+stockInfo.getDeveliryId()+"\",\"delivery_code\":\""+stockInfo.getDeveliryCode()+"\",\"express_no\":\"null\",\"packages\":[{\"code\":\""+packageCode+"\",\"express_no\":\"null\",\"weight\":0,\"details\":[{\"sku_code\":\""+stockInfo.getSkuCode()+"\",\"qty\":"+stockInfo.getQty()+"}]}]}]}";
            orderService.pack(body, true);

            //生成出库单号
            String deliverCode = RandomString.getRandomString(15);
            String storied = "9D52";
            String out_body = "method=cn.c-scm.wms.feedback.do&data={\"code\":\"zhuangchedan006\",\"truck_no\":\"1230\",\"delivery_code\":\""+stockInfo.getDeveliryCode()+"\",\"driver\":\"5432\",\"telephone\":\"13661629888\",\"cellphone\":\"4362173\",\"dl_list\":[{\"code\":\""+deliverCode+"\",\"dc_code\":\""+storied+"\",\"vl_list\":[\""+packageCode+"\"]}]}";
            orderService.outstock(out_body, true);
            //获取订单信息，验证订单状态
            validateOrderStatus(orderId,accessTokenGJ, uid, GJOrderStatus.PENDING.getIndex(),"生活APP下当日达合伙人配送单余额支付后管家中查询订单状态不是待确认状态");

            //包裹签收员 签收包裹
            String signPackageQuery = "?access_token=" + accessTokenGJ + "&platform=ios&timestamp=" + System.currentTimeMillis() + "&channel=anything&v=2.4.10.0";
            String signPackageBody = "{\"actions\":[{\"orderid\":\"" + deliverCode + "\",\"packagecode\":\"" + packageCode + "\",\"reason\":\"\",\"status\":1}]}";
            orderService.signPackage(signPackageQuery, signPackageBody, 0);


            //合伙人登录管家App
            // 配送员角色的账号登录管家APP
            String partnerQuery = "?platform=android";
            String partnerBody = "{\"pwd\": \"123456a\", \"username\": \"13621952282\"}";
            JsonPath partnerResult = loginService.loginGJ(partnerQuery, partnerBody, 0);
            String partnerToken = partnerResult.getString("token");
            Thread.sleep(2000);
            // 调用管家的订单详情接口获取订单状态
            validateOrderStatus(orderId,partnerToken, uid, GJOrderStatus.PENDING.getIndex(),"管家中分配派送后订单状态不是待确认状态");

            // 合伙人接包裹
            partnerQuery = "?platform=ios&access_token=" + partnerToken+"&timestamp="+System.currentTimeMillis();;
            String packageActionExpress1Body = "{\"code\":\"" + packageCode + "\",\"action\":1}";
            orderService.packageAction(partnerQuery, packageActionExpress1Body, 0);
            // 调用管家的订单详情接口获取订单状态
            //validateOrderStatus(orderId,accessTokenGJ, uid,GJOrderStatus.READY_TO_PICKUP.getIndex(),"管家中分配派送员完成后订单状态不是待提状态");

            Thread.sleep(60000);
            //合伙人提包裹
            String packageActionExpress2Body = "{\"code\":\"" + packageCode + "\",\"action\":2}";
            orderService.packageAction(partnerQuery, packageActionExpress2Body, 0);

            Thread.sleep(5000);
            //合伙人核销包裹
            String packageActionExpress3Body = "{\"code\":\"" + packageCode + "\",\"action\":3,\"memo\":\"40.079815,116.325135\"}";
            orderService.packageAction(partnerQuery, packageActionExpress3Body, 0);

            Thread.sleep(2000);
            //获取订单信息，验证订单状态
            validateOrderStatus(orderId, partnerToken, uid, GJOrderStatus.COMPLETE.getIndex(), "管家中核销完成后订单状态不是已核销状态");

            // 重新登录永辉生活APP刷新用户信息
            String queryNew = "?platform=ios";
            String bodyNew = "{\"phonenum\": \""+phoneNum+"\", \"securitycode\": \"601933\"}";
            UserInfo userInfoNew = loginService.loginSHAndGetUserInfo(queryNew, bodyNew, 0);
            Assert.isTrue(userInfoNew.getToComment() - 1 == toComment, "核销后待评价订单总数没有加1");

            // 积分校验
            Map<Double, Double> goodsArr = new HashMap<Double, Double>();
            // key为数量，value为价格
            goodsArr.put(1d, 23.90);
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
