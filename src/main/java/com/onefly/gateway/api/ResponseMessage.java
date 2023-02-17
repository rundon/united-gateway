package com.onefly.gateway.api;

import com.onefly.gateway.constant.SignType;
import com.onefly.gateway.utils.MapBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;


/**
 * 响应报文
 *
 * @author Sean createAt 2021/6/22
 */
@Getter
@Setter
@ToString
public class ResponseMessage implements java.io.Serializable {

    private static final long serialVersionUID = 5435501706637912303L;

    /**
     * 响应请求的时间
     */
    private String timestamp;

    /**
     * 随机字符串
     */
    private String randomString;

    /**
     * 请求是否处理成功
     */
    private Boolean success;

    /**
     * 响应状态码
     */
    private String status;

    /**
     * 响应数据
     * 根据接口情况判断是否存在该值
     */
    private String payload;

    /**
     * 请求参数编码格式
     */
    private String charset;

    /**
     * 响应结果说明
     */
    private String describe;

    /**
     * 签名方式
     */
    private SignType signType;

    /**
     * 签名原始数据=发送请求时间+随机字符串+响应状态码+报文数据
     * 报文签名、Base64编码
     * 请求时、客户端使用自身秘钥计算签名
     * 响应时、服务端使用自身秘钥计算签名
     */
    private String sign;

    public Map<String, Object> toMap() {
        return MapBuilder.<String, Object>builder()
                .put("timestamp", timestamp)
                .put("randomString", randomString)
                .put("success", success)
                .put("status", status)
                .put("payload", payload)
                .put("describe", describe)
                .put("signType", signType)
                .put("sign", sign)
                .build();
    }

    public Charset charset() {
        if (null == charset) {
            return StandardCharsets.UTF_8;
        }
        return Charset.forName(charset);
    }
}
