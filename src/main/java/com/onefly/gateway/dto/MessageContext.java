package com.onefly.gateway.dto;

import com.onefly.gateway.api.RequestMessage;
import com.onefly.gateway.entity.Client;
import lombok.Getter;
import lombok.Setter;

/**
 * 请求报文上下文
 *
 * @author Sean createAt 2021/6/23
 */
@Getter
@Setter
public class MessageContext {

    public static final String MESSAGE_CONTEXT = "AOMI@MESSAGE_CONTEXT";

    /**
     * 传输秘钥明文
     */
    private byte[] trk;

    /**
     * 请求参数明文
     */
    private byte[] payload;

    /**
     * 客户端信息
     */
    private Client client;

    private RequestMessage requestMessage;
}
