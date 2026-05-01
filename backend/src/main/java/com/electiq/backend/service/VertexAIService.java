package com.electiq.backend.service;

import com.electiq.backend.config.AppConstants;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Integrates with Google Cloud Vertex AI (Gemini) to generate election-domain responses.
 * <p>
 * This service leverages a singleton {@link GenerativeModel} initialized at startup,
 * avoiding the high overhead of establishing new gRPC channels per request.
 * <p>
 * Authentication is managed via Application Default Credentials (ADC).
 */
@Service
public class VertexAIService {

    private static final Logger logger = LoggerFactory.getLogger(VertexAIService.class);

    private final GenerativeModel model;

    /**
     * Initializes the service with a pre-configured generative model.
     *
     * @param model the singleton model bean from VertexAIConfig.
     */
    public VertexAIService(GenerativeModel model) {
        this.model = model;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Sends {@code prompt} to the Gemini model and returns the generated text.
     *
     * @param prompt complete prompt string (system context + user query); must not be {@code null}
     * @return trimmed text response from the model, or a user-safe fallback message on failure
     */
    public String generateResponse(String prompt) {
        if (model == null) {
            logger.error("Vertex AI model is not initialized. Please check your GOOGLE_CLOUD_PROJECT configuration.");
            return AppConstants.MSG_AI_UNCONFIGURED;
        }

        try {
            logger.info("Sending request to Vertex AI singleton model...");

            GenerateContentResponse resp = model.generateContent(prompt);

            String text = ResponseHandler.getText(resp);
            logger.info("Response received from Vertex AI");

            return (text != null && !text.isBlank())
                    ? text.trim()
                    : AppConstants.MSG_AI_UNAVAILABLE;

        } catch (Exception ex) {
            logger.error("Vertex AI request failed: {}", ex.getMessage());
            return AppConstants.MSG_AI_UNAVAILABLE;
        }
    }
}
