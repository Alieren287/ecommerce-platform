package com.alier.ecommercecore.common.exception;

/**
 * Usage guide and examples for the simplified categorized exception system with default messages.
 * 
 * This class demonstrates how to use the exception hierarchy with embedded default messages:
 * 
 * 1. ValidationException - always maps to HTTP 400 Bad Request
 * 2. ResourceNotFoundException - always maps to HTTP 404 Not Found 
 * 3. ConflictException - always maps to HTTP 409 Conflict
 * 4. InternalServerException - always maps to HTTP 500 Internal Server Error
 * 5. BusinessException - general business logic violations, maps to HTTP 400 Bad Request
 * 
 * Each exception type automatically maps to its specific HTTP status code.
 * Error codes now include default messages, providing a direct relationship between codes and messages.
 * 
 * Examples:
 * 
 * // Using default messages from error codes (recommended)
 * throw new ValidationException(ProductErrorCode.PRODUCT_NAME_NULL);  // Uses: "Product name is required and cannot be null"
 * throw new ResourceNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND);  // Uses: "Product not found with the specified identifier"
 * throw new ConflictException(ProductErrorCode.PRODUCT_SKU_EXISTS);  // Uses: "A product with this SKU already exists in the system"
 * 
 * // Custom message overrides (when you need different context)
 * throw new ValidationException(ProductErrorCode.PRODUCT_NAME_NULL, "Product name is missing in the import file");
 * throw new ResourceNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND, "Product with SKU 'ABC123' was not found");
 * throw new ConflictException(ProductErrorCode.PRODUCT_SKU_EXISTS, "Cannot update: SKU 'XYZ789' already exists");
 * 
 * For domain-specific exceptions, use factory methods like ProductException:
 * 
 * // Using default messages (clean and simple)
 * throw ProductException.validation(ProductErrorCode.PRODUCT_NAME_NULL);
 * throw ProductException.notFound(ProductErrorCode.PRODUCT_NOT_FOUND);
 * throw ProductException.conflict(ProductErrorCode.PRODUCT_SKU_EXISTS);
 * throw ProductException.internalServer(ProductErrorCode.PRODUCT_UPDATE_FAILED);
 * 
 * // Using custom messages when needed (flexibility for context)
 * throw ProductException.validation(ProductErrorCode.PRODUCT_NAME_NULL, "Name is required for product creation");
 * throw ProductException.notFound(ProductErrorCode.PRODUCT_NOT_FOUND, "Product with ID " + productId + " not found");
 * throw ProductException.conflict(ProductErrorCode.PRODUCT_SKU_EXISTS, "Cannot import: SKU already exists in catalog");
 * 
 * Benefits of this approach:
 * - Direct relationship between error codes and messages (type safety)
 * - Default messages for 90% of use cases (simplicity)
 * - Custom message overrides for special contexts (flexibility)
 * - No separate message classes needed (maintainability)
 * - Compile-time safety (no mismatched codes and messages)
 */
public final class ExceptionUsageGuide {
    private ExceptionUsageGuide() {
        // Utility class - prevent instantiation
    }
} 