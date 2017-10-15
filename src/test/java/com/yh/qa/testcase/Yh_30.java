package com.yh.qa.testcase;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.entity.OrderDetail;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.KDSService;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
import com.yh.qa.util.ValidateUtil;
import io.restassured.path.json.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Yh_30 extends BaseTestCase {
    @Autowired
    private LoginService loginService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private KDSService kdsService;

    @Test
    public void testHttpPost() throws Exception {
        String query = "";
        String body = "";
        JsonPath jsonPath = null;
        String orderId = "";
        String accessTokenSH = "";
        String accessTokenGJ = "";
        String uid = "";
        UserInfo userInfo = null;
        // 余额
        int balance;
        // 积分
        Double credit;
        // 订单总数
        int num;
        // 待评价订单数量
        int toComment;
        // 待配送订单数量
        int toPickup;
        // 订单状态
        int status;

        try {
            // 设置case名称
            testcase.setTestName("北京极速达下单（不拆单）和履单流程");

            // case开始执行
            // 登录永辉生活app
            query = "?platform=ios";
            body = "{\"phonenum\": \"17316312358\", \"securitycode\": \"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
            accessTokenSH = userInfo.getAccess_token();
            balance = userInfo.getBalance();
            credit = userInfo.getCredit();
            System.out.println("前面的" + credit);
            num = userInfo.getNum();
            toComment = userInfo.getToComment();
            toPickup = userInfo.getToPickup();
            uid = userInfo.getUId();

            // 生成会员店当日达配送订单
            query = "?channel=qa3&deviceid=000000000000000&platform=Android&v=4.2.2.1&access_token="
                    + accessTokenSH+"&timestamp="+System.currentTimeMillis();
            long date = System.currentTimeMillis()/86400000*86400000-28800000;
            body = "{\"pointpayoption\": 0,\"autocoupon\": 1,\"texpecttime\": {\"date\": " + date +
                    ",\"timeslots\": [{\"from\": \"\",\"to\": \"\",\"immediatedescription\": \"立即堂食\",\"slottype\": \"immediate\" }] },\"freedeliveryoption\": 1,\"sellerid\": 6,\"type\": \"food\",\"recvinfo\": {\"phone\": \"17316312358\",\"scope\": 0,\"isdefault\": 1,\"name\": \"卢先生\",\"location\": {\"lat\": \"22.551823\",\"lng\": \"114.092705\" },\"address\": {\"detail\": \"\",\"area\": \"华强广场-C座\",\"city\": \"深圳\",\"cityid\": \"13\" } },\"selectedcoupons\": [],\"products\": [{\"num\": 100,\"id\": \"7061;7062;\" }],\"pickself\": 1,\"dinnersnumber\": 1,\"totalpayment\": 0,\"balancepayoption\": 1,\"storeid\": \"9I07\" }";
            jsonPath = orderService.confirm(query, body, 0);
            orderId = jsonPath.getString("orderid");

            // 重新登录永辉生活APP刷新用户信息
            query = "?platform=ios";
            body = "{\"phonenum\": \"17316312358\", \"securitycode\": \"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
            accessTokenSH = userInfo.getAccess_token();

            Assert.isTrue(userInfo.getBalance() + 8722 == balance, "下单支付后用户余额减少数额错误");
            Assert.isTrue(userInfo.getNum() - 1 == num, "下单支付后订单总数没有加1");
            Assert.isTrue(userInfo.getToPickup() - 1 == toPickup, "下单后待配送订单总数没有加1");

            //KDS 获取订单列表
            Map<String, String> queryPara = new HashMap<String, String>();
            queryPara.put("shopId","9I07");
            queryPara.put("stallId","beef");
            queryPara.put("appId","abc");
            jsonPath = kdsService.getProcessOrderList(queryPara,0);
            String batchNo = jsonPath.getString("batchNo");
            String pickupCode = jsonPath.getString("waitProcessList.pickupCode");
            List<String> orders = jsonPath.getList("waitProcessList.orderId");
            List<String> pickItemIds = jsonPath.getList("waitProcessList.waitProcessItemList.pickItemId");
            Assert.isTrue(orders.contains(orderId),"未找到需要加工的订单");

            //KDS 确认加工列表
            queryPara.clear();
            queryPara.put("shopId","9I07");
            queryPara.put("appId","abc");
            queryPara.put("batchNo",batchNo);
            jsonPath = kdsService.confirmOrder(queryPara,"",0);

            //KDS 开始、完成加工菜品, 用户自提
            for(String pickItemId:pickItemIds){
                queryPara.clear();
                queryPara.put("shopId","9I07");
                queryPara.put("appId","abc");
                queryPara.put("stallId","beef");
                queryPara.put("pickItemId",pickItemId);
                jsonPath = kdsService.beginProcessOrder(queryPara,"",0);
                jsonPath = kdsService.finishProcessOrder(queryPara,"",0);
                queryPara.put("pickupCode",pickupCode);
                jsonPath = kdsService.pickUpOrder(queryPara,"",0);
            }



            // 重新登录永辉生活APP刷新用户信息
            query = "?platform=ios";
            body = "{\"phonenum\": \"17316312358\", \"securitycode\": \"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
            accessTokenSH = userInfo.getAccess_token();

            Assert.isTrue(userInfo.getToComment() - 1 == toComment, "核销后待评价订单总数没有加1");

            // 积分校验
            List<OrderDetail> goodsArr = new ArrayList<OrderDetail>();
            goodsArr.add(new OrderDetail(1d, 87.22d));
            Double tempCredit = ValidateUtil.calculateCredit2(goodsArr);
            System.out.println(tempCredit + "**" + credit);
            Assert.isTrue(userInfo.getCredit() - tempCredit == credit, "核销后用户积分增加不正确");

            // 登出永辉生活app
            query = "?platform=Android&access_token=" + accessTokenSH;
            jsonPath = loginService.loginOutSH(query, 0);

        } catch (Exception e) {
            testcase.setStatus("FAIL");
            testcase.setDescription(e.getMessage());
            throw e;
        } finally {
            // TODO 刪除测试数据
        }

    }
}
