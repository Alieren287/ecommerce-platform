package com.alier.ecommercecore.common.exception;

import lombok.Getter;

/**
 * Exception for validation errors.
 * Always maps to HTTP 400 Bad Request status.
 */
@Getter
public class ValidationException extends BusinessException {

    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ValidationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
} 