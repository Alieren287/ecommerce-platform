package com.alier.ecommerceproductservice.domain.exception;

import com.alier.ecommercecore.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Product domain-specific error codes with default messages.
 */
@RequiredArgsConstructor
@Getter
public enum ProductErrorCode implements ErrorCode {
    // Not Found errors
    PRODUCT_NOT_FOUND("ERR-PRD-001", "Product not found with the specified identifier"),
    PRODUCT_VARIANT_NOT_FOUND("ERR-PRD-011", "Product variant not found with the specified identifier"),

    // Conflict errors
    PRODUCT_SKU_EXISTS("ERR-PRD-002", "A product with this SKU already exists in the system"),
    PRODUCT_OUT_OF_STOCK("ERR-PRD-003", "Product is currently out of stock and cannot be purchased"),
    PRODUCT_VARIANT_SKU_EXISTS("ERR-PRD-012", "A product variant with this SKU already exists in the system"),
    PRODUCT_VARIANT_OUT_OF_STOCK("ERR-PRD-013", "Product variant is currently out of stock and cannot be purchased"),

    // Validation errors - Product Name
    PRODUCT_NAME_NULL("ERR-PRD-004", "Product name is required and cannot be null"),
    PRODUCT_NAME_EMPTY("ERR-PRD-005", "Product name is required and cannot be empty or contain only whitespace"),
    PRODUCT_NAME_TOO_LONG("ERR-PRD-006", "Product name exceeds the maximum allowed length of 255 characters"),

    // Validation errors - Product Price
    PRODUCT_PRICE_NULL("ERR-PRD-007", "Product price is required and cannot be null"),
    PRODUCT_PRICE_NEGATIVE("ERR-PRD-008", "Product price cannot be negative. Please provide a valid positive price"),
    PRODUCT_PRICE_ZERO("ERR-PRD-009", "Product price cannot be zero. Please provide a valid positive price"),

    // Validation errors - Stock Quantity
    STOCK_QUANTITY_NULL("ERR-PRD-010", "Stock quantity is required and cannot be null"),
    STOCK_QUANTITY_NEGATIVE("ERR-PRD-014", "Stock quantity cannot be negative. Please provide a valid quantity of zero or more"),

    // Validation errors - Product Variant Name
    PRODUCT_VARIANT_NAME_NULL("ERR-PRD-015", "Product variant name is required and cannot be null"),
    PRODUCT_VARIANT_NAME_EMPTY("ERR-PRD-016", "Product variant name is required and cannot be empty or contain only whitespace"),
    PRODUCT_VARIANT_NAME_TOO_LONG("ERR-PRD-017", "Product variant name exceeds the maximum allowed length of 255 characters"),

    // Validation errors - Product Variant Price
    PRODUCT_VARIANT_PRICE_NULL("ERR-PRD-018", "Product variant price is required and cannot be null"),
    PRODUCT_VARIANT_PRICE_NEGATIVE("ERR-PRD-019", "Product variant price cannot be negative. Please provide a valid positive price"),
    PRODUCT_VARIANT_PRICE_ZERO("ERR-PRD-020", "Product variant price cannot be zero. Please provide a valid positive price"),

    // Operation errors - Product
    PRODUCT_ACTIVATION_NO_STOCK("ERR-PRD-021", "Cannot activate product because it has no stock. Please add inventory before activation"),
    PRODUCT_ACTIVATION_NO_IMAGES("ERR-PRD-022", "Cannot activate product because it has no images. Please upload at least one product image before activation"),
    PRODUCT_DECREASE_QUANTITY_INVALID("ERR-PRD-023", "Quantity to decrease must be a positive number greater than zero"),
    PRODUCT_INCREASE_QUANTITY_INVALID("ERR-PRD-024", "Quantity to increase must be a positive number greater than zero"),
    PRODUCT_IMAGE_URL_EMPTY("ERR-PRD-025", "Image URL cannot be empty or contain only whitespace"),
    PRODUCT_DISCOUNT_INVALID("ERR-PRD-026", "Discount percentage must be between 0 and 100"),

    // Operation errors - Product Variant
    PRODUCT_VARIANT_DECREASE_QUANTITY_INVALID("ERR-PRD-027", "Quantity to decrease must be a positive number greater than zero"),
    PRODUCT_VARIANT_INCREASE_QUANTITY_INVALID("ERR-PRD-028", "Quantity to increase must be a positive number greater than zero"),
    PRODUCT_VARIANT_ATTRIBUTE_KEY_EMPTY("ERR-PRD-029", "Attribute key cannot be empty or contain only whitespace"),

    // System errors
    PRODUCT_UPDATE_FAILED("ERR-PRD-030", "Product update operation failed due to a system error. Please try again"),

    // General validation errors
    INVALID_INPUT_PARAMETER("ERR-PRD-031", "Invalid input parameter provided"),
    INVALID_PRICE_RANGE("ERR-PRD-032", "Invalid price range specified"),
    PRODUCT_REINDEX_FAILED("ERR-PRD-033", "Product reindexing operation failed");

    private final String code;
    private final String defaultMessage;
} 