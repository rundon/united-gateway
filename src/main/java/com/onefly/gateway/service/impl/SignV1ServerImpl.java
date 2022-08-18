package com.onefly.gateway.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.onefly.gateway.constant.Common;
import com.onefly.gateway.constant.HttpHeader;
import com.onefly.gateway.constant.SignType;
import com.onefly.gateway.entity.Client;
import com.onefly.gateway.exception.ResourceNonExistException;
import com.onefly.gateway.exception.ServiceException;
import com.onefly.gateway.exception.SignatureException;
import com.onefly.gateway.service.ClientService;
import com.onefly.gateway.service.SignServer;
import com.onefly.gateway.utils.crypto.RSA;
import com.onefly.gateway.utils.crypto.RSAUtil;
import com.onefly.gateway.utils.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * v1报文
 */
@Slf4j
public class SignV1ServerImpl implements SignServer {
    private final List<HttpMessageReader<?>> messageReaders;

    private final Set<MessageBodyDecoder> messageBodyDecoders;

    private final Set<MessageBodyEncoder> messageBodyEncoders;

    private final ClientService clientService;

    public SignV1ServerImpl(
            List<HttpMessageReader<?>> messageReaders,
            Set<MessageBodyDecoder> messageBodyDecoders,
            Set<MessageBodyEncoder> messageBodyEncoders,
            ClientService clientService
    ) {
        this.messageReaders = messageReaders;
        this.messageBodyDecoders = messageBodyDecoders;
        this.messageBodyEncoders = messageBodyEncoders;
        this.clientService = clientService;
    }

    @Override
    public Mono<Void> sign(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String clientId = request.getHeaders().getFirst(HttpHeader.CLIENT_ID);
        String sign = request.getHeaders().getFirst(HttpHeader.SIGNATURE);
        log.debug("请求用户标识: clientId: {}", clientId);
        if (StringUtils.isEmpty(clientId)) {
            return Mono.error(new ResourceNonExistException("客户端不存在: " + clientId));
        }
       // Optional.ofNullable(request.getHeaders().getFirst(HttpHeader.PARAMS)).orElseThrow(() -> new SignatureException(HttpHeader.PARAMS + "不能为空"));
        String signType = request.getHeaders().getFirst(HttpHeader.METHOD) == null ? SignType.HmacSHA256.toString() : request.getHeaders().getFirst(HttpHeader.METHOD);
        Client client = clientService.getClient(clientId);
        if (null == client || SignType.RSA.toString().equals(signType) && StringUtils.isEmpty(client.getClientPublicKey())
                || !SignType.RSA.toString().equals(signType) && StringUtils.isEmpty(client.getClientSecret())) {
            log.error("请求方没有配置密钥信息");
            return Mono.error(new ServiceException("请求方没有配置密钥信息"));
        }
        if (client.getValidityDate() != null && LocalDateTime.now().isAfter(client.getValidityDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())) {
            return Mono.error(new ServiceException("密钥信息已经过期"));
        }
        exchange.getAttributes().put("client", client);

        if (exchange.getRequest().getMethod() == HttpMethod.GET) {
            return handleGet(exchange, chain, client, sign);
        }
        return handleOther(exchange, chain, client, sign);
    }

    /**
     * 转换成json
     *
     * @param body
     * @return
     */
    private byte[] loadToJsonStr(byte[] body, HttpHeaders header) {
        MediaType responseMediaType = header.getContentType();
        String timestamp = header.getFirst(HttpHeader.TIMESTAMP);
        String nonce = header.getFirst(HttpHeader.NONCE);
        String headParams = header.getFirst(HttpHeader.PARAMS);//以逗号隔开  params + timestamp + nonce
        String str = new String(body, StandardCharsets.UTF_8);
        List<String> params = Arrays.asList(headParams.split(","));
        Map<String, String> map = Maps.newHashMap();
        if (MediaType.APPLICATION_JSON.isCompatibleWith(responseMediaType)) {
            Map<String, Object> json = Json.fromJson(str, new TypeReference<Map<String, Object>>() {
            });
            json.entrySet().stream().forEach(entry -> {
                if (entry.getValue() != null && !(entry.getValue() instanceof ArrayList || entry.getValue() instanceof JsonNode)) {
                    map.put(entry.getKey(), entry.getValue().toString());
                }
            });
        } else {
            String[] array = str.split("&");
            for (String item : array) {
                String[] index = item.split("=");
                if (index.length > 1) {
                    map.put(index[0], index[1]);
                } else {
                    map.put(index[0], "");
                }
            }
        }
        return loadMapToByte(map, params, header.getFirst(HttpHeader.VERSION), timestamp, nonce);
    }

    private byte[] loadMapToByte(Map<String, String> map, List<String> params, String version, String timestamp, String nonce) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < params.size(); i++) {
            String param = params.get(i);
            sb.append(param + "=" + map.get(param));
            if (i != params.size() - 1) {
                sb.append("&");
            }
        }
        sb.append(version).append(nonce).append(timestamp);
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] loadSignatureMsg(ServerHttpRequest request) {
        Map<String, String> map = Maps.newHashMap();
        HttpHeaders header = request.getHeaders();
        String timestamp = header.getFirst(HttpHeader.TIMESTAMP);
        String nonce = header.getFirst(HttpHeader.NONCE);
        String headParams = header.getFirst(HttpHeader.PARAMS);//以逗号隔开  params + timestamp + nonce
        List<String> params = Arrays.asList(headParams.split(","));
        request.getQueryParams().forEach((k, v) -> {
            if (null != v && v.size() > 0) {
                try {
                    map.put(k, URLDecoder.decode(v.get(0), "UTF-8"));
                } catch (UnsupportedEncodingException ignored) {
                }
            }
        });
        return loadMapToByte(map, params, header.getFirst(HttpHeader.VERSION), timestamp, nonce);
    }

    private Mono<Void> handleOther(ServerWebExchange exchange, GatewayFilterChain chain, Client client, String sign) {
        ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);
        HttpHeaders header = exchange.getRequest().getHeaders();
        String signType = header.getFirst(HttpHeader.METHOD) == null ? SignType.HmacSHA256.toString() : header.getFirst(HttpHeader.METHOD);
        // TODO: flux or mono
        Mono<byte[]> modifiedBody = serverRequest.bodyToMono(byte[].class)
                .flatMap(body -> {
                    if (StringUtils.isNotBlank(header.getFirst(HttpHeader.PARAMS))) {
                        try {
                            verify(client, loadToJsonStr(body, header), sign, signType);
                        } catch (Exception e) {
                            log.error("签名验证失败: {}", e.getMessage());
                            return Mono.error(e);
                        }
                    }
                    return Mono.just(body);
                });
        BodyInserter<Mono<byte[]>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromPublisher(modifiedBody, byte[].class);
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchange.getRequest().getHeaders());
        // the new content type will be computed by bodyInserter
        // and then set in the request decorator
        headers.remove(HttpHeaders.CONTENT_LENGTH);
        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
        return bodyInserter.insert(outputMessage, new BodyInserterContext()).then(Mono.defer(() -> {
            ServerHttpRequest decorator = decorate(exchange, headers, outputMessage);
            return chain.filter(
                    exchange.mutate()
                            .request(decorator)
                            .response(new SignV1HttpResponse(
                                    exchange,
                                    messageReaders,
                                    messageBodyDecoders,
                                    messageBodyEncoders
                            ))
                            .build()
            );
        }));
    }

    private Mono<Void> handleGet(ServerWebExchange exchange, GatewayFilterChain chain, Client client, String sign) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders header = request.getHeaders();
        String signType = header.getFirst(HttpHeader.METHOD) == null ? SignType.HmacSHA256.toString() : header.getFirst(HttpHeader.METHOD);
        if (StringUtils.isNotBlank(header.getFirst(HttpHeader.PARAMS))) {
            try {
                verify(client, loadSignatureMsg(request), sign, signType);
            } catch (Exception e) {
                log.error("签名校验失败: {}", e.getMessage());
                return Mono.error(e);
            }
        }
        return chain.filter(
                exchange.mutate()
                        .request(request.mutate().build())
                        .response(new SignV1HttpResponse(
                                exchange,
                                messageReaders,
                                messageBodyDecoders,
                                messageBodyEncoders
                        ))
                        .build()
        );
    }

    public void verify(Client client, byte[] body, String sign, String signType) {
        if (StringUtils.isEmpty(sign)) {
            log.error("签名信息为空");
            throw new SignatureException("签名信息为空");
        }
        String bodyStr = new String(body, StandardCharsets.UTF_8);
        log.debug("请求原始数据: [{}]", bodyStr);
        try {
            boolean isOk = false;
            if (SignType.RSA.toString().equals(signType)) {
                byte[] signBytes = Base64.getDecoder().decode(sign);
                PublicKey publicKey = RSA.parsePublicKeyWithBase64(client.getClientPublicKey());
                isOk = RSAUtil.signVerify(publicKey, Common.SIGN_ALGORITHMS, body, signBytes);
            } else if (SignType.HmacMD5.toString().equals(signType)) {
                String gHmac = Hashing.hmacMd5(client.getClientSecret().getBytes()).newHasher().putBytes(body).hash().toString();
                isOk = gHmac.equals(sign);
            } else if (SignType.HmacSHA1.toString().equals(signType)) {
                String gHmac = Hashing.hmacSha1(client.getClientSecret().getBytes()).newHasher().putBytes(body).hash().toString();
                isOk = gHmac.equals(sign);
            } else if (SignType.HmacSHA256.toString().equals(signType)) {
                String gHmac = Hashing.hmacSha256(client.getClientSecret().getBytes()).newHasher().putBytes(body).hash().toString();
                isOk = gHmac.equals(sign);
            } else {
                throw new SignatureException("只支持RSA(支持密文传输),HmacMD5,HmacSHA1,HmacSHA256");
            }
            if (isOk) {
                return;
            }
            log.error("签名错误:{}", sign);
            throw new SignatureException("签名校验失败");
        } catch (Exception e) {
            log.error("签名执行失败: {}", e.getMessage());
            throw new SignatureException("签名校验异常", e);
        }
    }

    private ServerHttpRequestDecorator decorate(ServerWebExchange exchange, HttpHeaders headers, CachedBodyOutputMessage outputMessage) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                long contentLength = headers.getContentLength();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(headers);
                if (contentLength > 0) {
                    httpHeaders.setContentLength(contentLength);
                } else {
                    // TODO: this causes a 'HTTP/1.1 411 Length Required' // on
                    // httpbin.org
                    httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                }
                return httpHeaders;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return outputMessage.getBody();
            }
        };
    }
}
