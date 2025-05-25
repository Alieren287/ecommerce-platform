package com.alier.ecommercecore.common.exception;

import lombok.Getter;

/**
 * Exception for internal server errors.
 * Always maps to HTTP 500 Internal Server Error status.
 */
@Getter
public class InternalServerException extends BusinessException {

    public InternalServerException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public InternalServerException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
} 