package com.electiq.backend.security;

import com.electiq.backend.config.AppConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that enforces API key authentication on all non-exempt endpoints.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyFilter.class);

    @Value("${" + AppConstants.ENV_API_KEY + ":}")
    private String configuredApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isExemptPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        String providedKey = request.getHeader(AppConstants.API_KEY_HEADER);

        if (providedKey == null) {
            logger.debug("Received API key: missing");
        } else {
            logger.debug("Received API key: present");
        }

        if (!isValidKey(providedKey)) {
            logger.warn("API key invalid");
            writeUnauthorisedResponse(response);
            return;
        }

        logger.debug("API key valid");
        chain.doFilter(request, response);
    }

    private boolean isExemptPath(String path) {
        return path.equals(AppConstants.HEALTH_PATH)
                || path.startsWith(AppConstants.ACTUATOR_PATH)
                || path.startsWith(AppConstants.SWAGGER_UI_PATH)
                || path.startsWith(AppConstants.API_DOCS_PATH);
    }

    private boolean isValidKey(String providedKey) {
        if (providedKey == null || configuredApiKey == null || configuredApiKey.isBlank()) {
            return false;
        }
        return configuredApiKey.equals(providedKey);
    }

    private void writeUnauthorisedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + AppConstants.MSG_UNAUTHORIZED + "\"}");
    }
}
