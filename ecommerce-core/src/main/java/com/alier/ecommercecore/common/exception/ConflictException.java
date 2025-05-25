package com.alier.ecommercecore.common.exception;

import lombok.Getter;

/**
 * Exception for conflict errors.
 * Always maps to HTTP 409 Conflict status.
 */
@Getter
public class ConflictException extends BusinessException {

    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ConflictException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
} 