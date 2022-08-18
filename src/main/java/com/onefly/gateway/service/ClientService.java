package com.onefly.gateway.service;

import com.onefly.gateway.dto.CreateClientDto;
import com.onefly.gateway.dto.UpdateClientDto;
import com.onefly.gateway.entity.Client;

/**
 * 秘钥服务
 *
 * @author Sean createAt 2021/6/22
 */
public interface ClientService {

    /**
     * 获取
     *
     * @param clientId 客户端ID
     * @return 客户端公钥
     */
    Client getClient(String clientId);

    /**
     * 保存
     *
     * @param dto
     * @return
     */
    Client save(CreateClientDto dto);

    /**
     * 更新
     *
     * @param dto
     * @return
     */
    Client update(UpdateClientDto dto);

    /**
     * 删除
     *
     * @param clientId
     */
    void delete(String clientId);
}
