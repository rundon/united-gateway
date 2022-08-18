package com.onefly.gateway.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Sean createAt 2021/6/25
 */
@Getter
@Setter
public class UpdateClientDto implements java.io.Serializable {

    private static final long serialVersionUID = 1811085515771997070L;

    private String clientId;

    /**
     * 客户端公钥
     */
    private String clientPublicKey;


    private String clientPrivateKey;

}
