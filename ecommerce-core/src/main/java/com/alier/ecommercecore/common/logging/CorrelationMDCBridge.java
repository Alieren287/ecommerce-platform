package com.alier.ecommercecore.common.logging;

import org.slf4j.MDC;

import java.util.Map;

/**
 * Bridge between CorrelationContext and SLF4J MDC.
 * This class synchronizes values between the two contexts.
 */
public final class CorrelationMDCBridge {

    private CorrelationMDCBridge() {
        // Utility class, no instantiation
    }

    /**
     * Sets a value in both CorrelationContext and MDC.
     *
     * @param key   The key
     * @param value The value
     */
    public static void putCorrelationId(String key, String value) {
        CorrelationContext.put(key, value);
        MDC.put(key, value);
    }

    /**
     * Removes a value from both CorrelationContext and MDC.
     *
     * @param key The key
     */
    public static void removeCorrelationId(String key) {
        CorrelationContext.put(key, null);
        MDC.remove(key);
    }

    /**
     * Copies all values from CorrelationContext to MDC.
     */
    public static void copyToMDC() {
        Map<String, String> contextMap = CorrelationContext.getAll();
        for (Map.Entry<String, String> entry : contextMap.entrySet()) {
            if (entry.getValue() != null) {
                MDC.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Copies all values from MDC to CorrelationContext.
     * Note: This is not a complete operation as MDC doesn't provide a way to get all values.
     * It only copies specific correlation IDs.
     */
    public static void copyFromMDC() {
        copyIfPresent(CorrelationIds.TRACE_ID);
        copyIfPresent(CorrelationIds.REQUEST_ID);
        copyIfPresent(CorrelationIds.TENANT_ID);
        copyIfPresent(CorrelationIds.USER_ID);
    }

    private static void copyIfPresent(String key) {
        String value = MDC.get(key);
        if (value != null) {
            CorrelationContext.put(key, value);
        }
    }

    /**
     * Clears all correlation IDs from both CorrelationContext and MDC.
     */
    public static void clearAll() {
        CorrelationContext.clear();
        MDC.clear();
    }
} 