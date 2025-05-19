package com.alier.ecommercewebcore.infrastructure.config;

import com.alier.ecommercecore.common.logging.CorrelationContextExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Spring async configuration that preserves correlation context.
 * This ensures that async operations carry the same trace/request IDs
 * as the thread that initiated them.
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Configures the default async executor to preserve correlation context.
     *
     * @return The correlation-aware task executor
     */
    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("app-async-");
        executor.initialize();
        
        // Wrap the executor to preserve correlation context across async boundaries
        return CorrelationContextExecutor.wrapExecutor(executor);
    }
    
    /**
     * Creates a dedicated executor for longer running tasks.
     *
     * @return The correlation-aware long-running task executor
     */
    @Bean(name = "longRunningTaskExecutor")
    public Executor longRunningTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("long-task-");
        executor.initialize();
        
        // Wrap the executor to preserve correlation context across async boundaries
        return CorrelationContextExecutor.wrapExecutor(executor);
    }
} 