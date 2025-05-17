package com.alier.ecommercewebcore.rest.exception;

import com.alier.ecommercecore.common.exception.BaseBusinessException;
import lombok.Getter;

@Getter
public class RestBusinessException extends BaseBusinessException {

    private final HttpErrorCode errorCode;

    public RestBusinessException(HttpErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public RestBusinessException(HttpErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}
