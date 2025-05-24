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
    PRODUCT_NOT_FOUND("ERR-PRD-001", HttpStatus.NOT_FOUND),
    PRODUCT_VARIANT_NOT_FOUND("ERR-PRD-011", HttpStatus.NOT_FOUND),

    // Conflict errors
    PRODUCT_SKU_EXISTS("ERR-PRD-002", HttpStatus.CONFLICT),
    PRODUCT_OUT_OF_STOCK("ERR-PRD-003", HttpStatus.CONFLICT),
    PRODUCT_VARIANT_SKU_EXISTS("ERR-PRD-012", HttpStatus.CONFLICT),
    PRODUCT_VARIANT_OUT_OF_STOCK("ERR-PRD-013", HttpStatus.CONFLICT),

    // Validation errors
    INVALID_PRODUCT_PRICE("ERR-PRD-004", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_NAME("ERR-PRD-005", HttpStatus.BAD_REQUEST),
    INVALID_STOCK_QUANTITY("ERR-PRD-006", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_OPERATION("ERR-PRD-007", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_VARIANT_PRICE("ERR-PRD-014", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_VARIANT_NAME("ERR-PRD-015", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_VARIANT_OPERATION("ERR-PRD-016", HttpStatus.BAD_REQUEST),

    // Operation errors
    PRODUCT_ACTIVATION_FAILED("ERR-PRD-008", HttpStatus.BAD_REQUEST),
    PRODUCT_DEACTIVATION_FAILED("ERR-PRD-009", HttpStatus.INTERNAL_SERVER_ERROR),
    PRODUCT_UPDATE_FAILED("ERR-PRD-010", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus httpStatus;
} 