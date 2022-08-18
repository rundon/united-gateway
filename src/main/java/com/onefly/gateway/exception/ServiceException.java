package com.onefly.gateway.exception;

import java.io.Serializable;

/**
 * 服务统一异常类
 *
 * @author 田尘殇Sean(sean.snow @ live.com) Create At 16/7/25
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = -8914766196902007963L;

    private Serializable errorCode = ErrorCode.EXCEPTION;

    private Serializable payload = null;

    public ServiceException() {
        super();
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public Serializable getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Serializable errorCode) {
        this.errorCode = errorCode;
    }

    public Serializable getPayload() {
        return payload;
    }

    public void setPayload(Serializable payload) {
        this.payload = payload;
    }
}

