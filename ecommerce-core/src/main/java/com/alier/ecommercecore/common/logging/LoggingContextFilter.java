package com.alier.ecommercecore.common.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that manages trace ID and request ID context for logging and tracking.
 * This filter extracts the trace ID and request ID from incoming requests or generates
 * new ones if not present, then places them in both the MDC context for logging and the
 * response headers for propagation.
 */
@Slf4j
@Component
public class LoggingContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {

            try {
                // Handle trace ID for distributed tracing
                String traceId = extractOrGenerateId(httpRequest,
                        LoggingConstants.TRACE_ID_HEADER,
                        LoggingConstants.DEFAULT_TRACE_ID_PREFIX);
                MDC.put(LoggingConstants.TRACE_ID_MDC_KEY, traceId);
                httpResponse.setHeader(LoggingConstants.TRACE_ID_HEADER, traceId);

                // Handle request ID for request tracking
                String requestId = extractOrGenerateId(httpRequest,
                        LoggingConstants.REQUEST_ID_HEADER,
                        LoggingConstants.DEFAULT_REQUEST_ID_PREFIX);
                MDC.put(LoggingConstants.REQUEST_ID_MDC_KEY, requestId);
                httpResponse.setHeader(LoggingConstants.REQUEST_ID_HEADER, requestId);

                // Make IDs available as request attributes
                httpRequest.setAttribute(LoggingConstants.TRACE_ID_MDC_KEY, traceId);
                httpRequest.setAttribute(LoggingConstants.REQUEST_ID_MDC_KEY, requestId);

                log.debug("Processing request with traceId: {}, requestId: {}", traceId, requestId);
                chain.doFilter(request, response);
            } finally {
                MDC.remove(LoggingConstants.TRACE_ID_MDC_KEY);
                MDC.remove(LoggingConstants.REQUEST_ID_MDC_KEY);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * Extracts an ID from the request headers or generates a new one if not present.
     *
     * @param request    the HTTP request
     * @param headerName the name of the header to extract
     * @param prefix     the prefix to use for generated IDs
     * @return the ID string
     */
    private String extractOrGenerateId(HttpServletRequest request, String headerName, String prefix) {
        String id = request.getHeader(headerName);

        if (id == null || id.isBlank()) {
            id = prefix + UUID.randomUUID().toString();
            log.debug("No {} found in request. Generated: {}", headerName, id);
        } else {
            log.debug("Using {} from request: {}", headerName, id);
        }

        return id;
    }
} 