package com.alier.ecommercecore.common.logging;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Executor utilities that preserve the CorrelationContext across thread boundaries.
 * This is important for async operations where the thread that initiates an operation
 * is different from the thread that completes it.
 */
public final class CorrelationContextExecutor {
    
    private CorrelationContextExecutor() {
        // Utility class, no instantiation
    }
    
    /**
     * Creates a new Runnable that preserves the current CorrelationContext.
     *
     * @param runnable The original Runnable
     * @return A new Runnable that sets up the CorrelationContext before executing
     */
    public static Runnable wrap(Runnable runnable) {
        Map<String, String> contextSnapshot = CorrelationContext.captureContext();
        return () -> {
            Map<String, String> previousContext = CorrelationContext.captureContext();
            try {
                CorrelationContext.restoreContext(contextSnapshot);
                CorrelationMDCBridge.copyToMDC();
                runnable.run();
            } finally {
                CorrelationContext.restoreContext(previousContext);
                CorrelationMDCBridge.copyToMDC();
            }
        };
    }
    
    /**
     * Creates a new Callable that preserves the current CorrelationContext.
     *
     * @param callable The original Callable
     * @param <V>      The return type of the Callable
     * @return A new Callable that sets up the CorrelationContext before executing
     */
    public static <V> Callable<V> wrap(Callable<V> callable) {
        Map<String, String> contextSnapshot = CorrelationContext.captureContext();
        return () -> {
            Map<String, String> previousContext = CorrelationContext.captureContext();
            try {
                CorrelationContext.restoreContext(contextSnapshot);
                CorrelationMDCBridge.copyToMDC();
                return callable.call();
            } finally {
                CorrelationContext.restoreContext(previousContext);
                CorrelationMDCBridge.copyToMDC();
            }
        };
    }
    
    /**
     * Creates a new Supplier that preserves the current CorrelationContext.
     *
     * @param supplier The original Supplier
     * @param <T>      The return type of the Supplier
     * @return A new Supplier that sets up the CorrelationContext before executing
     */
    public static <T> Supplier<T> wrap(Supplier<T> supplier) {
        Map<String, String> contextSnapshot = CorrelationContext.captureContext();
        return () -> {
            Map<String, String> previousContext = CorrelationContext.captureContext();
            try {
                CorrelationContext.restoreContext(contextSnapshot);
                CorrelationMDCBridge.copyToMDC();
                return supplier.get();
            } finally {
                CorrelationContext.restoreContext(previousContext);
                CorrelationMDCBridge.copyToMDC();
            }
        };
    }
    
    /**
     * Creates a new CompletableFuture that preserves the current CorrelationContext.
     *
     * @param supplier The supplier that produces the CompletableFuture
     * @param <T>      The result type of the CompletableFuture
     * @return A new CompletableFuture that has the CorrelationContext propagated
     */
    public static <T> CompletableFuture<T> completableFuture(Supplier<CompletableFuture<T>> supplier) {
        return CompletableFuture.supplyAsync(wrap(supplier))
                .thenCompose(future -> future);
    }
    
    /**
     * Creates a new Executor that wraps all submitted tasks with the current CorrelationContext.
     *
     * @param delegate The underlying Executor to delegate to
     * @return A new Executor that preserves the CorrelationContext
     */
    public static Executor wrapExecutor(Executor delegate) {
        return task -> delegate.execute(wrap(task));
    }
} 