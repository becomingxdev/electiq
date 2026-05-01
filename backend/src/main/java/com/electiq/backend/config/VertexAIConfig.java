package com.electiq.backend.config;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.electiq.backend.config.AppConstants.*;

/**
 * Configuration class for Google Cloud Vertex AI infrastructure.
 * <p>
 * This class establishes the Vertex AI client and Generative Model as
 * Spring beans, ensuring they are initialized once as singletons during
 * application startup.
 */
@Configuration
public class VertexAIConfig {

    private static final Logger logger = LoggerFactory.getLogger(VertexAIConfig.class);

    @Value("${" + ENV_GOOGLE_CLOUD_PROJECT + ":}")
    private String projectId;

    @Value("${" + ENV_GOOGLE_CLOUD_LOCATION + ":" + VERTEX_DEFAULT_LOCATION + "}")
    private String location;

    /**
     * Initializes the Vertex AI client.
     * <p>
     * Note: The Vertex AI client manages underlying gRPC channels.
     * Reusing this client avoids the high overhead of establishing
     * connections on every request.
     *
     * @return a singleton {@link VertexAI} client.
     */
    @Bean(destroyMethod = "close")
    public VertexAI vertexAI() {
        if (projectId == null || projectId.isBlank()) {
            logger.warn("Google Cloud Project ID is not configured. Vertex AI features will be unavailable.");
            return null;
        }

        logger.info("Initializing Vertex AI singleton for project: {} in location: {}", projectId, location);
        VertexAI client = new VertexAI(projectId, location);
        logger.info("Vertex AI initialized once (singleton)");
        return client;
    }

    /**
     * Initializes the Generative Model (Gemini).
     *
     * @param vertexAI the injected singleton Vertex AI client.
     * @return a singleton {@link GenerativeModel}.
     */
    @Bean
    public GenerativeModel generativeModel(VertexAI vertexAI) {
        if (vertexAI == null) {
            return null;
        }
        return new GenerativeModel(VERTEX_MODEL_NAME, vertexAI);
    }
}
