package com.yh.qa.service.impl;

import com.yh.qa.dao.OrderDao;
import com.yh.qa.service.OrderService;
import com.yh.qa.util.ResultBean;
import com.yh.qa.util.Schema;
import com.yh.qa.util.ValidateUtil;

import io.restassured.path.json.JsonPath;

import com.yh.qa.util.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import static com.yh.qa.util.ValidateUtil.validateSuccess;

@Service("OrderConfirmService")
public class OrderServiceImpl extends HttpServiceImpl implements OrderService {
	private static Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
	@Autowired
	private OrderDao orderDao;

	@Value("${domain-shenghuo}")
	private String domainShenghuo;

	@Value("${domain-guanjia}")
	private String domainGuanJia;

	@Value("${domain-adapter}")
	private String domainAdapter;

	@Override
	public JsonPath confirm(String query, String body, int code) throws Exception {
		ResultBean result = post(domainShenghuo + Path.SH_ORDER_CONFIRM + query, body, Schema.SH_ORDER_CONFIRM_ACHEMA);
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath orderAction(String query, String body, int code) throws Exception {
		ResultBean result = post(domainGuanJia + Path.ORDERACTION + query, body, Schema.GJ_ORDERACTION_ACHEMA);
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath signPackage(String query, String body, int code) throws Exception {
		ResultBean result = post(domainGuanJia + Path.SIGNPACKAGE+query, body, Schema.GJ_SIGNPACKAGE_ACHEMA);
		return ValidateUtil.validateCode(result, code);
	}

    @Override
    public JsonPath packageAction(String query, String body, int code) throws Exception {
        ResultBean result = post(domainGuanJia + Path.PACKAGEACTION+query, body, Schema.GJ_PACKAGEACTION_ACHEMA);
        return ValidateUtil.validateCode(result, code);
    }

	@Override
	public JsonPath detail(String query, int code) throws Exception {
		ResultBean result = get(domainShenghuo + Path.SH_ORDER_DETAIL + query, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath orderList(String query, int code) throws Exception {
		ResultBean result = get(domainShenghuo + Path.SH_ORDER_LIST + query, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath outstock(String body, Boolean flag) throws Exception {
		ResultBean result = post(domainAdapter + Path.OUTSTACK, body, "");
		return validateSuccess(result, "success", flag);
	}

	@Override
	public JsonPath pack(String body, Boolean flag) throws Exception {
		ResultBean result = post(domainShenghuo + Path.Packing, body, "");
		return validateSuccess(result, "dos[0].success", flag);
	}

	public JsonPath detailGj(String query, int code) throws Exception {
		ResultBean result = get(domainGuanJia + Path.GJ_ORDER_DETAIL + query, "");
		return ValidateUtil.validateCode(result, code);
	}

    public JsonPath packageDetailGj(String query, int code) throws Exception {
        ResultBean result = get(domainGuanJia + Path.PACKAGEDETAIL + query, "");
        return ValidateUtil.validateCode(result, code);
    }

	@Override
	public JsonPath waitingPack(String query, int code) throws Exception {
		ResultBean result = get(domainGuanJia + Path.WAITINGPACK+query, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath startPack(String query, String body, int code) throws Exception {
		ResultBean result = post(domainGuanJia + Path.STSRTPACK + query, body, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath completePack(String query, String body, int code) throws Exception {
		ResultBean result = post(domainGuanJia + Path.COMPLETEPACK + query, body, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath batchAction(String query, String body, int code) throws Exception {
		ResultBean result = post(domainGuanJia + Path.BATCHACTION + query, body, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath registerLackSku(String uri, String body, int code) throws Exception{
		ResultBean result =  post(domainGuanJia + Path.OUTOFSTOCK + uri, body, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath mergeGroup(String query, String body, int code) throws Exception {
		ResultBean result = post(domainGuanJia + Path.MERGEGROUP + query, body, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath mergingScan(String query, String body, int code) throws Exception {
		ResultBean result = post(domainGuanJia + Path.MERGINGSCAN + query, body, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath mergingBind(String query, String body, int code) throws Exception {
		ResultBean result = post(domainGuanJia + Path.MERGINGBIND + query, body, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath mergingInbox(String query, String body, int code) throws Exception {
		ResultBean result = post(domainGuanJia + Path.MERGINGINBOX + query, body, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath action(String query, String body, int code) throws Exception {
		ResultBean result = post(domainGuanJia + Path.ACTION + query, body, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath applyRefund(String query, String body, int code) throws Exception {
		ResultBean result = post(domainShenghuo + Path.APPLYREFUND + query, body, "");
		return ValidateUtil.validateCode(result, code);
	}

	@Override
	public JsonPath partialReturn(String query, String body, int code) throws Exception {
		ResultBean result = post(domainGuanJia + Path.PARTIALRETURN + query, body, "");
		return ValidateUtil.validateCode(result, code);
    }

	/**
	 *
	 * @param orderId 订单号
	 * @param n 分钟数
	 * @return
	 * @throws Exception
	 */
	@Override
	public String getDelayedWaveIdByOrderId(String orderId, int n) throws Exception{
		String waveId = null;
		for(int i=0;i<=n*6;i++){
			try{
				waveId = orderDao.getWaveIdByOrderId(orderId);
				return waveId;
			}catch(Exception e){
				logger.info("未查询到{}订单的波次号，请等待10秒！",orderId);
				Thread.sleep(10000);  //10秒查询一次
			}
		}
		return waveId;
	}

	// 调用管家的订单详情接口获取订单状态
    @Override
	public void validateOrderStatusGJ(String orderId, String accessTokenGJ, String uid, int code, int index, String message) throws Exception {
		String query = "?platform=Android&orderid=" + orderId + "&access_token=" + accessTokenGJ + "&id=" + uid;
		JsonPath jsonPath = detailGj(query, code);
		int status = jsonPath.getInt("status");
		Assert.isTrue(status == index, message);
	}

    @Override
    public void validatePackageStatusGJ(String packageCode, String accessTokenGJ, int code, int index, String message) throws Exception {
        String query = "?code="+packageCode+"&access_token="+accessTokenGJ+"&role=26&platform=ios&timestamp=1507962510364&channel=anything&v=2.4.10.0";
        JsonPath jsonPath = packageDetailGj(query, code);
        int status = jsonPath.getInt("status");
        Assert.isTrue(status == index, message);
    }

}
