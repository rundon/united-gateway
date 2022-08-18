package com.onefly.gateway.constant;

/**
 * @author 田尘殇Sean(sean.snow @ live.com) createAt 2018/6/28
 */
public class HttpHeader {

    /**
     * 签名版本
     */
    public static final String MESSAGE_VERSION = "X-Message-Version";
    /**
     * 目标地址
     */
    public static final String TARGET_HEADER_KEY = "X-Target-Uri";

    /**
     * 签名
     */
    public static final String SIGNATURE = "X-API-Signature";
    /**
     * 加密方法
     */
    public static final String METHOD = "X-API-Signature-Method";
    /**
     * 用户端ID
     */
    public static final String CLIENT_ID = "X-Client-Id";
    /**
     * API 版本
     */
    public static final String VERSION = "X-API-Version";
    /**
     * 时间戳
     */
    public static final String TIMESTAMP = "X-API-Timestamp";//
    /**
     * 随机字符串
     */
    public static final String NONCE = "X-API-Nonce";//X-API-Nonce

    public static final String TRK = "X-API-TRK";
    /**
     * 需要签名的参数 以逗号隔开  params + timestamp + nonce
     */
    public static final String PARAMS = "X-API-Signature-Params";
    /**
     * 签名方法，默认HmacSHA256
     */
    public static final String method = "X-API-Signature-Method";
}


