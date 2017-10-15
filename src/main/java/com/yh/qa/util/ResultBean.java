package com.yh.qa.util;

import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matcher;

/**
 * @author 许崇英
 * @since 2017/8/31
 */
public class ResultBean{
	private ResultStatus resultStatus ;
    private String data;
    private ValidatableResponse validatableResponse;

    public ResultStatus getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(ResultStatus resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "ResultBean{" +
                "resultStatus=" + resultStatus +
                ", data=" + data +
                '}';
    }

    public ValidatableResponse getValidatableResponse() {
        return validatableResponse;
    }

    public void setValidatableResponse(ValidatableResponse validatableResponse) {
        this.validatableResponse = validatableResponse;
    }

    public ValidatableResponse validate(String path, Matcher<?> matcher, Object... additionalKeyMatcherPairs){
        return this.validatableResponse
                .assertThat()
                .body(path, matcher, additionalKeyMatcherPairs);
    }

    public String getValue(String path){
        return  this.validatableResponse.extract().path(path);
    }
}
