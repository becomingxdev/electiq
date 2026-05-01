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
 *
 * <p>The expected key is read from the {@code API_KEY} environment variable.
 * Requests must supply a matching value in the {@value AppConstants#API_KEY_HEADER} header.
 *
 * <h2>Exemptions</h2>
 * <ul>
 *   <li>{@value AppConstants#HEALTH_PATH} — system liveness probe</li>
 *   <li>{@value AppConstants#ACTUATOR_PATH} — Spring Boot actuator endpoints</li>
 *   <li>{@value AppConstants#SWAGGER_UI_PATH} — interactive API documentation</li>
 *   <li>{@value AppConstants#API_DOCS_PATH} — raw OpenAPI specification</li>
 * </ul>
 *
 * <p>If the {@code API_KEY} variable is not set, <em>all</em> requests are rejected
 * with {@code 401 Unauthorized} to prevent accidental open access in production.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyFilter.class);

    @Value("${" + AppConstants.ENV_API_KEY + ":}")
    private String configuredApiKey;

    // -------------------------------------------------------------------------
    // Filter implementation
    // -------------------------------------------------------------------------

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

        if (!isValidKey(providedKey)) {
            logger.warn("Unauthorised request blocked — path: {}", path);
            writeUnauthorisedResponse(response);
            return;
        }

        chain.doFilter(request, response);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the path is excluded from API key enforcement.
     */
    private boolean isExemptPath(String path) {
        return path.equals(AppConstants.HEALTH_PATH)
                || path.startsWith(AppConstants.ACTUATOR_PATH)
                || path.startsWith(AppConstants.SWAGGER_UI_PATH)
                || path.startsWith(AppConstants.API_DOCS_PATH);
    }

    /**
     * Returns {@code true} if the provided key is non-null and matches the configured value.
     * A missing or blank {@code configuredApiKey} is treated as misconfigured and returns {@code false}.
     */
    private boolean isValidKey(String providedKey) {
        return providedKey != null
                && !configuredApiKey.isBlank()
                && configuredApiKey.equals(providedKey);
    }

    /**
     * Writes a structured {@code 401 Unauthorized} JSON body to the response.
     */
    private void writeUnauthorisedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + AppConstants.MSG_UNAUTHORIZED + "\"}");
    }
}
