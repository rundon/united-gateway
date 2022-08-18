package com.onefly.gateway.api;

import com.onefly.gateway.constant.Result;
import com.onefly.gateway.exception.ErrorCode;

/**
 * @author 田尘殇Sean(sean.snow @ live.com) createAt 2016/12/26
 */
public class AbstractController {

    protected Result success() {
        return success(null);
    }

    protected Result success(Object payload) {
        return Result.create(ErrorCode.SUCCESS, ErrorCode.SUCCESS.getMessage(), payload);
    }

    protected Result success(Object payload, String describe) {
        return Result.create(ErrorCode.SUCCESS, describe, payload);
    }

}
