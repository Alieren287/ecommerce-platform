package com.alier.ecommercecore.common.exception;

import lombok.Getter;

/**
 * Base business exception that serves as the parent for all business logic exceptions.
 * Can be used directly for general business rule violations or extended for specific error types.
 */
@Getter
public class BusinessException extends BaseBusinessException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
} 