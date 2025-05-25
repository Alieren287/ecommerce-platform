package com.alier.ecommercecore.common.exception;

import lombok.Getter;

/**
 * Base business exception that serves as the parent for all business logic exceptions.
 * Can be used directly for general business rule violations or extended for specific error types.
 * <p>
 * Provides static factory methods for creating categorized exceptions with default messages.
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

    // ========== Static Factory Methods ==========

    /**
     * Creates a ValidationException with default message from error code.
     *
     * @param errorCode The error code with embedded default message
     * @return ValidationException with default message
     */
    public static ValidationException validation(ErrorCode errorCode) {
        return new ValidationException(errorCode, errorCode.getDefaultMessage());
    }

    /**
     * Creates a ValidationException with custom message override.
     *
     * @param errorCode     The error code
     * @param customMessage Custom message to override the default
     * @return ValidationException with custom message
     */
    public static ValidationException validation(ErrorCode errorCode, String customMessage) {
        return new ValidationException(errorCode, customMessage);
    }

    /**
     * Creates a ResourceNotFoundException with default message from error code.
     *
     * @param errorCode The error code with embedded default message
     * @return ResourceNotFoundException with default message
     */
    public static ResourceNotFoundException notFound(ErrorCode errorCode) {
        return new ResourceNotFoundException(errorCode, errorCode.getDefaultMessage());
    }

    /**
     * Creates a ResourceNotFoundException with custom message override.
     *
     * @param errorCode     The error code
     * @param customMessage Custom message to override the default
     * @return ResourceNotFoundException with custom message
     */
    public static ResourceNotFoundException notFound(ErrorCode errorCode, String customMessage) {
        return new ResourceNotFoundException(errorCode, customMessage);
    }

    /**
     * Creates a ConflictException with default message from error code.
     *
     * @param errorCode The error code with embedded default message
     * @return ConflictException with default message
     */
    public static ConflictException conflict(ErrorCode errorCode) {
        return new ConflictException(errorCode, errorCode.getDefaultMessage());
    }

    /**
     * Creates a ConflictException with custom message override.
     *
     * @param errorCode     The error code
     * @param customMessage Custom message to override the default
     * @return ConflictException with custom message
     */
    public static ConflictException conflict(ErrorCode errorCode, String customMessage) {
        return new ConflictException(errorCode, customMessage);
    }

    /**
     * Creates an InternalServerException with default message from error code.
     *
     * @param errorCode The error code with embedded default message
     * @return InternalServerException with default message
     */
    public static InternalServerException internalServer(ErrorCode errorCode) {
        return new InternalServerException(errorCode, errorCode.getDefaultMessage());
    }

    /**
     * Creates an InternalServerException with custom message override.
     *
     * @param errorCode     The error code
     * @param customMessage Custom message to override the default
     * @return InternalServerException with custom message
     */
    public static InternalServerException internalServer(ErrorCode errorCode, String customMessage) {
        return new InternalServerException(errorCode, customMessage);
    }

    /**
     * Creates a BusinessException with default message from error code.
     *
     * @param errorCode The error code with embedded default message
     * @return BusinessException with default message
     */
    public static BusinessException business(ErrorCode errorCode) {
        return new BusinessException(errorCode, errorCode.getDefaultMessage());
    }

    /**
     * Creates a BusinessException with custom message override.
     *
     * @param errorCode     The error code
     * @param customMessage Custom message to override the default
     * @return BusinessException with custom message
     */
    public static BusinessException business(ErrorCode errorCode, String customMessage) {
        return new BusinessException(errorCode, customMessage);
    }
} 