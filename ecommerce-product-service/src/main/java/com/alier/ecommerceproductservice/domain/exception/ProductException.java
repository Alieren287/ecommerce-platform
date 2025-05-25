package com.alier.ecommerceproductservice.domain.exception;

import com.alier.ecommercecore.common.exception.*;

/**
 * Factory class for creating product-related exceptions.
 * Supports both default messages from error codes and custom message overrides.
 */
public class ProductException {

    // Factory methods using default messages from error codes
    public static ValidationException validation(ProductErrorCode errorCode) {
        return new ValidationException(errorCode, errorCode.getDefaultMessage());
    }

    public static ResourceNotFoundException notFound(ProductErrorCode errorCode) {
        return new ResourceNotFoundException(errorCode, errorCode.getDefaultMessage());
    }

    public static ConflictException conflict(ProductErrorCode errorCode) {
        return new ConflictException(errorCode, errorCode.getDefaultMessage());
    }

    public static InternalServerException internalServer(ProductErrorCode errorCode) {
        return new InternalServerException(errorCode, errorCode.getDefaultMessage());
    }

    public static BusinessException business(ProductErrorCode errorCode) {
        return new BusinessException(errorCode, errorCode.getDefaultMessage());
    }

    // Factory methods with custom message overrides
    public static ValidationException validation(ProductErrorCode errorCode, String customMessage) {
        return new ValidationException(errorCode, customMessage);
    }

    public static ResourceNotFoundException notFound(ProductErrorCode errorCode, String customMessage) {
        return new ResourceNotFoundException(errorCode, customMessage);
    }

    public static ConflictException conflict(ProductErrorCode errorCode, String customMessage) {
        return new ConflictException(errorCode, customMessage);
    }

    public static InternalServerException internalServer(ProductErrorCode errorCode, String customMessage) {
        return new InternalServerException(errorCode, customMessage);
    }

    public static BusinessException business(ProductErrorCode errorCode, String customMessage) {
        return new BusinessException(errorCode, customMessage);
    }
} 