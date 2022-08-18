package com.onefly.gateway.exception;

import java.io.Serializable;

/**
 * 资源不存在异常
 *
 * @author 田尘殇Sean(sean.snow @ live.com) Create At 16/7/27
 */
public class ResourceNonExistException extends ServiceException {

    private static final long serialVersionUID = 8251792064612904687L;

    public ResourceNonExistException() {
        this("资源不存在");
    }


    public ResourceNonExistException(String message) {
        super(message);
    }

    public ResourceNonExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNonExistException(Throwable cause) {
        super(cause);
    }

    @Override
    public Serializable getErrorCode() {
        return ErrorCode.RESOURCE_NON_EXIST;
    }
}
