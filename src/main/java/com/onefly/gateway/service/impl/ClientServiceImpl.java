package com.onefly.gateway.service.impl;

import com.onefly.gateway.constant.CacheKey;
import com.onefly.gateway.dto.CreateClientDto;
import com.onefly.gateway.dto.UpdateClientDto;
import com.onefly.gateway.entity.Client;
import com.onefly.gateway.exception.ResourceNonExistException;
import com.onefly.gateway.repository.ClientRepository;
import com.onefly.gateway.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Sean createAt 2021/6/23
 */
@Slf4j
@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Override
    @Cacheable(cacheNames = CacheKey.CLIENT, key = "#clientId")
    public Client getClient(String clientId) {
        return clientRepository.findById(clientId).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Client save(CreateClientDto dto) {
        Client client = new Client();
        client.setClientPublicKey(dto.getClientPublicKey());
        client.setClientPrivateKey(dto.getClientPrivateKey());
        client.setPlatform(dto.getPlatform());
        return clientRepository.save(client);
    }

    @Override
    @CachePut(cacheNames = CacheKey.CLIENT, key = "#dto.clientId")
    @Transactional(rollbackFor = Exception.class)
    public Client update(UpdateClientDto dto) {
        if (dto.getClientId() == null) {
            throw new IllegalArgumentException("ID必填");
        }
        Client client = clientRepository.findById(dto.getClientId()).orElse(null);
        if (null == client) {
            throw new ResourceNonExistException("客户端不存在");
        }
        client.setClientPublicKey(dto.getClientPublicKey());
        client.setClientPrivateKey(dto.getClientPrivateKey());
        return clientRepository.save(client);
    }

    @Override
    @CacheEvict(cacheNames = CacheKey.CLIENT, key = "#clientId")
    @Transactional(rollbackFor = Exception.class)
    public void delete(String clientId) {
        clientRepository.deleteById(clientId);
    }

}
