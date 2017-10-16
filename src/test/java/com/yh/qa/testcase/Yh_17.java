package com.yh.qa.testcase;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.dao.OrderDao;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.OutStockOrderInfo;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.service.UserService;
import com.yh.qa.util.CalculateUtil;
import com.yh.qa.util.DateUtil;
import com.yh.qa.util.RandomString;
import com.yh.qa.util.ValidateUtil;
import io.restassured.path.json.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author matt Gong
 */
public class Yh_17 extends BaseTestCase {
    @Autowired
    private LoginService loginService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDao orderDao;

    @Test
    public void Yh_17() throws Exception {
        String phoneNum = "13621952295";

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

        // case名称
        testcase.setTestName("会员店非合伙人次日达配送包裹履单(B2B2C)");

        try {
            // 1、登录永辉生活app
            String loginQuery = "?platform=ios";
            String loginBody = "{\"phonenum\": \""+phoneNum+"\", \"securitycode\": \"601933\"}";
            UserInfo userInfo = loginService.loginSHAndGetUserInfo(loginQuery, loginBody, 0);
            // 获取下个请求需要的值
            String access_token = userInfo.getAccess_token();
            balance = userInfo.getBalance();
            credit = userInfo.getCredit();
            num = userInfo.getNum();
            toComment = userInfo.getToComment();
            toDelivery = userInfo.getToDelivery();
            String uid = userInfo.getUId();

            // 2. 生成次日达配送订单
            //上海市杨浦新湾新城-开鲁店-商品M-386569
            String orderQuery = "?channel=2&deviceid=0151EBBD-5436-48CB-A7D3-91E087F04E28&uid=" + uid + "&platform=ios&v=4.2.3.3&timestamp=" + System.currentTimeMillis() + "&access_token=" + access_token;
            String orderBody = "{\"balancepayoption\":1,\"device_info\":\"867628020935276\",\"freedeliveryoption\":1,\"paypasswordtype\":0,\"pickself\":0,\"pointpayoption\":0,\"pricetotal\":0,\"products\":[{\"id\":\"M-839323\",\"isbulkitem\":0,\"num\":100,\"pattern\":\"n\"}],\"recvinfo\":{\"address\":{\"area\":\"开鲁四、六村\",\"city\":\"上海\",\"cityid\":\"1\",\"detail\":\"8棟201\"},\"alias\":\"家\",\"foodsupport\":0,\"id\":\"32351\",\"isSearch\":false,\"isdefault\":1,\"itemType\":0,\"location\":{\"lat\":\"31.332414831071024\",\"lng\":\"121.5368188681738\"},\"name\":\"龚\",\"nextdaydeliver\":0,\"phone\":\""+phoneNum+"\",\"scope\":0},\"sellerid\":1,\"storeid\":\"9D52\",\"texpecttime\":{\"date\":"+ DateUtil.getTomorrowTimeInMillis()+",\"timeslots\":[{\"from\":\"09:00\",\"slottype\":\"expectTime\",\"to\":\"20:00\"}]},\"totalpayment\":0,\"uid\":\""+uid+"\"}";
            JsonPath jsonPath = orderService.confirm(orderQuery, orderBody, 0);
            Thread.sleep(1000);

            //获取用户信息，验证用户订单数，用户余额
            String query = "?channel=qa3&deviceid=864854034674759&platform=Android&timestamp=" + System.currentTimeMillis() + "&v=4.2.2.2&access_token=" + access_token;
            UserInfo info = userService.getInfo(query, uid,0);
            Assert.isTrue(info.getBalance() + 2390 == balance, "下单支付后用户余额减少数额错误");
            Assert.isTrue(info.getNum() - 1 == num, "下单支付后订单总数没有加1");
            Assert.isTrue(info.getToDelivery() - 1 == toDelivery, "下单后配送订单没有加1");

            // 获取订单号
            String orderId = jsonPath.getString("orderid");

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
            ValidateUtil.validateGJOrderStatus(GJOrderStatus.PENDING,"app下单后",orderService,orderId, accessTokenGJ,uid,null);

            //店长签收包裹
            String packageQuery = "?access_token=" + accessTokenGJ + "&platform=ios&timestamp=" + System.currentTimeMillis() + "&channel=anything&v=2.4.10.0";
            String signPackageBody = "{\"actions\":[{\"orderid\":\"" + deliverCode + "\",\"packagecode\":\"" + packageCode + "\",\"reason\":\"\",\"status\":1}]}";
            orderService.signPackage(packageQuery, signPackageBody, 0);

            Thread.sleep(60000);
            //店长提包裹
            String packageActionBody = "{\"code\":\"" + packageCode + "\",\"action\":2}";
            orderService.packageAction(packageQuery, packageActionBody, 0);

            //店长核销包裹
            String packageActionExpress3Body = "{\"code\":\"" + packageCode + "\",\"action\":3,\"memo\":\"31.332414831071024,121.5368188681738\"}";
            orderService.packageAction(packageQuery, packageActionExpress3Body, 0);
            //获取订单信息，验证订单状态
            ValidateUtil.validateGJOrderStatus(GJOrderStatus.COMPLETE,"合伙人核销包裹后",orderService,orderId, accessTokenGJ,uid,null);


            // 重新登录永辉生活APP刷新用户信息
            String queryNew = "?platform=ios";
            String bodyNew = "{\"phonenum\": \""+phoneNum+"\", \"securitycode\": \"601933\"}";
            UserInfo userInfoNew = loginService.loginSHAndGetUserInfo(queryNew, bodyNew, 0);

            Assert.isTrue(userInfoNew.getToComment() - 1 == toComment, "核销后待评价订单总数没有加1");

            // 积分校验
            Map<Double, Double> goodsArr = new HashMap<>();
            // key为数量，value为价格
            goodsArr.put(1d, 23.90);
            Double tempCredit = ValidateUtil.calculateCredit(goodsArr);
            System.out.println(userInfoNew.getCredit());
            System.out.println(tempCredit);
            System.out.println(credit);
            System.out.println((userInfoNew.getCredit() - tempCredit) == credit);
            Assert.isTrue(CalculateUtil.sub(userInfoNew.getCredit(),tempCredit) == new BigDecimal(credit).doubleValue(), "核销后用户积分增加不正确，原来"+credit+",增加"+tempCredit+",现在"+userInfoNew.getCredit());
        } catch (Exception e) {
            testcase.setStatus("FAIL");
            testcase.setDescription(e.getMessage());
            throw e;
        }
    }

    // 调用管家的订单详情接口获取订单状态
    private void validateOrderStatus(String orderId, String accessTokenGJ, String uid, int index, String message) throws Exception {
        Thread.sleep(2000);
        String query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
        JsonPath jsonPath = orderService.detailGj(query, 0);
        int status = jsonPath.getInt("status");
        Assert.isTrue(status == index, message+",而是"+status);
    }
}
