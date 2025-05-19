package com.alier.ecommercecore.common.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Thread-local storage for correlation IDs and other context values.
 * This class is framework-agnostic and can be used in any module.
 */
public final class CorrelationContext {

    private static final ThreadLocal<Map<String, String>> contextMap = ThreadLocal.withInitial(HashMap::new);

    private CorrelationContext() {
        // Utility class, no instantiation
    }

    /**
     * Sets a value in the context.
     *
     * @param key   The key
     * @param value The value
     */
    public static void put(String key, String value) {
        contextMap.get().put(key, value);
    }

    /**
     * Gets a value from the context.
     *
     * @param key The key
     * @return The value, or null if not found
     */
    public static String get(String key) {
        return contextMap.get().get(key);
    }

    /**
     * Gets trace id from the context.
     *
     * @return The value, or generate if not found
     */
    public static String getTraceId() {
        String traceId = CorrelationContext.get(CorrelationIds.TRACE_ID);
        if (traceId == null) {
            traceId = CorrelationContext.generateTraceId(CorrelationIds.DEFAULT_TRACE_ID_PREFIX);
            CorrelationMDCBridge.putCorrelationId(CorrelationIds.TRACE_ID, traceId);
        }
        return traceId;
    }

    /**
     * Clears all values from the context.
     * Should be called at the end of each request/operation to prevent memory leaks.
     */
    public static void clear() {
        contextMap.get().clear();
    }

    /**
     * Gets all context values.
     *
     * @return A copy of all context values
     */
    public static Map<String, String> getAll() {
        return new HashMap<>(contextMap.get());
    }

    /**
     * Generates a new trace ID with the given prefix.
     *
     * @param prefix The prefix to use
     * @return A new trace ID
     */
    public static String generateTraceId(String prefix) {
        return prefix + UUID.randomUUID().toString();
    }

    /**
     * Generates a new request ID with the given prefix.
     *
     * @return A new request ID
     */
    public static String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Creates a context snapshot that can be restored later.
     * Useful for async operations that cross thread boundaries.
     *
     * @return A snapshot of the current context
     */
    public static Map<String, String> captureContext() {
        return new HashMap<>(contextMap.get());
    }

    /**
     * Restores a context from a snapshot.
     *
     * @param contextSnapshot The snapshot to restore
     */
    public static void restoreContext(Map<String, String> contextSnapshot) {
        if (contextSnapshot == null) {
            clear();
            return;
        }

        Map<String, String> currentContext = contextMap.get();
        currentContext.clear();
        currentContext.putAll(contextSnapshot);
    }
} 