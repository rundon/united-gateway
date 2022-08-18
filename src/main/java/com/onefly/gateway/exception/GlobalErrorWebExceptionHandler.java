package com.onefly.gateway.exception;

import com.google.common.collect.Maps;
import com.onefly.gateway.constant.Result;
import com.onefly.gateway.service.MessageService;
import com.onefly.gateway.utils.json.Json;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

/**
 * 全局异常处理
 *
 * @author Sean createAt 2021/5/8
 */
@Slf4j
@Setter
public class GlobalErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    private MessageService messageService;

    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resources, errorProperties, applicationContext);
        messageService = applicationContext.getBean(MessageService.class);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> map = Maps.newHashMap();
        Throwable t = getError(request);
        log.error("请求处理异常: {}", request.exchange().getRequest().getURI(), t);
        if (t instanceof ResponseStatusException) {
            Map<String, Object> returnMap = super.getErrorAttributes(request, options);
            map.put("code", returnMap.get("status"));
            map.put("msg", returnMap.get("path"));
            return map;
        }
        ServiceException se;
        if (t instanceof ServiceException) {
            se = (ServiceException) t;
        } else if (t instanceof IllegalArgumentException) {
            se = new ServiceException(t.getMessage(), t);
            se.setErrorCode(ErrorCode.PARAMS_ERROR);
        } else {
            se = new ServiceException(t);
        }
        Result result = Result.create(se.getErrorCode(), se.getMessage(), se.getPayload());
        Result.Entity body = result.getBody();
        Optional.ofNullable(body).ifPresent((tmp) -> {
            map.put("code", Integer.valueOf(tmp.getStatus()));
            map.put("msg", tmp.getDescribe());
            Optional.ofNullable(tmp.getPayload()).ifPresent((p) -> map.put("data", Json.toJson(p).toString()));
        });
        return map;
    }


    /**
     * 根据code获取对应的HttpStatus
     *
     * @param errorAttributes errorAttributes
     */
    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        return  200;
    }

}
