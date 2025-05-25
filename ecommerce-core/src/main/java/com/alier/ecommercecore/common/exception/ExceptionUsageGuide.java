package com.alier.ecommercecore.common.exception;

/**
 * Exception Usage Guide
 * <p>
 * This guide demonstrates how to use the exception hierarchy in the ecommerce platform.
 * All modules should use BusinessException factory methods for consistent exception handling.
 * <p>
 * ## Basic Usage with Default Messages
 * <p>
 * Use error codes with embedded default messages:
 * <p>
 * ```java
 * // Validation errors (400 Bad Request)
 * throw BusinessException.validation(ProductErrorCode.PRODUCT_NAME_NULL);
 * <p>
 * // Resource not found (404 Not Found)
 * throw BusinessException.notFound(ProductErrorCode.PRODUCT_NOT_FOUND);
 * <p>
 * // Business conflicts (409 Conflict)
 * throw BusinessException.conflict(ProductErrorCode.PRODUCT_SKU_EXISTS);
 * <p>
 * // Internal server errors (500 Internal Server Error)
 * throw BusinessException.internalServer(ProductErrorCode.PRODUCT_UPDATE_FAILED);
 * ```
 * <p>
 * ## Custom Messages (when context is needed)
 * <p>
 * Override default messages with custom context:
 * <p>
 * ```java
 * throw BusinessException.validation(ProductErrorCode.PRODUCT_NAME_NULL, "Name is required for product creation");
 * throw BusinessException.notFound(ProductErrorCode.PRODUCT_NOT_FOUND, "Product with ID " + productId + " not found");
 * throw BusinessException.conflict(ProductErrorCode.PRODUCT_SKU_EXISTS, "Cannot import: SKU already exists in catalog");
 * ```
 * <p>
 * ## Error Code Implementation
 * <p>
 * Each module should implement ErrorCode interface:
 * <p>
 * ```java
 * public enum ProductErrorCode implements ErrorCode {
 * PRODUCT_NOT_FOUND("ERR-PRD-001", "Product not found with the specified identifier"),
 * PRODUCT_NAME_NULL("ERR-PRD-002", "Product name cannot be null or empty");
 * <p>
 * private final String code;
 * private final String defaultMessage;
 * <p>
 * ProductErrorCode(String code, String defaultMessage) {
 * this.code = code;
 * this.defaultMessage = defaultMessage;
 * }
 *
 * @Override public String getCode() { return code; }
 * @Override public String getDefaultMessage() { return defaultMessage; }
 * }
 * ```
 * <p>
 * ## Benefits
 * <p>
 * - **Consistency**: All modules use the same BusinessException factory methods
 * - **Type Safety**: Compile-time relationship between error codes and messages
 * - **Flexibility**: Default messages for common cases, custom messages for specific contexts
 * - **HTTP Mapping**: Automatic HTTP status code mapping in GlobalRestExceptionHandler
 * - **Maintainability**: Centralized exception creation logic
 */
public final class ExceptionUsageGuide {
    private ExceptionUsageGuide() {
        // Utility class - prevent instantiation
    }
} 