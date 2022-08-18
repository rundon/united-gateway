package com.onefly.gateway.constant;

import com.onefly.gateway.utils.crypto.AesUtils;
import com.onefly.gateway.utils.crypto.RSAUtil;

/**
 * @author Sean createAt 2021/6/24
 */
public class Common {

    /**
     * AES 加密算法、模式、补位方式
     */
    public static final String AES_TRANSFORMATION = AesUtils.AES_CBC_PKCS5Padding;

    /**
     * AES 加密密钥长度
     */
    public static final int AES_KEY_LENGTH = 128;

    /**
     * RSA 签名算法
     */
    public static final String RSA_SIGN_ALGORITHM = RSAUtil.SIGN_ALGORITHMS_SHA512;

    /**
     * 时间戳格式
     */
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 日期格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 时间格式
     */
    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * 日期时间格式
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 签名算法
     */
    public static final String SIGN_ALGORITHMS = "SHA256withRSA";
}
