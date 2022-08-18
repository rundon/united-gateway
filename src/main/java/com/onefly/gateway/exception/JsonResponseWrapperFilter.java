package com.onefly.gateway.exception;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.onefly.gateway.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JsonResponseWrapperFilter implements ComplexFilter {

    public static final String IS_IGNOREAUTHFILTER = "ingore";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //操作针对某些路由跳过全局过滤器
        if (exchange.getAttributes().get(IS_IGNOREAUTHFILTER) != null) {
            return chain.filter(exchange);
        }
        //包装响应体
        ServerWebExchange newExchange = exchange.mutate().response(
                ServerHttpResponseDecoratorHelper.build(exchange, (originalBody) -> {
                    String requestUri = exchange.getRequest().getPath().pathWithinApplication().value();
                    MediaType responseMediaType = exchange.getResponse().getHeaders().getContentType();
                    log.info("Request [{}] response content-type is {}", requestUri, responseMediaType);
                    if (MediaType.APPLICATION_JSON.isCompatibleWith(responseMediaType)) {
                        return rewriteBody(exchange, originalBody);
                    } else {
                        return Mono.just(originalBody);
                    }
                })).build();
        return chain.filter(newExchange);
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }


    private Mono<byte[]> rewriteBody(ServerWebExchange exchange, byte[] originalBody) {
        HttpStatus originalResponseStatus = exchange.getResponse().getStatusCode();
        //将状态码统一重置为200，在这里重置才是终极解决办法
        log.debug("Response status code is {} , body is {}", originalResponseStatus, new String(originalBody));
        if (originalResponseStatus == HttpStatus.OK) {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            if (originalBody == null) {
                log.debug("下游服务响应内容为空，但是http状态码为200，则按照成功的响应体包装返回");
                return makeMono(new Result());
            } else {
                try {
                    //只能parse出JSONObject、JSONArray、Integer、Boolean等类型，当是一个string但是非json格式则抛出异常
                    Object jsonObject = JSON.parse(originalBody);
                    //如果响应内容已经包含了errcode字段，则表示下游的响应体本身已经是统一结果体了，无需再包装
                    if ((jsonObject instanceof JSONObject) && ((JSONObject) jsonObject).containsKey("code")) {
                        log.debug("服务响应体已经是统一结果体，无需包装");
                        return Mono.just(originalBody);
                    } else {
                        return makeMono(new Result().ok(jsonObject));
                    }
                } catch (Exception e) {
                    log.error("解析下游响应体异常", e);
                    return makeMono(new Result().ok(originalBody));
                }
            }
        } else {
            //非200 无法弹出
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            //如果不是401和403异常则重置为200状态码
//            if (!ArrayUtils.contains(new int[]{401, 403, 400}, originalResponseStatus.value())) {
//                exchange.getResponse().setStatusCode(HttpStatus.OK);
//            }

            //响应异常的报文
            if (originalBody == null) {
                return Mono.just(JSON.toJSONBytes(new Result().error(), SerializerFeature.WriteMapNullValue));
            } else {
                try {
                    //只能parse出JSONObject、JSONArray、Integer、Boolean等类型，当是一个string但是非json格式则抛出异常
                    Object jsonObject = JSON.parse(originalBody);
                    //如果响应内容已经包含了errcode字段，则表示下游的响应体本身已经是统一结果体了
                    if ((jsonObject instanceof JSONObject)) {
                        JSONObject jo = ((JSONObject) jsonObject);
                        if (jo.containsKey("code") && jo.containsKey("msg")) {
                            return Mono.just(originalBody);
                        } else if (jo.containsKey("error") && jo.containsKey("error_description")) {
                            String errorCode = jo.getString("error");
                            String message = jo.getString("error_description");
                            if ("Bad credentials".equals(message)) {
                                return Mono.just(JSON.toJSONBytes(new Result().error(10004, "账号或密码错误")
                                        , SerializerFeature.WriteMapNullValue));
                            } else if (errorCode.equals("invalid_token")) {
                                return Mono.just(JSON.toJSONBytes(new Result().error(450, "token 过期")
                                        , SerializerFeature.WriteMapNullValue));
                            } else if (errorCode.equals("invalid_grant")) {
                                return Mono.just(JSON.toJSONBytes(new Result().error(401, "未授权")
                                        , SerializerFeature.WriteMapNullValue));
                            } else {
                                return Mono.just(JSON.toJSONBytes(new Result().error(1000111, "未识别异常")
                                        , SerializerFeature.WriteMapNullValue));
                            }
                        } else {
                            //下游返回的包体是一个jsonobject，并不是规范的错误包体
                            if (jo.containsKey("status") && jo.containsKey("path")) {
                                return Mono.just(JSON.toJSONBytes(new Result().error(jo.getIntValue("status"), jo.getString("path"))
                                        , SerializerFeature.WriteMapNullValue));
                            } else {
                                return Mono.just(JSON.toJSONBytes(new Result().error(JSON.toJSONString(jsonObject)), SerializerFeature.WriteMapNullValue));
                            }
                        }
                    } else {
                        //不是一个jsonobject，可能是一个jsonarray
                        return Mono.just(JSON.toJSONBytes(new Result().error(JSON.toJSONString(jsonObject)), SerializerFeature.WriteMapNullValue));
                    }
                } catch (Exception e) {
                    log.error("解析下游响应体异常", e);
                    return Mono.just(JSON.toJSONBytes(new Result().error(new String(originalBody)), SerializerFeature.WriteMapNullValue));
                }
            }
        }
    }

    private Mono<byte[]> makeMono(Result result) {
        return Mono.just(JSON.toJSONBytes(result, SerializerFeature.WriteMapNullValue));
    }

}
