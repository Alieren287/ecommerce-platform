package com.alier.ecommercecore.common.logging;

/**
 * Constants related to logging infrastructure.
 */
public final class LoggingConstants {

    /**
     * Header name for trace ID propagation.
     */
    public static final String TRACE_ID_HEADER = "X-Trace-ID";

    /**
     * MDC key for storing trace ID.
     */
    public static final String TRACE_ID_MDC_KEY = "traceId";

    /**
     * Default trace ID value when none is provided.
     */
    public static final String DEFAULT_TRACE_ID_PREFIX = "gen-";

    /**
     * Header name for request ID propagation.
     */
    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    /**
     * MDC key for storing request ID.
     */
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    /**
     * Default request ID value when none is provided.
     */
    public static final String DEFAULT_REQUEST_ID_PREFIX = "req-";

    private LoggingConstants() {
        // Utility class, no instantiation
    }
} 