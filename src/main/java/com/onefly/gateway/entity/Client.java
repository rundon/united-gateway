package com.onefly.gateway.entity;

import com.onefly.gateway.constant.SignType;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Sean createAt 2021/6/24
 */
@Data
@Entity
@Table(name = "client")
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
public class Client implements java.io.Serializable {
    private static final long serialVersionUID = -3384408008530642865L;

    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(length = 32)
    private String clientId;

    /**
     * 客户端公钥
     */
    @Column(name = "client_public_key", columnDefinition = "text  COMMENT '客户端公钥'")
    private String clientPublicKey;

    /**
     * 客户端私钥
     */
    @Column(name = "client_private_key", columnDefinition = "text  COMMENT '客户端私钥'")
    private String clientPrivateKey;

    /**
     * HmacMD5,HmacSHA1,HmacSHA256
     */
    @Column(name = "client_secret", columnDefinition = "text  COMMENT '客户端秘钥(HmacMD5,HmacSHA1,HmacSHA256)'")
    private String clientSecret;

    /**
     * 有效期
     */
    @Column(name = "validity_date", columnDefinition = "datetime  COMMENT '有效期为空时一直不过期'")
    private Date validityDate;

    /**
     * 所属平台
     */
    @Column(name = "platform", columnDefinition = "varchar(10)  COMMENT '所属平台 PC,IOS,ANDROID,API'")
    private String platform;

    /**
     * 创建人
     */
    @Column(name = "creator", columnDefinition = "bigint  COMMENT '创建人'")
    private Long creator;

    /**
     * 创建时间
     */
    @Column(name = "create_date", columnDefinition = "datetime  COMMENT '创建时间'")
    private Date createDate;
}
