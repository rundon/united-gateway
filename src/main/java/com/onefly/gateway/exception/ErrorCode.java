package com.onefly.gateway.exception;

/**
 * @author Sean Create At 2019-11-19
 */
public enum ErrorCode {

    SUCCESS("0", "request handle success."),
    DEFECT_SUCCESS("0001", "有缺陷的成功"),
    EXCEPTION("3000", "an unknown error occurred"),

    // 从 3001 - 3199 开始为参数错误类代码 开始
    PARAMS_ERROR("3001", "invalid parameter"),
    CHECK_VALUE_ERROR("3002", "check value error"),
    VERSION_ERROR("3003", "版本错误"),
    EXPIRED_ERROR("3004", "过期"),

    MIN_VALUE("3008", "输入的值小于最低值"),
    MAX_VALUE("3009", "输入的值大于最大值"),

    SIGNATURE_INVALID("3011", "无效的签名"),
    // 从 3001 - 3199 开始为参数错误类代码 结束


    // 从 3200 - 3250 为系统类错误代码开始
    CUSTOM_ERROR_MESSAGE("3200", "CUSTOM_ERROR_MESSAGE"),
    REQUEST_CONTENT_TYPE_NOT_SUPPORTED("3201", "request content type not supported"),
    REQUEST_METHOD_NOT_SUPPORTED("3202", "request method not supported"),
    SERVICE_NOT_IMPL("3203", "服务未实现"),

    UNAUTHORIZED("3210", "unauthorized"),
    INVALID_CREDENTIAL("3211", "invalid credential"),

    ACCESS_DENIED("3250", "No access permission"),
    // 从 3200 - 3250 为系统类错误代码结束


    // 从 3301 - 3400 为资源相关错误代码开始
    RESOURCE_EXIST("3301", "resource exist"),
    RESOURCE_NON_EXIST("3302", "resource non exist"),
    RESOURCE_STATUS("3303", "resource status exception"),
    // 从 3301 - 3400 为资源相关错误代码结束
    ;


    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }


    @Override
    public String toString() {
        return "[" + this.code + "]" + this.message;
    }


}
