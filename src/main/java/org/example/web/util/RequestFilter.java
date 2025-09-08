package org.example.web.util;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestFilter implements Filter {
    private static final String HEADER = "X-Request-ID";
    private static final String MDC_KEY = "requestId";


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        String requestId = httpRequest.getHeader(HEADER);

        if (requestId == null || requestId.isBlank()) {
            requestId = java.util.UUID.randomUUID().toString();
        }

        org.slf4j.MDC.put(MDC_KEY, requestId);

        try {
            httpResponse.setHeader(HEADER, requestId);

            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            org.slf4j.MDC.remove(MDC_KEY);
        }
    }
}
