package com.electiq.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to protect API endpoints using an API key provided in the 'x-api-key' header.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${API_KEY:}")
    private String apiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1. Exclude health checks from API key requirement
        if (path.equals("/health") || path.startsWith("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Validate API Key
        String requestKey = request.getHeader("x-api-key");

        if (apiKey == null || apiKey.isEmpty() || !apiKey.equals(requestKey)) {
            logger.warn("Unauthorized access attempt to " + path + " with invalid API key");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Unauthorized: Invalid API key\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
