package com.electiq.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.electiq.backend.config.AppConstants.API_KEY_HEADER;

/**
 * Configuration for OpenAPI documentation (SpringDoc/Swagger).
 */
@Configuration
public class SwaggerConfig {

    /**
     * Defines the OpenAPI metadata and security schemes.
     * <p>
     * Includes a global security requirement for the "ApiKey" scheme
     * so that all endpoints can be tested with the x-api-key header
     * directly from the Swagger UI.
     *
     * @return a configured {@link OpenAPI} instance.
     */
    @Bean
    public OpenAPI electiqOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Electiq API")
                        .description("AI-powered election assistant backend")
                        .version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList("ApiKey"))
                .components(new Components()
                        .addSecuritySchemes("ApiKey", new SecurityScheme()
                                .name(API_KEY_HEADER)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)));
    }
}
