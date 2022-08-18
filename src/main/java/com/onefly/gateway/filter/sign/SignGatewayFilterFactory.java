package com.onefly.gateway.filter.sign;

import com.onefly.gateway.constant.HttpHeader;
import com.onefly.gateway.constant.MessageVersion;
import com.onefly.gateway.service.SignServer;
import com.onefly.gateway.service.impl.SignV1ServerImpl;
import com.onefly.gateway.service.impl.SignV2ServerImpl;
import com.onefly.gateway.service.ClientService;
import com.onefly.gateway.service.MessageService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * @author Sean createAt 2021/6/24
 */
@Slf4j
@Component
public class SignGatewayFilterFactory extends AbstractGatewayFilterFactory<SignGatewayFilterFactory.Config> {

    @Autowired
    private ServerCodecConfigurer codecConfigurer;

    @Autowired
    private Set<MessageBodyDecoder> bodyDecoders;

    @Autowired
    private Set<MessageBodyEncoder> bodyEncoders;

    @Autowired
    private ClientService clientService;

    @Autowired
    private MessageService messageService;

    public SignGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            //请求头
            HttpHeaders headers = request.getHeaders();
            // 报文版本
            String messageVersion = Optional.ofNullable(headers.getFirst(HttpHeader.MESSAGE_VERSION)).orElse(MessageVersion.V1_0_0.getVersion());
            MessageVersion version = Optional.ofNullable(MessageVersion.of(messageVersion)).orElse(MessageVersion.LATEST);
            SignServer signServer;
            switch (version) {
                case V1_0_0:
                    signServer = new SignV1ServerImpl(
                            codecConfigurer.getReaders(),
                            bodyDecoders,
                            bodyEncoders,
                            clientService
                    );
                    break;
                case V2_0_0:
                    signServer = new SignV2ServerImpl(
                            messageService,
                            codecConfigurer.getReaders(),
                            bodyDecoders,
                            bodyEncoders
                    );
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + version);
            }
            return signServer.sign(exchange, chain);
        };
    }

    @Getter
    @Setter
    public static class Config {

    }


}
