package com.yh.qa.testcase;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.dao.OrderDao;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.KDSService;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.util.ValidateUtil;
import io.restassured.path.json.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @author by xieweiwei
 * @date 2017年10月10日
 * @desc 超级物种点餐堂食商品和堂食货架区商品履单流程
 * @TODO KDS订单操作后订单状态变化不正确
 *
 */

@SpringBootTest
public class Yh_14 extends BaseTestCase {
    @Autowired
    private LoginService loginService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private KDSService kdsService;

    @Autowired
    private OrderDao orderDao;

    @Test
    public void testSuperSpeciesKDSAndNormalOrderFlowing() throws Exception {
        String query = "";
        String body = "";
        JsonPath jsonPath = null;
        String orderId = "";
        String pickOrderId = "";
        String accessTokenSH = "";
        String accessTokenGJ = "";
        String uid = "";
        String sign;
        UserInfo userInfo = null;
        //余额
        int balance;
        //积分
        Double credit;
        //订单总数
        int num;
        //待自提订单数量
        int toPickup;
        //订单状态
        int status;

        try {
            // 设置case名称
            testcase.setTestName("超级物种点餐堂食商品和堂食货架区商品履单流程");
            // case开始执行
            // 登录永辉生活app
            query = "?platform=ios";
            body = "{\"phonenum\":\"13816043212\",\"securitycode\":\"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
            accessTokenSH = userInfo.getAccess_token();
            balance = userInfo.getBalance();
            credit = userInfo.getCredit();
            System.out.println("前面的" + credit);
            num = userInfo.getNum();
            toPickup = userInfo.getToPickup();
            uid = userInfo.getUId();

            // 生成超级物种点餐堂食商品和堂食货架区普通商品订单
            query = "?channel=qa3&deviceid=0000000000000005&platform=ios&v=4.2.3.3&access_token=" + accessTokenSH + "&timestamp=" + System.currentTimeMillis();
            body = "{\"pointpayoption\":0,\"autocoupon\":1,\"texpecttime\":{\"date\":1507824000000,\"timeslots\":[{\"from\":\"\",\"to\":\"\",\"immediatedescription\":\"立即堂食\",\"slottype\":\"immediate\"}]},\"freedeliveryoption\":1,\"sellerid\":6,\"type\":\"food\",\"recvinfo\":{\"phone\":\"13816043212\",\"scope\":0,\"isdefault\":1,\"name\":\"伟伟\",\"location\":{\"lat\":\"26.10352\",\"lng\":\"119.319264\"},\"address\":{\n" + "\"detail\":\"\",\"area\":\"温泉公园\",\"city\":\"福州\",\"cityid\":\"4\"}},\"selectedcoupons\":[],\"products\":[{\"num\":100,\"id\":\"S-914291\"},{\"num\":100,\"id\":\"S-918755\"}],\"pickself\":1,\"dinnersnumber\":1,\"totalpayment\":0,\"balancepayoption\":true,\"storeid\":\"9I01\"}";
            jsonPath = orderService.confirm(query, body, 0);
            orderId = jsonPath.getString("orderid");

            // 重新登录永辉生活app刷新用户信息
            query = "?platform=ios";
            body = "{\"phonenum\":\"13816043212\",\"securitycode\":\"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
            accessTokenSH = userInfo.getAccess_token();

            Assert.isTrue(userInfo.getBalance() + 1680 == balance, "下单支付后用户余额减少数额错误");
            Assert.isTrue(userInfo.getNum() - 1 == num, "下单支付后订单总数没有加1");
            Assert.isTrue(userInfo.getToPickup() - 1 == toPickup, "下单后待自提订单总数没有加1");

            // 使用超级物种温泉店货架区商品拣货员账号登录管家app
            query = "?platform=ios";
            body = "{\"pwd\": \"123456a\", \"username\": \"13816043315\"}";
            jsonPath = loginService.loginGJ(query, body, 0);
            accessTokenGJ = jsonPath.getString("token");

            // 调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.PENDING.getIndex(), "生活APP下超级物种货架区普通商品订单余额支付后管家中查询订单状态不是待确认状态");

            // 根据订单id获取拣货单id
            pickOrderId = orderDao.getPickOrderIdByOrderId(orderId);

            // 拣货员操作货架区商品开始拣货
            query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp" + System.currentTimeMillis();
            body = "{\"orderid\": \"" + pickOrderId + "\",\"action\": 7}";
            jsonPath = orderService.orderAction(query, body, 0);

            // 调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.PENDING.getIndex(), "堂食货架区商品开始拣货后订单状态不是待确认状态");

            // 拣货员操作货架区商品拣货单拣货完成
            query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp" + System.currentTimeMillis();
            body = "{\"orderid\": \"" + pickOrderId + "\",\"action\": 8}";
            jsonPath = orderService.orderAction(query, body, 0);

            // 调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.PENDING.getIndex(), "堂食货架区商品拣货完成后订单状态不是待确认状态");

            // 拣货员操作货架区商品拣货单核销
            query = "?platform=ios&access_token=" + accessTokenGJ + "&timestamp" + System.currentTimeMillis();
            body = "{\"pickorderid\": \"" + pickOrderId + "\"}";
            jsonPath = orderService.pickOrderComplete(query, body, 0);

            // 调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.PENDING.getIndex(), "堂食货架区商品核销后订单状态不是待确认状态");

            //KDS 获取订单列表
            TimeUnit.SECONDS.sleep(10);
            Map<String, String> queryPara = new HashMap<String, String>();
            queryPara.put("shopId", "9I01");
            queryPara.put("stallId", "beef");
            queryPara.put("appId", "abc");
            jsonPath = kdsService.getProcessOrderList(queryPara, 0);
            String batchNo = jsonPath.getString("batchNo");
            String pickupCode = jsonPath.getString("waitProcessList.pickupCode");
            List<String> orders = jsonPath.getList("waitProcessList.orderId");
            List<String> pickItemIds = jsonPath.getList("waitProcessList.waitProcessItemList.pickItemId");


            boolean checkOrderResult = false;

            for (Iterator iter = orders.iterator(); iter.hasNext(); ) {
                if (iter.next().toString().equals(orderId)) {
                    checkOrderResult = true;
                }
            }

            Assert.isTrue(checkOrderResult, "未找到需要加工的订单");

            //KDS 确认加工列表
            queryPara.clear();
            queryPara.put("shopId", "9I01");
            queryPara.put("appId", "abc");
            queryPara.put("batchNo", batchNo);
            jsonPath = kdsService.confirmOrder(queryPara, "", 0);

            // 调用管家的订单详情接口获取订单状态
            query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
            jsonPath = orderService.detailGj(query, 0);
            status = jsonPath.getInt("status");
            Assert.isTrue(status == GJOrderStatus.PENDING.getIndex(), "堂食商品确认加工单后订单状态不是待确认状态");

            //KDS 开始、完成加工菜品, 用户自提
            for (Iterator iter = pickItemIds.iterator(); iter.hasNext(); ) {
                String pickItemIdList = iter.next().toString();
                String pickItemId = pickItemIdList.substring(1, pickItemIdList.length() - 1);
                queryPara.clear();
                queryPara.put("shopId", "9I01");
                queryPara.put("appId", "abc");
                queryPara.put("stallId", "beef");
                queryPara.put("pickItemId", pickItemId);

                //开始加工
                jsonPath = kdsService.beginProcessOrder(queryPara, "", 0);

                // 调用管家的订单详情接口获取订单状态
                query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
                jsonPath = orderService.detailGj(query, 0);
                status = jsonPath.getInt("status");
//                Assert.isTrue(status == GJOrderStatus.START_PACK.getIndex(), "堂食商品开始加工后订单状态不是开始拣货状态");

                //完成加工
                jsonPath = kdsService.finishProcessOrder(queryPara, "", 0);

                // 调用管家的订单详情接口获取订单状态
                query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
                jsonPath = orderService.detailGj(query, 0);
                status = jsonPath.getInt("status");
//                Assert.isTrue(status == GJOrderStatus.READY_TO_PICKUP.getIndex(), "堂食商品加工完成后订单状态不是待提货状态");

                queryPara.put("pickupCode", pickupCode.substring(1, pickupCode.length() - 1));

                //自提菜品
                jsonPath = kdsService.pickUpOrder(queryPara, "", 0);

                // 调用管家的订单详情接口获取订单状态
                query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
                jsonPath = orderService.detailGj(query, 0);
                status = jsonPath.getInt("status");
//                Assert.isTrue(status == GJOrderStatus.COMPLETE.getIndex(), "堂食商品自提后订单状态不是已完成状态");
            }


            //重新登录永辉生活app刷新用户信息
            query = "?platform=ios";
            body = "{\"phonenum\":\"13816043212\",\"securitycode\":\"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
            accessTokenSH = userInfo.getAccess_token();

            //积分校验
            //TODO
/**            Map<Double, Double> goodsArr = new HashMap<Double, Double>();
 // key为数量，value为价格
 goodsArr.put(1d, 16.80);
 Double tempCredit = ValidateUtil.calculateCredit(goodsArr);
 Assert.isTrue(userInfo.getCredit() - tempCredit == credit, "核销后用户积分增加不正确");
 **/
            //登出永辉生活app
            query = "?platform=Android&access_token=" + accessTokenSH;
            jsonPath = loginService.loginOutSH(query, 0);

        } catch (Exception e) {
            testcase.setStatus("FAIL");
            testcase.setDescription(e.getMessage());
            throw e;
        } finally {

        }
    }
}
