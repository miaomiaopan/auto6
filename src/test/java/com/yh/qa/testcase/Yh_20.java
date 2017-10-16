package com.yh.qa.testcase;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.dao.OrderDao;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.service.UserService;
import com.yh.qa.util.RandomString;
import com.yh.qa.util.ShopAccount;
import com.yh.qa.util.ValidateUtil;
import io.restassured.path.json.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author liuwei
 *
 * @date 2017年10月12日
 */
@SpringBootTest
public class Yh_20 extends BaseTestCase {
    @Autowired
    private LoginService loginService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDao orderDao;

    @Test
    public void Yh_20() throws Exception {
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
            testcase.setTestName("自配送系统派单给配送员");

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
            String orderBody = "{\"pointpayoption\":0,\"autocoupon\":0,\"uid\":\""+uid+"\",\"texpecttime\":{\"date\":1507737600000,\"timeslots\":[{\"to\":\"\",\"slottype\":\"immediate\",\"form\":\"\"}]},\"freedeliveryoption\":1,\"device_info\":\"FF2B0CA1-7C30-4AF8-95C1-DDD179F572E1\",\"sellerid\":\"3\",\"comment\":\"\",\"recvinfo\":{\"phone\":\"15555156675\",\"isdefault\":0,\"location\":{\"lat\":\"31.326512\",\"lng\":\"121.478500\"},\"id\":\"32339\",\"scope\":0,\"address\":{\"detail\":\"3\",\"area\":\"永辉超市(长江国际店)\",\"city\":\"上海\",\"cityid\":1},\"name\":\"刘伟\"},\"products\":[{\"id\":\"B-156668\",\"num\":100}],\"pickself\":0,\"totalpayment\":0,\"balancepayoption\":1,\"storeid\":\""+ ShopAccount.BRAVO_SH_CHANGJIANGGUOJI_SHOP_ID +"\"}";

            jsonPath = orderService.confirm(orderQuery, orderBody, 0);

            //获取用户信息，验证用户订单数，用户余额，待自提订单
            String userInfoQuery = "?channel=qa3&deviceid=864854034674759&platform=Android&timestamp=" + System.currentTimeMillis() + "&v=4.2.2.2&access_token=" + loginTokenSH;
            UserInfo info = userService.getInfo(userInfoQuery,uid, 0);
            Assert.isTrue(info.getBalance() + 1880 == balance, "下单支付后用户余额减少数额错误");
            Assert.isTrue(info.getNum() - 1 == num, "下单支付后订单总数没有加1");
            Assert.isTrue(info.getToDelivery() - 1 == toDelivery, "下单后待配送订单总数没有加1");

            // 获取订单号
            String orderId = jsonPath.getString("orderid");

            //管家操作
            String loginGJQuery = "?platform=android";

            // 使用拣货员角色的账号登录管家APP
            String loginGJPickupBody = "{\"pwd\": \"123456a\", \"username\": \""+ShopAccount.BRAVO_SH_CHANGJIANGGUOJI_JIANHUOYUAN+"\"}";
            jsonPath = loginService.loginGJ(loginGJQuery, loginGJPickupBody, 0);
            //获取管家token
            String pickupTokenGJ = jsonPath.getString("token");

            //获取订单信息，验证订单状态
            orderService.validateOrderStatusGJ(orderId, pickupTokenGJ, uid, 0, GJOrderStatus.PENDING.getIndex(), "生活APP下当日达非合伙人配送单余额支付后管家中查询订单状态不是待确认状态,orderId:"+orderId);

            // 开始拣货
            String pickupQuery = "?platform=ios&access_token=" + pickupTokenGJ+"&timestamp="+System.currentTimeMillis();;
            String pickupBodyBegin = "{\"orderid\": \"" + orderId + "\", \"action\": \"7\"}";
            orderService.orderAction(pickupQuery, pickupBodyBegin, 0);

            TimeUnit.SECONDS.sleep(5);

            orderService.validateOrderStatusGJ(orderId, pickupTokenGJ, uid, 0, GJOrderStatus.START_PACK.getIndex(), "管家中开始拣货后订单状态不是开始拣货状态,orderId:"+orderId);

            // 拣货完成
            //query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();
            String pickupBodyEnd = "{\"orderid\": \"" + orderId + "\", \"action\": \"8\"}";
            orderService.orderAction(pickupQuery, pickupBodyEnd, 0);

            //validateOrderStatusGJ(orderId, pickupTokenGJ, uid, 0, GJOrderStatus.WAITING_ASSIGN.getIndex(), "==");

            //等待系统自动派送间隔最少3分钟
            TimeUnit.MINUTES.sleep(8);

            // 分配派送员
            //query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();;
            //body = "{\"orderid\": \"" + orderId + "\",\"assignto\": \"50012059\", \"action\": \"5\"}";
            //jsonPath = orderService.orderAction(query, body, 0);

            // 配送员角色的账号登录管家APP
            //String loginGJQuery = "?platform=android";
            String dispatchBody = "{\"pwd\": \"123456a\", \"username\": \""+ShopAccount.BRAVO_SH_CHANGJIANGGUOJI_PEISONGYUAN+"\"}";
            jsonPath = loginService.loginGJ(loginGJQuery, dispatchBody, 0);
            String dispatchTokenGJ = jsonPath.getString("token");

            //String orderId = "1204130310063000";
            orderService.validateOrderStatusGJ(orderId, dispatchTokenGJ, uid, 0, GJOrderStatus.READY_TO_PICKUP.getIndex(), "管家中分配派送员完成后订单状态不是待提状态,orderId:"+orderId);

            // 派送员提单
            String getQuery = "?platform=ios&access_token=" + dispatchTokenGJ+"&timestamp="+System.currentTimeMillis();
            //String getBody = "{\"orderid\": \"" + orderId + "\", \"action\": \"2\"}";
            //orderService.orderAction(getQuery, getBody, 0);

            List<String> orderIds = new ArrayList<String>();
            orderIds.add(orderId);
            // 根据订单号获取批次号
            List<String> batchIds = orderDao.getBatchIdsByOrderIds(orderIds);
            if (batchIds.isEmpty()) {
                throw new Exception("没有找到批次号，需要加上等候时间");
            }
            // 根据批次号获取整批提货需要的数据
            Map<String, List<String>> batchInfo = orderDao.getMapByBatchIds(batchIds);
            // 遍历批次，整批提货每个批次
            for (String batchId : batchInfo.keySet()) {
                //String getQuery = "?platform=ios&access_token=" + dispatchTokenGJ + "&timestamp="+System.currentTimeMillis();
                String getBody = "{\"action\":1,\"batchid\":" + batchId + ",\"orderids\":[" + RandomString.getStringFromList(batchInfo.get(batchId), true) + "]}";

                jsonPath = orderService.batchAction(getQuery, getBody, 0);
            }

            // 调用管家的订单详情接口获取订单状态
            //for (String orderIdTmp : orderIds) {
                //query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
                //jsonPath = orderService.detailGj(query, 0);
                //status = jsonPath.getInt("status");
                //Assert.isTrue(status == GJOrderStatus.PICKUP.getIndex(), "提货后的状态不是提货的状态");
            //}

            TimeUnit.SECONDS.sleep(5);

            orderService.validateOrderStatusGJ(orderId, dispatchTokenGJ, uid, 0, GJOrderStatus.PICKUP.getIndex(), "管家派送员提单完成后订单状态不是待核销状态,orderId:"+orderId);

            // 核销
            //query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();
            String verifyBody = "{\"orderid\": \"" + orderId + "\", \"action\": \"3\"}";
            orderService.orderAction(getQuery, verifyBody, 0);

            // 调用管家的订单详情接口获取订单状态
            //query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            //jsonPath = orderService.detailGj(query, 0);
            //status = jsonPath.getInt("status");
            //Assert.isTrue(status == GJOrderStatus.COMPLETE.getIndex(), "管家中核销完成后订单状态不是已核销状态");

            orderService.validateOrderStatusGJ(orderId, dispatchTokenGJ, uid, 0, GJOrderStatus.COMPLETE.getIndex(), "管家中核销完成后订单状态不是已核销状态,orderId:"+orderId);

            // 重新登录永辉生活APP刷新用户信息
            String queryNew = "?platform=ios";
            String bodyNew = "{\"phonenum\": \""+phoneNum+"\", \"securitycode\": \"601933\"}";
            UserInfo userInfoNew = loginService.loginSHAndGetUserInfo(queryNew, bodyNew, 0);

            Assert.isTrue(userInfoNew.getToComment() - 1 == toComment, "核销后待评价订单总数没有加1");

            TimeUnit.SECONDS.sleep(30);
            // 积分校验
            Map<Double, Double> goodsArr = new HashMap<Double, Double>();
            // key为数量，value为价格
            goodsArr.put(1d, 12.8);
            Double tempCredit = ValidateUtil.calculateCredit(goodsArr);
            Assert.isTrue(userInfoNew.getCredit() - tempCredit == credit, "核销后用户积分增加不正确");

        } catch (Exception e) {
            testcase.setStatus("FAILURE");
            testcase.setDescription(e.getMessage());
            throw e;
        } finally {
            // TODO 刪除测试数据
        }
    }

}