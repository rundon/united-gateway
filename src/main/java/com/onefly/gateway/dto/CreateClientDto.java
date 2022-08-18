package com.onefly.gateway.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author Sean createAt 2021/6/25
 */
@Data
public class CreateClientDto implements java.io.Serializable {

    private static final long serialVersionUID = 5714089793031687051L;

    /**
     * 客户端公钥
     */
    private String clientPublicKey;


    private String clientPrivateKey;

    @NotEmpty
    private String platform;
}
