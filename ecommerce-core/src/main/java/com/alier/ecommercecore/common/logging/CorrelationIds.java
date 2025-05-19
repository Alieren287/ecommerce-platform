package com.alier.ecommercecore.common.logging;

/**
 * Constants related to correlation IDs.
 * This class is framework-agnostic and can be used in any module.
 */
public final class CorrelationIds {

    /**
     * Header name for trace ID propagation.
     */
    public static final String TRACE_ID_HEADER = "X-Trace-ID";

    /**
     * Context key for storing trace ID.
     */
    public static final String TRACE_ID = "traceId";

    /**
     * Default trace ID value when none is provided.
     */
    public static final String DEFAULT_TRACE_ID_PREFIX = "gen-";

    /**
     * Header name for request ID propagation.
     */
    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    /**
     * Context key for storing request ID.
     */
    public static final String REQUEST_ID = "requestId";

    /**
     * MDC key for storing tenant ID.
     */
    public static final String TENANT_ID = "tenantId";

    /**
     * Header name for tenant ID propagation.
     */
    public static final String TENANT_ID_HEADER = "X-Tenant-ID";

    /**
     * MDC key for storing user ID.
     */
    public static final String USER_ID = "userId";

    /**
     * Header name for user ID propagation.
     */
    public static final String USER_ID_HEADER = "X-User-ID";

    private CorrelationIds() {
        // Utility class, no instantiation
    }
} 