package com.alier.ecommercewebcore.rest.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum GlobalErrorCode implements HttpErrorCode {
    // General errors
    UNEXPECTED_ERROR("ERR-GEN-001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("ERR-GEN-002", "Validation error occurred", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("ERR-GEN-003", "Unauthorized access", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("ERR-GEN-004", "Forbidden access", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND("ERR-GEN-005", "Resource not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}