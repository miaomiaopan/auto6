package com.yh.qa.util;

/**
 * @author 许崇英
 * @since 2017/8/31
 */
public enum ResultStatus {
    FAIL(-1, "异常失败"),
    SCHEMA_FAIL(0, "schema验证失败"),
    FIELD_FAIL(1, "schema验证失败"),
    SCHEMA_SUCCESS(2, "schema验证通过"),
    FIELD_SUCCESS(3, "字段验证通过"),
    NOT_EXECUTE(4, "未执行");

    ResultStatus(int code, String status) {
        this.code = code;
        this.status = status;
    }

    private int code;
    private String status;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
