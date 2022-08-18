package com.onefly.gateway.exception;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;

public interface ComplexFilter extends GatewayFilter, GlobalFilter, Ordered {
}
