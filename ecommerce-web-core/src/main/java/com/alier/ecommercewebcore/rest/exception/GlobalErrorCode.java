package com.alier.ecommercewebcore.rest.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum GlobalErrorCode implements HttpErrorCode {
    // General errors
    UNEXPECTED_ERROR("ERR-GEN-001",HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("ERR-GEN-002",HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("ERR-GEN-003", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("ERR-GEN-004", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND("ERR-GEN-005", HttpStatus.NOT_FOUND);

    private final String code;
    private final HttpStatus httpStatus;
}