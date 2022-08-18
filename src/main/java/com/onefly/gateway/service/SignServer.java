package com.onefly.gateway.service;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 签名
 */
public interface SignServer {

    Mono<Void> sign(ServerWebExchange exchange, GatewayFilterChain chain);
}
