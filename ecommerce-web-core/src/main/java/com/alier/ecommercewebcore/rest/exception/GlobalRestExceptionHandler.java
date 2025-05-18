package com.alier.ecommercewebcore.rest.exception;

import com.alier.ecommercecore.common.dto.BaseResponse;
import com.alier.ecommercecore.common.exception.BusinessException;
import com.alier.ecommercecore.common.logging.LoggingConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST controllers.
 * Provides consistent error responses for common exceptions.
 */
@Slf4j
@RestControllerAdvice
public class GlobalRestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Gets the current request ID from the request attributes.
     * This ID is set by the LoggingContextFilter.
     *
     * @return The current request ID
     */
    protected String getRequestId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        Object requestId = request.getAttribute(LoggingConstants.REQUEST_ID_MDC_KEY);
        return requestId != null ? requestId.toString() : null;
    }

    /**
     * Gets the current trace ID from the request attributes.
     * This ID is set by the LoggingContextFilter.
     *
     * @return The current trace ID
     */
    protected String getTraceId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        Object traceId = request.getAttribute(LoggingConstants.TRACE_ID_MDC_KEY);
        return traceId != null ? traceId.toString() : null;
    }

    /**
     * Gets the current HTTP request from the RequestContextHolder.
     *
     * @return The current HTTP request, or null if not in a request context
     */
    protected HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * Adds request and trace IDs to a BaseResponse.
     *
     * @param response The response to enhance with IDs
     * @param <T>      The type of data in the response
     * @return The response with IDs added
     */
    protected <T> BaseResponse<T> addIds(BaseResponse<T> response) {
        response.withIds(getRequestId(), getTraceId());
        return response;
    }

    /**
     * Handles constraint violation exceptions (validation errors).
     *
     * @param ex The exception
     * @return A response entity with error details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage(),
                        (error1, error2) -> error1 + ", " + error2
                ));

        log.warn("Validation error occurred, requestId: {}, traceId: {}, errors: {}",
                getRequestId(), getTraceId(), errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(addIds(BaseResponse.error("Validation Error", errors)));
    }

    /**
     * Handle general business logic exceptions.
     *
     * @param ex The exception
     * @return A response entity with error details
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("Business rule violation, requestId: {}, traceId: {}, errorCode: {}, message: {}",
                getRequestId(), getTraceId(), ex.getErrorCode().getCode(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(addIds(BaseResponse.error(ex.getMessage())));
    }

    /**
     * Handle general business logic exceptions.
     *
     * @param ex The exception
     * @return A response entity with error details
     */
    @ExceptionHandler(RestBusinessException.class)
    public ResponseEntity<BaseResponse<Void>> handleRestBusinessException(RestBusinessException ex) {
        log.warn("Rest business rule violation, requestId: {}, traceId: {}, errorCode: {}, message: {}",
                getRequestId(), getTraceId(), ex.getErrorCode().getCode(), ex.getMessage());

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(addIds(BaseResponse.error(ex.getMessage())));
    }

    /**
     * Handle generic exceptions.
     *
     * @param ex The exception
     * @return A response entity with error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGenericException(Exception ex) {
        // Log the actual exception for debugging (not exposed to client)
        log.error("Unhandled exception occurred with requestId: {}, traceId: {}",
                getRequestId(), getTraceId(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(addIds(BaseResponse.error("An unexpected error occurred")));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest webRequest) {

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                        (error1, error2) -> error1 + ", " + error2
                ));

        String requestId = null;
        String traceId = null;
        if (webRequest instanceof ServletWebRequest) {
            HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();
            requestId = (String) request.getAttribute(LoggingConstants.REQUEST_ID_MDC_KEY);
            traceId = (String) request.getAttribute(LoggingConstants.TRACE_ID_MDC_KEY);
        }

        log.warn("Method argument validation failed, requestId: {}, traceId: {}, errors: {}",
                requestId, traceId, errors);

        BaseResponse<Map<String, String>> errorResponse = BaseResponse.error("Validation Error", errors);
        if (requestId != null && traceId != null) {
            errorResponse.withIds(requestId, traceId);
        } else {
            if (requestId != null) {
                errorResponse.withRequestId(requestId);
            }
            if (traceId != null) {
                errorResponse.withTraceId(traceId);
            }
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
} 