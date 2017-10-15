package com.yh.qa.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.yh.qa.entity.OrderDetail;

import io.restassured.path.json.JsonPath;

/**
 * @author panmiaomiao
 *
 * @date 2017年10月2日
 */
public class ValidateUtil {
	// 判断接口返回的json中code是否为0，不为0,则抛出异常。返回以data为root的JsonPath
	public static JsonPath validateCode(ResultBean result, int code) throws Exception {
		JsonPath jsonPath = JsonPath.from(result.getData());
		if (jsonPath.getInt("code") != code) {
			throw new Exception(jsonPath.getString("message"));
		}

		return jsonPath.setRoot("data");
	}

	public static JsonPath validateSuccess(ResultBean result, String path, Boolean flag) throws Exception{
		JsonPath jsonPath = JsonPath.from(result.getData());
		if (jsonPath.getBoolean(path) != flag) {
			throw new Exception("失败：返回的success为"+jsonPath.getBoolean(path));
		}

		return jsonPath;
	}

	//在不使用任何优惠的情况下计算积分(适用于只有一条明细)
	public static Double calculateCredit(Map<Double, Double> goodsArr) {
		BigDecimal credit = new BigDecimal(0);
		for (Double key : goodsArr.keySet()) {
			BigDecimal quantity = BigDecimal.valueOf(key).setScale(2 ,BigDecimal.ROUND_HALF_UP);
			BigDecimal price = BigDecimal.valueOf(goodsArr.get(key)).setScale(2 ,BigDecimal.ROUND_HALF_UP);
			credit = credit.add(((quantity.multiply(price)).setScale(2, BigDecimal.ROUND_HALF_UP)).divide(quantity).setScale(2,BigDecimal.ROUND_HALF_UP).multiply(quantity).setScale(2, BigDecimal.ROUND_HALF_UP));
		}
		
		return credit.doubleValue();
	}
	
	//在不使用任何优惠的情况下计算积分(适用于只有多条明细)
	public static Double calculateCredit2(List<OrderDetail> details) {
		BigDecimal credit = new BigDecimal(0);
		for(OrderDetail detail : details){
			BigDecimal quantity = BigDecimal.valueOf(detail.getQuantity()).setScale(2 ,BigDecimal.ROUND_HALF_UP);
			BigDecimal price = BigDecimal.valueOf(detail.getPrice()).setScale(2 ,BigDecimal.ROUND_HALF_UP);
			credit = credit.add(((quantity.multiply(price)).setScale(2, BigDecimal.ROUND_HALF_UP)).divide(quantity).setScale(2,BigDecimal.ROUND_HALF_UP).multiply(quantity).setScale(2, BigDecimal.ROUND_HALF_UP));
		}
		
		return credit.doubleValue();
	}
	
}
