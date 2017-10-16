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
public class Yh_5 extends BaseTestCase {
    @Autowired
    private LoginService loginService;
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDao orderDao;

    @Test
    public void yh5() throws Exception {
        String phoneNum = "13621952291";

        // case名称
        testcase.setTestName("会员店非合伙人次日达自提单履单");

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
            int toPickup = userInfo.getToPickup();

            // 2. 生成次日达自提订单
            //上海唐镇-金湘店-波士顿龙虾
            String orderQuery = "?channel=qa3&deviceid=867628020935276&platform=Android&timestamp="+System.currentTimeMillis()+"&v=4.2.2.1&access_token="
                    + access_token;
            //String orderBody = "{\"balancepayoption\":1,\"device_info\":\"867628020935276\",\"freedeliveryoption\":1,\"paypasswordtype\":0,\"pickself\":0,\"pointpayoption\":0,\"pricetotal\":0,\"products\":[{\"id\":\"M-924260\",\"isbulkitem\":0,\"num\":100,\"pattern\":\"n\"}],\"recvinfo\":{\"address\":{\"area\":\"上海润欣科技有限公司\",\"city\":\"上海\",\"cityid\":\"1\",\"detail\":\"3楼\"},\"alias\":\"公司\",\"foodsupport\":0,\"id\":\"32321\",\"isSearch\":false,\"isdefault\":1,\"itemType\":0,\"location\":{\"lat\":\"31.17488955848173\",\"lng\":\"121.41398457273941\"},\"name\":\"龚建飞\",\"nextdaydeliver\":0,\"phone\":\"13621952291\",\"scope\":0},\"sellerid\":1,\"storeid\":\"9D13\",\"texpecttime\":{\"date\":\""+DateUtil.getTomorrowTimeInMillis()+"\",\"timeslots\":[{\"from\":\"09:00\",\"slottype\":\"expectTime\",\"to\":\"20:00\"}]},\"totalpayment\":0,\"uid\":\""+uid+"\"}";
            String orderBody = "{\"balancepayoption\":1,\"device_info\":\"867628020935276\",\"freedeliveryoption\":1,\"paypasswordtype\":0,\"pickself\":1,\"pointpayoption\":0,\"pricetotal\":0,\"products\":[{\"id\":\"M-924260\",\"isbulkitem\":0,\"num\":100,\"pattern\":\"n\"}],\"recvinfo\":{\"address\":{},\"foodsupport\":0,\"isSearch\":false,\"isdefault\":0,\"itemType\":0,\"location\":{\"lat\":\"\",\"lng\":\"\"},\"name\":\""+phoneNum+"\",\"nextdaydeliver\":0,\"phone\":\""+phoneNum+"\",\"scope\":0},\"sellerid\":1,\"storeid\":\"9D31\",\"texpecttime\":{\"date\":"+ DateUtil.getTomorrowTimeInMillis()+",\"timeslots\":[{\"from\":\"09:00\",\"slottype\":\"expectTime\",\"to\":\"20:00\"}]},\"totalpayment\":0,\"uid\":\""+uid+"\"}";
            JsonPath result = orderService.confirm(orderQuery, orderBody,0);
            // 获取订单号
            String orderId = result.getString("orderid");

            //获取用户信息， 验证用户订单数
            String query = "?channel=qa3&deviceid=864854034674759&platform=Android&timestamp="+System.currentTimeMillis()+"&v=4.2.2.2&access_token="+access_token;
            UserInfo info =userService.getInfo(query,uid, 0);
            Assert.isTrue(info.getBalance() + 9900 == balance, "下单支付后用户余额减少数额错误");
            Assert.isTrue(info.getNum() - 1 == num, "下单支付后订单总数没有加1");
            Assert.isTrue(info.getToPickup() - 1 == toPickup, "下单后待自提订单总数没有加1");
            //获取订单信息，验证订单状态
            validateOrderStatus(orderId,access_token, GJOrderStatus.TAKE.getIndex(),"生活APP下次日达非合伙人自提单余额支付后订单状态不是拣货中");

            //得到出库订单信息
            OutStockOrderInfo stockInfo = orderDao.getOutStockOrderInfo(orderId);
            String packageCode = RandomString.getRandomString(20);

            //生成包裹号
            String body = "method=cn.c-scm.wms.feedback.do&data={\"dos\":[{\"feedback\":311,\"code\":\""+stockInfo.getDeveliryId()+"\",\"delivery_code\":\""+stockInfo.getDeveliryCode()+"\",\"express_no\":\"null\",\"packages\":[{\"code\":\""+packageCode+"\",\"express_no\":\"null\",\"weight\":0,\"details\":[{\"sku_code\":\""+stockInfo.getSkuCode()+"\",\"qty\":"+stockInfo.getQty()+"}]}]}]}";
            orderService.pack(body, true);

            //生成出库单号
            String deliverCode = RandomString.getRandomString(15);
            String storied = "9D31";
            String out_body = "method=cn.c-scm.wms.feedback.do&data={\"code\":\"zhuangchedan006\",\"truck_no\":\"1230\",\"delivery_code\":\""+stockInfo.getDeveliryCode()+"\",\"driver\":\"5432\",\"telephone\":\"13661629888\",\"cellphone\":\"4362173\",\"dl_list\":[{\"code\":\""+deliverCode+"\",\"dc_code\":\""+storied+"\",\"vl_list\":[\""+packageCode+"\"]}]}";
            orderService.outstock(out_body, true);
            //出库后，获取订单信息，验证订单状态
            validateOrderStatus(orderId,access_token, GJOrderStatus.WAITING_THIRDPARTY_TAKE.getIndex(),"生活APP下次日达非合伙人自提单出库后，订单状态不是待至店");

            //使用拥有店长角色的账号登录管家APP
            String loginGJQuery = "?platform=android";
            String loginGJBody = "{\"pwd\": \"123456a\", \"username\": \"9D31\"}";
            result = loginService.loginGJ(loginGJQuery, loginGJBody,0);
            //获取管家token
            String GJ_token = result.getString("token");

            //4. 店长收包裹
            String signPackageQuery = "?access_token="+GJ_token+"&platform=android&timestamp="+System.currentTimeMillis()+"&channel=anything&v=2.4.10.0";
            String signPackageBody = "{\"actions\":[{\"orderid\":\""+deliverCode+"\",\"packagecode\":\""+packageCode+"\",\"reason\":\"\",\"status\":1}]}";
            orderService.signPackage(signPackageQuery,signPackageBody,0);
            //签收包裹后，验证订单状态
            ValidateUtil.validateGJOrderStatus(GJOrderStatus.PENDING,"收包裹后",orderService,orderId, GJ_token,uid,null);
            //validateOrderStatus(orderId,access_token, GJOrderStatus.PICKUP.getIndex(),"订单验证后的状态不是已签收");

            //店长核销
            String packageActionQuery = "?access_token="+GJ_token+"&platform=android&timestamp="+System.currentTimeMillis()+"&channel=anything&v=2.4.10.0";
            String packageActionBody = "{\"code\":\""+packageCode+"\",\"action\":3}";
            orderService.packageAction(packageActionQuery,packageActionBody,0);
            ValidateUtil.validateGJOrderStatus(GJOrderStatus.COMPLETE,"app核销后",orderService,orderId, GJ_token,uid,null);


            //核销后验证订单状态, 评论的订单验证
            validateOrderStatus(orderId,access_token, GJOrderStatus.COMPLETE.getIndex(),"订单核销后的状态不是已核销");
            String query_after_action = "?channel=qa3&deviceid=864854034674759&platform=Android&timestamp="+System.currentTimeMillis()+"&v=4.2.2.2&access_token="+access_token;
            UserInfo info2 =userService.getInfo(query_after_action,uid, 0);
            Assert.isTrue(info2.getToComment() - 1 == toComment, "核销后待评价订单总数没有加1");

            // 积分校验
            Map<Double, Double> goodsArr = new HashMap<Double, Double>();
            //key为数量，value为价格
            goodsArr.put(1d,99.0);
            Double tempCredit = ValidateUtil.calculateCredit(goodsArr);
            Assert.isTrue(CalculateUtil.sub(info2.getCredit(),tempCredit) == new BigDecimal(credit).doubleValue(), "核销后用户积分增加不正确，原来"+credit+",增加"+tempCredit+",现在"+info2.getCredit());
        }catch (Exception e){
            testcase.setStatus("FAILURE");
            testcase.setDescription(e.getMessage());
            throw e;
        }
    }

    private void validateOrderStatus(String orderId, String access_token,int code, String message) throws Exception{
        String order_query = "?platform=Android&orderid=" + orderId + "&access_token=" + access_token;
        JsonPath order = orderService.detail(order_query, 0);
        int status = order.getInt("status");
        Assert.isTrue(status == code, message);
    }

}
