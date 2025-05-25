package com.alier.ecommercecore.common.exception;

import lombok.Getter;

/**
 * Exception for resource not found errors.
 * Always maps to HTTP 404 Not Found status.
 */
@Getter
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
} 