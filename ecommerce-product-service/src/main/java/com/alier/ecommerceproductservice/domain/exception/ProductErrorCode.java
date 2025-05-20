package com.alier.ecommerceproductservice.domain.exception;

import com.alier.ecommercewebcore.rest.exception.HttpErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Product domain-specific error codes with associated HTTP status codes.
 */
@RequiredArgsConstructor
@Getter
public enum ProductErrorCode implements HttpErrorCode {
    // Not Found errors
    PRODUCT_NOT_FOUND("ERR-PRD-001", "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_VARIANT_NOT_FOUND("ERR-PRD-011", "Product variant not found", HttpStatus.NOT_FOUND),

    // Conflict errors
    PRODUCT_SKU_EXISTS("ERR-PRD-002", "Product with this SKU already exists", HttpStatus.CONFLICT),
    PRODUCT_OUT_OF_STOCK("ERR-PRD-003", "Product is out of stock", HttpStatus.CONFLICT),
    PRODUCT_VARIANT_SKU_EXISTS("ERR-PRD-012", "Product variant with this SKU already exists", HttpStatus.CONFLICT),
    PRODUCT_VARIANT_OUT_OF_STOCK("ERR-PRD-013", "Product variant is out of stock", HttpStatus.CONFLICT),

    // Validation errors
    INVALID_PRODUCT_PRICE("ERR-PRD-004", "Invalid product price", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_NAME("ERR-PRD-005", "Invalid product name", HttpStatus.BAD_REQUEST),
    INVALID_STOCK_QUANTITY("ERR-PRD-006", "Invalid stock quantity", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_OPERATION("ERR-PRD-007", "Invalid product operation", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_VARIANT_PRICE("ERR-PRD-014", "Invalid product variant price", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_VARIANT_NAME("ERR-PRD-015", "Invalid product variant name", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_VARIANT_OPERATION("ERR-PRD-016", "Invalid product variant operation", HttpStatus.BAD_REQUEST),

    // Operation errors
    PRODUCT_ACTIVATION_FAILED("ERR-PRD-008", "Product activation failed", HttpStatus.BAD_REQUEST),
    PRODUCT_DEACTIVATION_FAILED("ERR-PRD-009", "Product deactivation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    PRODUCT_UPDATE_FAILED("ERR-PRD-010", "Product update failed", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
} 