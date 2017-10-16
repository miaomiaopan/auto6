package com.yh.qa.testcase;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.dao.OrderDao;
import com.yh.qa.datasource.DataSourceTemplete;
import com.yh.qa.entity.Case;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.OrderDb;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.repository.CaseRepository;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.service.UserService;
import com.yh.qa.util.CalculateUtil;
import com.yh.qa.util.RandomString;
import com.yh.qa.util.ResultBean;
import com.yh.qa.util.ValidateUtil;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/***
 *
 * @author 范文华
 * @data   2017-10-12
 * @desc   Bravo 自提包裹履单（B2B2C)
 */

@SpringBootTest
public class Yh_10 extends BaseTestCase {
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
    private static Logger logger = LoggerFactory.getLogger(Yh_10.class);

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

    @Test(description = "Bravo 配送包裹履单（B2B2C)")
    public void testBravoSelfPickOrderFlowing() throws Exception{
        String uri = "";
        String body = "";

        try{
            testcase.setTestName("Bravo 配送包裹履单（B2B2C)");
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

            //2. 调用永辉生活下单接口，余额支付成功后登出。 北京回龙观店 9167

            uri = "?uid=21286&channel=2&deviceid=B6B8ACE9-1167-4550-9672-EF9BA9C6AF55&platform=ios&v=4.2.3.3&timestamp=" + System.currentTimeMillis() + "&sign=6732c96163dcc5aae62c01942c08f648&access_token=" + accessTokenSH;
            body = "{\"pointpayoption\":0,\"autocoupon\":0,\"uid\":\"" + uid + "\",\"texpecttime\":{\"date\":" + System.currentTimeMillis() + ",\"timeslots\":[{\"to\":\"20:00\",\"slottype\":\"expectTime\",\"from\":\"09:00\"}]},\"freedeliveryoption\":1,\"device_info\":\"FF2B0CA1-7C30-4AF8-95C1-DDD179F572E1\",\"sellerid\":\"1\",\"comment\":\"\",\"recvinfo\":{\"isdefault\":1,\"phone\":\"13661629812\",\"alias\":\"家\",\"id\":\"32256\",\"location\":{\"lat\":\"40.079815\",\"lng\":\"116.325135\"},\"address\":{\"detail\":\"3\",\"area\":\"永辉超市(回龙观店)\",\"city\":\"北京\",\"cityid\":1},\"scope\":0,\"name\":\"回龙观\"},\"products\":[{\"id\":\"M-152239\",\"num\":100}],\"pickself\":0,\"totalpayment\":0,\"balancepayoption\":1,\"storeid\":\"9167\"}";

            jsonPath = orderService.confirm(uri, body, 0);
            orderNumber = jsonPath.getString("orderid");
            logger.info("订单 {} 创建成功", orderNumber);

            // 登出永辉生活app
            uri = "?platform=Android&access_token=" + accessTokenSH;
            loginService.loginOutSH(uri, 0);

            //3. 生成包裹号和出库单号

            // 获取出库单号
            Thread.sleep(1000);
            String outlibID = "";
            List<String> outStockID = null;
            outStockID = jdbcTemplateOrderDb.queryForList("SELECT id FROM t_trade_outstock where order_id =" + this.orderNumber, String.class);
            for (String outid : outStockID){
                outlibID = outid;
            }

            //生成包裹号
            Thread.sleep(2000);
            String packageCode = RandomString.getRandomString(20);
            body = "method=cn.c-scm.wms.feedback.do&data={\"dos\":[{\"feedback\":311,\"code\":\"" + outlibID + "\",\"delivery_code\":\"YHWL\",\"express_no\":\"null\",\"packages\":[{\"code\":\"" + packageCode + "\",\"express_no\":\"null\",\"weight\":0,\"details\":[{\"sku_code\":\"" + StringUtils.substringAfter(skuCode, "-")  + "\",\"qty\":1}]}]}]}";
            System.out.println("生成包裹号 body: " + body);
            orderService.pack(body, true);

            //生成出库单号
            String deliverCode = RandomString.getRandomString(15);
            body = "method=cn.c-scm.wms.feedback.do&data={\"code\":\"zhuangchedan006\",\"truck_no\":\"1230\",\"delivery_code\":\"YHWL\",\"driver\":\"5432\",\"telephone\":\"13661629888\",\"cellphone\":\"4362173\",\"dl_list\":[{\"code\":\"" + deliverCode + "\",\"dc_code\":\"9167\",\"vl_list\":[\"" + packageCode + "\"]}]}";
            orderService.outstock(body, true);

            //4. 包裹签收员登录，签收包裹
            uri = "?platform=android";
            body = "{\"pwd\": \"123456a\", \"username\": \"9167300\"}";
            jsonPath = loginService.loginGJ(uri, body, 0);
            accessTokenGJ = jsonPath.getString("token");
            uri = "?access_token=" + accessTokenGJ + "&platform=ios&timestamp=" + System.currentTimeMillis() + "&channel=anything&v=2.4.10.0";
            body = "{\"actions\":[{\"orderid\":\"" + deliverCode + "\",\"packagecode\":\"" + packageCode + "\",\"reason\":\"\",\"status\":1}]}";
            orderService.signPackage(uri, body, 0);
            Thread.sleep(2000);

            //5. 拣货员或者店长登录APP派单
            uri = "?platform=android";
            body = "{\"pwd\": \"123456a\", \"username\": \"9167100\"}";
            jsonPath = loginService.loginGJ(uri, body, 0);
            accessTokenGJ = jsonPath.getString("token");
            uri = "?access_token=" + accessTokenGJ + "&platform=ios&timestamp=" + System.currentTimeMillis() + "&channel=anything&v=2.4.10.0";
            body = "{\"code\":\"" + packageCode + "\", \"action\": \"5\", \"assignto\":" + ReceiverID + "}";
            orderService.packageAction(uri, body, 0);
            Thread.sleep(2000);

            //6. 配送员登录APP接单送货后核销

            uri = "?platform=android";
            body = "{\"pwd\": \"123456a\", \"username\": \"9167200\"}";
            jsonPath = loginService.loginGJ(uri, body, 0);
            accessTokenGJ = jsonPath.getString("token");
            // 调用管家的订单详情接口获取订单状态
            uri = "?platform=Android&orderid=" + orderNumber + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(uri, 0);
            status = jsonPath.getInt("status");
//            Assert.isTrue(status == GJOrderStatus.WAITING_TAKE.getIndex(), "派单完成后，订单状态不是等待接单状态");
            // 开始接单
            Thread.sleep(2000);
            String uriPackage = "?access_token=" + accessTokenGJ + "&platform=ios&timestamp=" + System.currentTimeMillis() + "&channel=anything&v=2.4.10.0";
            body = "{\"code\":\"" + packageCode + "\",\"action\":1}";
            jsonPath = orderService.packageAction(uriPackage, body, 0);
            // 调用管家的订单详情接口获取订单状态
            uri = "?platform=Android&orderid=" + orderNumber + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(uri, 0);
            status = jsonPath.getInt("status");
//            Assert.isTrue(status == GJOrderStatus.READY_TO_PICKUP.getIndex(), "接单完成后，订单状态不是等待提货状态");
            // 开始提包裹
            Thread.sleep(66000);
            logger.info("包裹号是 {} ", packageCode);
            body = "{\"code\":\"" + packageCode + "\",\"action\":2}";
            jsonPath = orderService.packageAction(uriPackage, body, 0);
            // 调用管家的订单详情接口获取订单状态
            uri = "?platform=Android&orderid=" + orderNumber + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(uri, 0);
            status = jsonPath.getInt("status");
//            Assert.isTrue(status == GJOrderStatus.PICKUP.getIndex(), "提货完成后，订单状态不是已经提货状态");
            // 开始核销
            Thread.sleep(2000);
            body = "{\"code\":\"" + packageCode + "\",\"action\":3,\"memo\":\"40.079815,116.325135\"}";
            jsonPath = orderService.packageAction(uriPackage, body, 0);
            // 调用管家的订单详情接口获取订单状态
            uri = "?platform=Android&orderid=" + orderNumber + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(uri, 0);
            status = jsonPath.getInt("status");
//            Assert.isTrue(status == GJOrderStatus.COMPLETE.getIndex(), "订单不是已经核销状态");

            // 重新登录永辉生活APP刷新用户信息
            uri = "?platform=ios";
            body = "{\"phonenum\": \"13661629813\", \"securitycode\": \"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(uri, body, 0);
            accessTokenSH = userInfo.getAccess_token();
            Assert.isTrue(userInfo.getToComment() - 1 == toComment, "核销后待评价订单总数没有加1");

            // 积分校验
            Map<Double, Double> goodsArr = new HashMap<Double, Double>();
            // key为数量，value为价格
            goodsArr.put(1d, 5.50);
            Double tempCredit = ValidateUtil.calculateCredit(goodsArr);
            Assert.isTrue(CalculateUtil.sub(userInfo.getCredit(),tempCredit) == new BigDecimal(credit).doubleValue(), "核销后用户积分增加不正确，原来"+credit+",增加"+tempCredit+",现在"+userInfo.getCredit());
            // 登出永辉生活app
            uri = "?platform=Android&access_token=" + accessTokenSH;
            jsonPath = loginService.loginOutSH(uri, 0);
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