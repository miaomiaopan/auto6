package com.yh.qa.testcase;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.dao.OrderDao;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.GJPackageStatus;
import com.yh.qa.entity.OrderDb;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.service.UserService;
import com.yh.qa.util.CalculateUtil;
import com.yh.qa.util.RandomString;
import com.yh.qa.util.ShopAccount;
import com.yh.qa.util.ValidateUtil;
import io.restassured.path.json.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 会员店、Bravo-不同商品类自动拆单，次日达
 */
public class Yh_4 extends BaseTestCase {
    private static Logger logger = LoggerFactory.getLogger(Yh_4.class);

    @Autowired
    private LoginService loginService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDao orderDao;

    @Test
    public void Yh_4() throws Exception {
        String phoneNum = "15555156675";

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

        // case名称
        testcase.setTestName("会员店、Bravo-不同商品类缺货拆单，次日达");

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
            //门店是北京回龙观店
            String orderQuery = "?channel=2&deviceid=0151EBBD-5436-48CB-A7D3-91E087F04E28&uid=" + uid + "&platform=ios&v=4.2.3.3&timestamp=" + System.currentTimeMillis() + "&access_token=" + access_token;
            String orderBody = "{\"pointpayoption\":0,\"autocoupon\":0,\"uid\":\"" + uid + "\",\"texpecttime\":{\"date\":1507564800000,\"timeslots\":[{\"to\":\"20:00\",\"slottype\":\"expectTime\",\"from\":\"09:00\"}]},\"freedeliveryoption\":1,\"device_info\":\"FF2B0CA1-7C30-4AF8-95C1-DDD179F572E1\",\"sellerid\":\"1\",\"comment\":\"\",\"recvinfo\":{\"isdefault\":1,\"phone\":\""+phoneNum+"\",\"alias\":\"家\",\"id\":\"32255\",\"location\":{\"lat\":\"40.079815\",\"lng\":\"116.325135\"},\"address\":{\"detail\":\"3\",\"area\":\"永辉超市(回龙观店)\",\"city\":\"北京\",\"cityid\":1},\"scope\":0,\"name\":\"刘伟\"},\"products\":[{\"id\":\"M-152239\",\"num\":100},{\"id\":\"M-924260\",\"num\":100}],\"pickself\":0,\"totalpayment\":0,\"balancepayoption\":1,\"storeid\":\""+ ShopAccount.BRAVO_BJ_HUILONGGUAN_SHOP_ID+"\"}";
            JsonPath jsonPath = orderService.confirm(orderQuery, orderBody, 0);

            //获取用户信息，验证用户订单数，用户余额，待自提订单
            String query = "?channel=qa3&deviceid=864854034674759&platform=Android&timestamp=" + System.currentTimeMillis() + "&v=4.2.2.2&access_token=" + access_token;
            UserInfo info = userService.getInfo(query, uid,0);
            Assert.isTrue(info.getBalance() + 10450 == balance, "下单支付后用户余额减少数额错误");
            Assert.isTrue(info.getNum() - 2 == num, "下单支付后订单总数没有加1");
            Assert.isTrue(info.getToDelivery() - 2 == toDelivery, "下单后待自提订单总数没有加1");

            // 获取订单号
            String parentOrderId = jsonPath.getString("orderid");

            List<OrderDb> outStockOrder = orderDao.getOutStockOrderForChildByParentOrder(parentOrderId);

            for (int i = 0; i < outStockOrder.size(); i++) {
                logger.info("处理第 {} 个商品", i + 1);

                String childOrderId = outStockOrder.get(i).getChildOrderId();

                String loginGJQuery = "?platform=android";
                //店长登录管家
                String loginGJShopKeeperBody = "{\"pwd\": \"123456a\", \"username\": \""+ShopAccount.BRAVO_BJ_HUILONGGUAN_DIANZHANG+"\"}";
                jsonPath = loginService.loginGJ(loginGJQuery, loginGJShopKeeperBody, 0);
                //获取管家token
                String shopKeeperToken = jsonPath.getString("token");

                Thread.sleep(2000);

                //获取订单信息，验证订单状态
                orderService.validateOrderStatusGJ(outStockOrder.get(i).getChildOrderId(), shopKeeperToken, uid, 0, GJOrderStatus.PENDING.getIndex(), "生活APP次日达非合伙人管家中查询订单状态不是待确认状态,ChildOrderId:"+childOrderId);

                //子订单出库单号
                String outStockOrderId = outStockOrder.get(i).getOutStockOrderId();

                //20位随机包裹号
                String packageCode = RandomString.getRandomString(20);

                //生成包裹号
                String packageBody = "method=cn.c-scm.wms.feedback.do&data={\"dos\":[{\"feedback\":311,\"code\":\"" + outStockOrderId + "\",\"delivery_code\":\"YHWL\",\"express_no\":\"null\",\"packages\":[{\"code\":\"" + packageCode + "\",\"express_no\":\"null\",\"weight\":0,\"details\":[{\"sku_code\":\"" + outStockOrder.get(i).getSkuCode() + "\",\"qty\":1}]}]}]}";
                orderService.pack(packageBody, true);

                Thread.sleep(1000);

                //验证包裹状态
                //orderService.validatePackageStatusGJ(packageCode, shopKeeperToken, 0, GJPackageStatus.WAITING_TAKE.getIndex(), "==");

                String deliverCode = RandomString.getRandomString(15);
                //生成出库单号
                String out_body = "method=cn.c-scm.wms.feedback.do&data={\"code\":\"zhuangchedan006\",\"truck_no\":\"1230\",\"delivery_code\":\"YHWL\",\"driver\":\"5432\",\"telephone\":\"13661629888\",\"cellphone\":\"4362173\",\"dl_list\":[{\"code\":\"" + deliverCode + "\",\"dc_code\":\""+ShopAccount.BRAVO_BJ_HUILONGGUAN_SHOP_ID+"\",\"vl_list\":[\"" + packageCode + "\"]}]}";
                orderService.outstock(out_body, true);

                Thread.sleep(1000);

                //管家操作
                //包裹签收员登录
                //String loginGJQuery = "?platform=android";
                String loginGJBody = "{\"pwd\": \"123456a\", \"username\": \""+ShopAccount.BRAVO_BJ_HUILONGGUAN_QIANSHOUYUAN+"\"}";
                jsonPath = loginService.loginGJ(loginGJQuery, loginGJBody, 0);
                //获取管家token
                String signTokenGJ = jsonPath.getString("token");

                //验证包裹状态
                //orderService.validatePackageStatusGJ(packageCode, signTokenGJ, 0, GJPackageStatus.WAITING_TAKE.getIndex(), "==");

                //包裹签收员 签收包裹
                String signPackageQuery = "?access_token=" + signTokenGJ + "&platform=ios&timestamp=" + System.currentTimeMillis() + "&channel=anything&v=2.4.10.0";
                String signPackageBody = "{\"actions\":[{\"orderid\":\"" + deliverCode + "\",\"packagecode\":\"" + packageCode + "\",\"reason\":\"\",\"status\":1}]}";
                orderService.signPackage(signPackageQuery, signPackageBody, 0);

                Thread.sleep(2000);

                //验证包裹状态
                orderService.validatePackageStatusGJ(packageCode, shopKeeperToken, 0, GJPackageStatus.WAITING_ASSIGN.getIndex(), "包裹不是等待分配状态,childOrderId:"+childOrderId);

                ////店长登录管家
                //String loginGJShopKeeperBody = "{\"pwd\": \"123456a\", \"username\": \"9167\"}";
                //jsonPath = loginService.loginGJ(loginGJQuery, loginGJShopKeeperBody, 0);
                ////获取管家token
                //String shopKeeperToken = jsonPath.getString("token");

                //店长派送包裹
                String packageActionQuery = "?access_token=" + shopKeeperToken + "&platform=ios&timestamp=" + System.currentTimeMillis() + "&channel=anything&v=2.4.10.0";
                String packageActionBody = "{\"code\":\"" + packageCode + "\",\"assignto\":50012031,\"action\":5}";
                orderService.packageAction(packageActionQuery, packageActionBody, 0);

                Thread.sleep(2000);

                //配送员 登录管家
                String loginGJExpressBody = "{\"pwd\": \"123456a\", \"username\": \""+ShopAccount.BRAVO_BJ_HUILONGGUAN_PEISONGYUAN+"\"}";
                jsonPath = loginService.loginGJ(loginGJQuery, loginGJExpressBody, 0);
                //获取管家token
                String expressToken = jsonPath.getString("token");

                //验证包裹状态
                orderService.validatePackageStatusGJ(packageCode, expressToken, 0, GJPackageStatus.WAITING_TAKE.getIndex(), "包裹不是待接状态,childOrderId:"+childOrderId);

                String packageActionExpressQuery = "?access_token=" + expressToken + "&platform=ios&timestamp=" + System.currentTimeMillis() + "&channel=anything&v=2.4.10.0";

                //配送员 接包裹
                String packageActionExpress1Body = "{\"code\":\"" + packageCode + "\",\"action\":1}";
                orderService.packageAction(packageActionExpressQuery, packageActionExpress1Body, 0);

                Thread.sleep(60000);

                //验证包裹状态
                orderService.validatePackageStatusGJ(packageCode, expressToken, 0, GJPackageStatus.READY_TO_PICKUP.getIndex(), "包裹不是待提状态,childOrderId:"+childOrderId);

                //配送员 提包裹
                String packageActionExpress2Body = "{\"code\":\"" + packageCode + "\",\"action\":2}";
                orderService.packageAction(packageActionExpressQuery, packageActionExpress2Body, 0);

                Thread.sleep(5000);

                //验证包裹状态
                orderService.validatePackageStatusGJ(packageCode, expressToken, 0, GJPackageStatus.WAITING_CANCEL_DEBT.getIndex(), "包裹不是待核销状态,childOrderId:"+childOrderId);

                //配送员 核销包裹
                String packageActionExpress3Body = "{\"code\":\"" + packageCode + "\",\"action\":3,\"memo\":\"40.079815,116.325135\"}";
                orderService.packageAction(packageActionExpressQuery, packageActionExpress3Body, 0);

                Thread.sleep(2000);
                //获取订单信息，验证订单状态
                orderService.validateOrderStatusGJ(outStockOrder.get(i).getChildOrderId(), expressToken, uid, 0, GJPackageStatus.COMPLETE.getIndex(), "管家中核销完成后订单状态不是已核销状态,ChildOrderId:"+childOrderId);

            }

            // 重新登录永辉生活APP刷新用户信息
            String queryNew = "?platform=ios";
            String bodyNew = "{\"phonenum\": \""+phoneNum+"\", \"securitycode\": \"601933\"}";
            UserInfo userInfoNew = loginService.loginSHAndGetUserInfo(queryNew, bodyNew, 0);

            Assert.isTrue(userInfoNew.getToComment() - 2 == toComment, "核销后待评价订单总数没有加1");

            // 积分校验
            Map<Double, Double> goodsArr = new HashMap<Double, Double>();
            // key为数量，value为价格
            goodsArr.put(1d, 104.5);
            Double tempCredit = ValidateUtil.calculateCredit(goodsArr);
            Assert.isTrue(CalculateUtil.sub(userInfoNew.getCredit(),tempCredit) == new BigDecimal(credit).doubleValue(), "核销后用户积分增加不正确，原来"+credit+",增加"+tempCredit+",现在"+userInfoNew.getCredit());
        } catch (Exception e) {
            testcase.setStatus("FAIL");
            testcase.setDescription(e.getMessage());
            throw e;
        }
    }

}
