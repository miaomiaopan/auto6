package com.yh.qa.service.impl;

import com.yh.qa.service.HttpService;
import com.yh.qa.util.ResultBean;
import com.yh.qa.util.ResultStatus;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * @author 许崇英
 * @since 2017/9/1
 */
@Service("httpService")
public class HttpServiceImpl implements HttpService {

	private static final String HTTPS = "https://";

	private static Logger logger = LoggerFactory.getLogger(HttpServiceImpl.class);

	@Override
	public ResultBean post(String url, String paramBody, String verifyJsonResultSchema) throws Exception {
		logger.info("POST请求开始：URL:{}", url);

		ResultBean resultBean = new ResultBean();

		Response response;

		ValidatableResponse validatableResponse = null;

		response = getRequestSpecification(url).headers(getHeaders()).body(paramBody).post(url);

		validatableResponse = response.then().statusCode(200);

		if (!StringUtils.isEmpty(verifyJsonResultSchema)) {
			try {
				// 验证json格式
				response = validatableResponse
						.body(JsonSchemaValidator.matchesJsonSchemaInClasspath(verifyJsonResultSchema)).extract()
						.response();
			} catch (Throwable a) {
				System.out.println(a.getLocalizedMessage());
				throw new Exception("SCHEMA校验出错");
			}
		}

		resultBean.setResultStatus(ResultStatus.SCHEMA_SUCCESS);
		resultBean.setData(response.asString());
		resultBean.setValidatableResponse(validatableResponse);
		logger.info("响应结果：{}", resultBean);
		return resultBean;
	}

	@Override
	public ResultBean get(String url, String verifyJsonResultSchema) throws Exception {

		logger.info("GET请求开始：URL:{}", url);

		ResultBean resultBean = new ResultBean();

		ValidatableResponse validatableResponse = null;

		Response response;

		response = getRequestSpecification(url).headers(getHeaders()).get(url);

		validatableResponse = response.then().statusCode(200);

		if (!StringUtils.isEmpty(verifyJsonResultSchema)) {
			try {
				// 验证json格式
				response = validatableResponse
						.body(JsonSchemaValidator.matchesJsonSchemaInClasspath(verifyJsonResultSchema)).extract()
						.response();
			} catch (Throwable a) {
				System.out.println(a.getLocalizedMessage());
				throw new Exception("SHEMA校验出错");
			}
		}

		resultBean.setResultStatus(ResultStatus.SCHEMA_SUCCESS);
		resultBean.setData(response.asString());
		resultBean.setValidatableResponse(validatableResponse);
		logger.info("响应结果：{}", resultBean);
		return resultBean;
	}

	private RequestSpecification getRequestSpecification(String url) {
		if (!StringUtils.isEmpty(url) && url.toLowerCase().contains(HTTPS)) {
			return given().relaxedHTTPSValidation();
		}
		return given();
	}

	private Map<String, String> getHeaders() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
	    headers.put("Accept-Charset", "utf-8");
		headers.put("Accept-Encoding", "gzip, deflate");
		//headers.put("Connection", "keep-alive");
		headers.put("User-Agent", "YhStore/4.2.0(client/phone; iOS 10.3.3; iPhone8,1)");

		return headers;
	}

}
