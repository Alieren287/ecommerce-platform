package com.alier.ecommercecore.common.messaging;

import com.alier.ecommercecore.common.logging.CorrelationContext;
import com.alier.ecommercecore.common.logging.CorrelationIds;
import com.alier.ecommercecore.common.logging.CorrelationMDCBridge;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Function;

/**
 * Adapter for managing correlation IDs in messaging systems like Kafka, RabbitMQ, etc.
 * This is framework-agnostic and can be used in any module.
 */
@Slf4j
public final class MessageCorrelationAdapter {

    private MessageCorrelationAdapter() {
        // Utility class, no instantiation
    }

    /**
     * Extracts correlation IDs from message headers and executes the given function.
     * This method is designed to be used in message consumers.
     *
     * @param headers       Function to extract header value by name
     * @param messageAction Function to process the message
     * @param <T>           The type of the message
     * @param <R>           The return type
     * @return The result of the message action
     */
    public static <T, R> R processMessageWithCorrelation(
            Function<String, String> headers,
            Function<T, R> messageAction,
            T message) {

        Map<String, String> previousContext = CorrelationContext.captureContext();
        try {
            setupCorrelationIds(headers);
            return messageAction.apply(message);
        } finally {
            CorrelationContext.restoreContext(previousContext);
            CorrelationMDCBridge.clearAll();
        }
    }

    /**
     * Sets up correlation IDs from message headers.
     *
     * @param headers Function to extract header value by name
     */
    private static void setupCorrelationIds(Function<String, String> headers) {
        // Extract trace ID from header or generate a new one
        String traceId = extractHeaderOrGenerate(
                headers,
                CorrelationIds.TRACE_ID_HEADER,
                CorrelationIds.DEFAULT_TRACE_ID_PREFIX);
        CorrelationMDCBridge.putCorrelationId(CorrelationIds.TRACE_ID, traceId);

        // For message processing, we want a new request ID for each message
        String requestId = CorrelationContext.generateRequestId();
        CorrelationMDCBridge.putCorrelationId(CorrelationIds.REQUEST_ID, requestId);

        // Extract additional correlation IDs
        extractAndSetOptional(headers, CorrelationIds.TENANT_ID_HEADER, CorrelationIds.TENANT_ID);
        extractAndSetOptional(headers, CorrelationIds.USER_ID_HEADER, CorrelationIds.USER_ID);

        log.debug("Processing message with traceId: {}, requestId: {}", traceId, requestId);
    }

    /**
     * Extracts an optional header and sets it in the correlation context.
     *
     * @param headers    Function to extract header value by name
     * @param headerName The header name to extract
     * @param contextKey The context key to set
     */
    private static void extractAndSetOptional(Function<String, String> headers, String headerName, String contextKey) {
        String value = headers.apply(headerName);
        if (value != null && !value.isBlank()) {
            CorrelationMDCBridge.putCorrelationId(contextKey, value);
            log.debug("Found {} in message header: {}", headerName, value);
        }
    }

    /**
     * Extracts a header value or generates a new value if not present.
     *
     * @param headers    Function to extract header value by name
     * @param headerName The header name to extract
     * @param prefix     The prefix to use for generated values
     * @return The extracted or generated value
     */
    private static String extractHeaderOrGenerate(Function<String, String> headers, String headerName, String prefix) {
        String value = headers.apply(headerName);
        if (value == null || value.isBlank()) {
            value = CorrelationContext.generateTraceId(prefix);
            log.debug("No {} found in message header. Generated: {}", headerName, value);
        } else {
            log.debug("Using {} from message header: {}", headerName, value);
        }
        return value;
    }

    /**
     * Prepares correlation IDs for a new outgoing message.
     * This method is designed to be used in message producers.
     *
     * @param headerSetter Function to set a header value
     */
    public static void prepareOutgoingMessageHeaders(BiConsumer<String, String> headerSetter) {
        // Propagate existing trace ID if available, otherwise generate a new one
        String traceId = CorrelationContext.getTraceId();

        // Set trace ID in the outgoing message
        headerSetter.accept(CorrelationIds.TRACE_ID_HEADER, traceId);

        // Propagate other correlation IDs if available
        propagateIfPresent(headerSetter, CorrelationIds.TENANT_ID, CorrelationIds.TENANT_ID_HEADER);
        propagateIfPresent(headerSetter, CorrelationIds.USER_ID, CorrelationIds.USER_ID_HEADER);

        log.debug("Prepared outgoing message with traceId: {}", traceId);
    }

    /**
     * Propagates a correlation ID to message headers if present.
     *
     * @param headerSetter Function to set a header value
     * @param contextKey   The context key to get
     * @param headerName   The header name to set
     */
    private static void propagateIfPresent(BiConsumer<String, String> headerSetter, String contextKey, String headerName) {
        String value = CorrelationContext.get(contextKey);
        if (value != null) {
            headerSetter.accept(headerName, value);
            log.debug("Propagating {} to outgoing message: {}", headerName, value);
        }
    }

    /**
     * Functional interface for setting headers.
     *
     * @param <T> The type of the first argument
     * @param <U> The type of the second argument
     */
    @FunctionalInterface
    public interface BiConsumer<T, U> {
        void accept(T t, U u);
    }
} 