package com.alier.ecommercewebcore.rest.filter;

import com.alier.ecommercecore.common.logging.LoggingContextFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Configuration for registering the LoggingContextFilter with Spring Boot.
 * This ensures the filter is available in web applications that use this module.
 */
@Configuration
public class LoggingFilterConfiguration {

    /**
     * Creates and registers the LoggingContextFilter as a servlet filter.
     * 
     * @return the filter registration bean
     */
    @Bean
    public FilterRegistrationBean<Filter> loggingContextFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LoggingContextFilter());
        registration.addUrlPatterns("/*");
        registration.setName("loggingContextFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
} 