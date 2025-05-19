package com.alier.ecommercewebcore.rest.controller;

import com.alier.ecommercecore.common.dto.BaseResponse;
import com.alier.ecommercecore.common.dto.PaginatedResponse;
import com.alier.ecommercecore.common.logging.CorrelationContext;
import com.alier.ecommercecore.common.logging.CorrelationIds;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Base controller class that provides common functionality for all REST controllers.
 * Follows best practices for REST API responses and error handling.
 */
@CrossOrigin(origins = "*")
public abstract class BaseController {

    /**
     * Gets the current request ID from the correlation context.
     *
     * @return The current request ID
     */
    protected String getRequestId() {
        return CorrelationContext.get(CorrelationIds.REQUEST_ID);
    }

    /**
     * Gets the current trace ID from the correlation context.
     *
     * @return The current trace ID
     */
    protected String getTraceId() {
        return CorrelationContext.get(CorrelationIds.TRACE_ID);
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
     * Creates a success response with the given data.
     *
     * @param data The data to include in the response
     * @param <T>  The type of the data
     * @return ResponseEntity with status 200 OK and the data wrapped in BaseResponse
     */
    protected <T> ResponseEntity<BaseResponse<T>> success(T data) {
        return ResponseEntity.ok(addIds(BaseResponse.success(data)));
    }

    /**
     * Creates a success response with the given data and message.
     *
     * @param data    The data to include in the response
     * @param message A custom success message
     * @param <T>     The type of the data
     * @return ResponseEntity with status 200 OK and the data wrapped in BaseResponse
     */
    protected <T> ResponseEntity<BaseResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(addIds(BaseResponse.success(data, message)));
    }

    /**
     * Creates a success response with no data.
     *
     * @return ResponseEntity with status 200 OK and a success message
     */
    protected <T> ResponseEntity<BaseResponse<T>> success() {
        return ResponseEntity.ok(addIds(BaseResponse.success(null, "Operation completed successfully")));
    }

    /**
     * Creates a success response with a custom message.
     *
     * @param message A custom success message
     * @return ResponseEntity with status 200 OK and a success message
     */
    protected <T> ResponseEntity<BaseResponse<T>> success(String message) {
        return ResponseEntity.ok(addIds(BaseResponse.success(null, message)));
    }

    /**
     * Creates a paginated response.
     *
     * @param paginatedResponse The paginated response data
     * @param <T>               The type of data in the paginated response
     * @return ResponseEntity with status 200 OK and the paginated data
     */
    protected <T> ResponseEntity<BaseResponse<PaginatedResponse<T>>> paginated(PaginatedResponse<T> paginatedResponse) {
        return ResponseEntity.ok(addIds(BaseResponse.success(paginatedResponse)));
    }

    /**
     * Creates a created response with the given data.
     *
     * @param data The data to include in the response
     * @param <T>  The type of the data
     * @return ResponseEntity with status 201 CREATED and the data wrapped in BaseResponse
     */
    protected <T> ResponseEntity<BaseResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addIds(BaseResponse.success(data)));
    }

    /**
     * Creates a created response with the given data and message.
     *
     * @param data    The data to include in the response
     * @param message A custom success message
     * @param <T>     The type of the data
     * @return ResponseEntity with status 201 CREATED and the data wrapped in BaseResponse
     */
    protected <T> ResponseEntity<BaseResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addIds(BaseResponse.success(data, message)));
    }

    /**
     * Creates a no content response.
     *
     * @return ResponseEntity with status 204 NO_CONTENT
     */
    protected ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Creates a bad request response with an error message.
     *
     * @param message The error message
     * @return ResponseEntity with status 400 BAD_REQUEST and the error message
     */
    protected <T> ResponseEntity<BaseResponse<T>> badRequest(String message) {
        return ResponseEntity.badRequest().body(addIds(BaseResponse.<T>error(message)));
    }

    /**
     * Creates a not found response with an error message.
     *
     * @param message The error message
     * @return ResponseEntity with status 404 NOT_FOUND and the error message
     */
    protected <T> ResponseEntity<BaseResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(addIds(BaseResponse.error(message)));
    }
} 