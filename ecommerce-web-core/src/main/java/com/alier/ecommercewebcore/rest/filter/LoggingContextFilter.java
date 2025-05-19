package com.alier.ecommercewebcore.rest.filter;

import com.alier.ecommercecore.common.logging.CorrelationContext;
import com.alier.ecommercecore.common.logging.CorrelationIds;
import com.alier.ecommercecore.common.logging.CorrelationMDCBridge;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Servlet filter that manages correlation IDs for logging and tracking.
 * This filter extracts correlation IDs from incoming requests or generates
 * new ones if not present, then places them in both contexts and headers.
 */
@Slf4j
@Component
public class LoggingContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            try {
                setupCorrelationIds(httpRequest, httpResponse);
                chain.doFilter(request, response);
            } finally {
                CorrelationMDCBridge.clearAll();
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * Sets up correlation IDs from the request or generates new ones.
     *
     * @param request  The HTTP request
     * @param response The HTTP response
     */
    private void setupCorrelationIds(HttpServletRequest request, HttpServletResponse response) {
        // Handle trace ID
        String traceId = extractOrGenerateId(request, 
                CorrelationIds.TRACE_ID_HEADER, 
                CorrelationIds.DEFAULT_TRACE_ID_PREFIX);
        CorrelationMDCBridge.putCorrelationId(CorrelationIds.TRACE_ID, traceId);
        response.setHeader(CorrelationIds.TRACE_ID_HEADER, traceId);

        // Handle request ID
        String requestId = CorrelationContext.generateRequestId();
        CorrelationMDCBridge.putCorrelationId(CorrelationIds.REQUEST_ID, requestId);
        response.setHeader(CorrelationIds.REQUEST_ID_HEADER, requestId);

        // Make IDs available as request attributes (for legacy code)
        request.setAttribute(CorrelationIds.TRACE_ID, traceId);
        request.setAttribute(CorrelationIds.REQUEST_ID, requestId);

        // Handle other potential correlation IDs (tenant, user, etc.)
        extractAndSetOptionalHeader(request, CorrelationIds.TENANT_ID_HEADER, CorrelationIds.TENANT_ID);
        extractAndSetOptionalHeader(request, CorrelationIds.USER_ID_HEADER, CorrelationIds.USER_ID);

        log.debug("Processing request with traceId: {}, requestId: {}", traceId, requestId);
    }

    /**
     * Extracts an optional header and sets it in the correlation context if present.
     *
     * @param request The HTTP request
     * @param headerName The header name
     * @param contextKey The context key
     */
    private void extractAndSetOptionalHeader(HttpServletRequest request, String headerName, String contextKey) {
        String value = request.getHeader(headerName);
        if (value != null && !value.isBlank()) {
            CorrelationMDCBridge.putCorrelationId(contextKey, value);
            log.debug("Found {} in request: {}", headerName, value);
        }
    }

    /**
     * Extracts an ID from the request headers or generates a new one if not present.
     *
     * @param request    The HTTP request
     * @param headerName The name of the header to extract
     * @param prefix     The prefix to use for generated IDs
     * @return The ID string
     */
    private String extractOrGenerateId(HttpServletRequest request, String headerName, String prefix) {
        String id = request.getHeader(headerName);

        if (id == null || id.isBlank()) {
            id = CorrelationContext.generateTraceId(prefix);
            log.debug("No {} found in request. Generated: {}", headerName, id);
        } else {
            log.debug("Using {} from request: {}", headerName, id);
        }

        return id;
    }
} 