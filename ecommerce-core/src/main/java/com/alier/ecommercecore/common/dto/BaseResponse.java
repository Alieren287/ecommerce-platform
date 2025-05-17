package com.alier.ecommercecore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    @Builder.Default
    private boolean success = true;
    private T data;
    private String message;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private String requestId;
    private String traceId;

    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .data(data)
                .message("Operation successful")
                .build();
    }

    public static <T> BaseResponse<T> success(T data, String message) {
        return BaseResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    public static <T> BaseResponse<T> error(String message) {
        return BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> BaseResponse<T> error(String message, T data) {
        return BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }

    public void withRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void withTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void withIds(String requestId, String traceId) {
        this.requestId = requestId;
        this.traceId = traceId;
    }
} 