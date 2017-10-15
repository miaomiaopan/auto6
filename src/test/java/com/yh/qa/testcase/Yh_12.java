package com.yh.qa.testcase;

import com.yh.qa.basecase.BaseTestCase;
import com.yh.qa.entity.GJOrderStatus;
import com.yh.qa.entity.OrderDetail;
import com.yh.qa.entity.UserInfo;
import com.yh.qa.service.LoginService;
import com.yh.qa.service.OrderService;
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
 * @author luqiankun
 *
 * @date 2017年10月12日
 */
@SpringBootTest
public class Yh_12 extends BaseTestCase {
    @Autowired
    private LoginService loginService;

    @Autowired
    private OrderService orderService;

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
        ArrayList GJOrderList = new ArrayList();
        ArrayList shopList = new ArrayList();
        shopList.add("9230");
        shopList.add("9218");
        HashMap<String, String> develiveryInShop = new HashMap<>();
        develiveryInShop.put("9230","50012059");
        develiveryInShop.put("9218","50012070");
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
            testcase.setTestName("北京极速达下单（拆单）和履单流程");

            // case开始执行
            // 登录永辉生活app
            //step 1
            query = "?platform=ios";
            body = "{\"phonenum\": \"17316312358\", \"securitycode\": \"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
            accessTokenSH = userInfo.getAccess_token();
            balance = userInfo.getBalance();
            credit = userInfo.getCredit();
            System.out.println("前面的" + credit);
            num = userInfo.getNum();
            toComment = userInfo.getToComment();
            toDelivery = userInfo.getToDelivery();
            uid = userInfo.getUId();

            //step2
            // 生成会员店当日达配送订单
            query = "?channel=qa3&deviceid=000000000000000&platform=Android&v=4.2.2.1&access_token="
                    + accessTokenSH+"&timestamp="+System.currentTimeMillis();
            long date = System.currentTimeMillis()/86400000*86400000-28800000;
            body = "{\"pointpayoption\": 0,\"autocoupon\": 0,\"uid\": \"" + uid + "\",\"texpecttime\": {\"date\": " + date + ",\"timeslots\": [{\"to\": \"\",\"slottype\": \"immediate\",\"form\": \"\" }] },\"freedeliveryoption\": 1,\"device_info\": \"8E9157E0-6BFB-49EC-B255-1D32E79EDD9A\",\"sellerid\": \"11\",\"comment\": \"\",\"recvinfo\": {\"isdefault\": 1,\"phone\": \"17316312358\",\"alias\": \"家\",\"id\": \"32345\",\"location\": {\"lat\": \"40.053865\",\"lng\": \"116.353402\" },\"address\": {\"detail\": \"3号楼501室\",\"area\": \"云静磊快捷酒店\",\"city\": \"北京\",\"cityid\": 2 },\"scope\": 0,\"name\": \"卢\" },\"products\": [{\"id\": \"X-276376\",\"num\": 100 }, {\"id\": \"X-1\",\"num\": 100 }],\"pickself\": 0,\"packingbagoption\": \"0\",\"totalpayment\": 0,\"balancepayoption\": 1,\"storeid\": \"BJ005\" }";
            jsonPath = orderService.confirm(query, body, 0);
            orderId = jsonPath.getString("orderid");

            //step3
            // 重新登录永辉生活APP刷新用户信息
            query = "?platform=ios";
            body = "{\"phonenum\": \"17316312358\", \"securitycode\": \"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
            accessTokenSH = userInfo.getAccess_token();

            Assert.isTrue(userInfo.getBalance() + 2298 == balance, "下单支付后用户余额减少数额错误");
            Assert.isTrue(userInfo.getNum() - 2 == num, "下单支付后订单总数没有加2");
            Assert.isTrue(userInfo.getToDelivery() - 2 == toDelivery, "下单后待配送订单总数没有加2");

            //获取拆单后订单号
            query = "?filter=4&uid=" +uid+ "&deviceid=000000000000000&distinctId=8E9157E0-6BFB-49EC-B255-1D32E79EDD9A&platform=ios&channel=2&v=4.2.3.12&page=0&access_token="
                    + accessTokenSH+"&timestamp="+System.currentTimeMillis();
            jsonPath = orderService.orderList(query,0);

            orderId = jsonPath.getString("orders[0].id");
            GJOrderList.add(orderId);
            orderId = jsonPath.getString("orders[1].id");
            GJOrderList.add(orderId);

            for (int i = 0; i < GJOrderList.size(); i++ ){
                for(int j=0; j< shopList.size(); j++){
                    // 使用拣货员角色的账号登录管家APP
                    query = "?platform=android";
                    body = "{\"pwd\": \"123456a\", \"username\": \"" + shopList.get(j).toString() + "-1\"}";
                    jsonPath = loginService.loginGJ(query, body, 0);
                    accessTokenGJ = jsonPath.getString("token");

                    // 调用管家的订单详情接口获取订单状态
                    query = "?platform=Android&orderid=" + GJOrderList.get(i).toString() + "&access_token=" + accessTokenGJ + "&id=" + uid;
                    jsonPath = orderService.detailGj(query, 0);
                    String shopGetFromGJ = jsonPath.getString("shop.shopid");
                    if(shopGetFromGJ.equals(shopList.get(j).toString())){
                        GJHandleOrderNormal(GJOrderList.get(i).toString(), uid,shopList.get(j).toString(), develiveryInShop.get(shopList.get(j).toString()));
                        break;
                    }
                }
            }


            //step6
            // 重新登录永辉生活APP刷新用户信息
            query = "?platform=ios";
            body = "{\"phonenum\": \"17316312358\", \"securitycode\": \"601933\"}";
            userInfo = loginService.loginSHAndGetUserInfo(query, body, 0);
            accessTokenSH = userInfo.getAccess_token();

            Assert.isTrue(userInfo.getToComment() - 2 == toComment, "核销后待评价订单总数没有加2");

            // 积分校验
            List<OrderDetail> goodsArr = new ArrayList<OrderDetail>();
            goodsArr.add(new OrderDetail(1d, 11.98));
            goodsArr.add(new OrderDetail(1d, 11.00));
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

    private void GJHandleOrderNormal(String orderId, String uid, String GJUser, String develivery) throws Exception {
        String query;
        String body;
        JsonPath jsonPath;
        String accessTokenGJ;
        int status;//step4
        // 使用拣货员角色的账号登录管家APP
        query = "?platform=android";
        body = "{\"pwd\": \"123456a\", \"username\": \"" + GJUser + "-1\"}";
        jsonPath = loginService.loginGJ(query, body, 0);
        accessTokenGJ = jsonPath.getString("token");

        // 调用管家的订单详情接口获取订单状态
        query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
        jsonPath = orderService.detailGj(query, 0);
        status = jsonPath.getInt("status");

        Assert.isTrue(status == GJOrderStatus.PENDING.getIndex(), "生活APP下当日达非合伙人配送单余额支付后管家中查询订单状态不是待确认状态");

        // 开始拣货
        query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();
        ;
        body = "{\"orderid\": \"" + orderId + "\", \"action\": \"7\"}";
        jsonPath = orderService.orderAction(query, body, 0);

        // 调用管家的订单详情接口获取订单状态
        query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
        jsonPath = orderService.detailGj(query, 0);
        status = jsonPath.getInt("status");

        Assert.isTrue(status == GJOrderStatus.START_PACK.getIndex(), "管家中开始拣货后订单状态不是开始拣货状态");

        // 拣货完成
        query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();
        ;
        body = "{\"orderid\": \"" + orderId + "\", \"action\": \"8\"}";

        jsonPath = orderService.orderAction(query, body, 0);

        // 调用管家的订单详情接口获取订单状态
        TimeUnit.SECONDS.sleep(8);
        query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
        jsonPath = orderService.detailGj(query, 0);
        status = jsonPath.getInt("status");

        Assert.isTrue(status == GJOrderStatus.WAITING_ASSIGN.getIndex(), "管家中拣货完成后订单状态不是等待派送状态");

        // 分配派送员
        query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();
        ;
        body = "{\"orderid\": \"" + orderId + "\",\"assignto\": \"" + develivery + "\", \"action\": \"5\"}";
        jsonPath = orderService.orderAction(query, body, 0);

        //step5
        // 配送员角色的账号登录管家APP
        query = "?platform=android";
        body = "{\"pwd\": \"123456a\", \"username\": \"" + GJUser + "-2\"}";
        jsonPath = loginService.loginGJ(query, body, 0);
        accessTokenGJ = jsonPath.getString("token");

        // 调用管家的订单详情接口获取订单状态
        query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
        jsonPath = orderService.detailGj(query, 0);
        status = jsonPath.getInt("status");

        Assert.isTrue(status == GJOrderStatus.WAITING_TAKE.getIndex(), "管家中分配派送后订单状态不是待派状态");

        // 派送员接单
        query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();
        ;
        body = "{\"orderid\": \"" + orderId + "\", \"action\": \"1\"}";
        jsonPath = orderService.orderAction(query, body, 0);

        // 调用管家的订单详情接口获取订单状态
        query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
        jsonPath = orderService.detailGj(query, 0);
        status = jsonPath.getInt("status");

        Assert.isTrue(status == GJOrderStatus.READY_TO_PICKUP.getIndex(), "管家中分配派送员完成后订单状态不是待提状态");

        // 派送员提单
        query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();
        ;
        body = "{\"orderid\": \"" + orderId + "\", \"action\": \"2\"}";
        jsonPath = orderService.orderAction(query, body, 0);

        // 调用管家的订单详情接口获取订单状态
        query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
        jsonPath = orderService.detailGj(query, 0);
        status = jsonPath.getInt("status");

        Assert.isTrue(status == GJOrderStatus.PICKUP.getIndex(), "管家派送员提单完成后订单状态不是待核销状态");

        // 核销
        query = "?platform=ios&access_token=" + accessTokenGJ+"&timestamp="+System.currentTimeMillis();
        ;
        body = "{\"orderid\": \"" + orderId + "\", \"action\": \"3\"}";
        jsonPath = orderService.orderAction(query, body, 0);

        // 调用管家的订单详情接口获取订单状态
        query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
        jsonPath = orderService.detailGj(query, 0);
        status = jsonPath.getInt("status");

        Assert.isTrue(status == GJOrderStatus.COMPLETE.getIndex(), "管家中核销完成后订单状态不是已核销状态");
    }
}
