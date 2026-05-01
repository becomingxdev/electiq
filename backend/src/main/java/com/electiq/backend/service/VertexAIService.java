package com.electiq.backend.service;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to interact with Google Cloud Vertex AI using Gemini models.
 * Configurable via environment variables for cross-environment compatibility.
 */
@Service
public class VertexAIService {

    private static final Logger logger = LoggerFactory.getLogger(VertexAIService.class);

    @Value("${GOOGLE_CLOUD_PROJECT:}")
    private String projectId;

    @Value("${GOOGLE_CLOUD_LOCATION:asia-south1}")
    private String location;

    private final String modelName = "gemini-2.5-flash";

    /**
     * Generates a text response for a given prompt using Gemini 1.5 Flash.
     * 
     * @param prompt The user input or system prompt to process.
     * @return The clean text response from the model, or a fallback message on
     *         failure.
     */
    public String generateResponse(String prompt) {
        if (projectId == null || projectId.isEmpty()) {
            logger.error("Vertex AI Error: GOOGLE_CLOUD_PROJECT environment variable is not set.");
            return "I'm sorry, my AI processing engine is currently unconfigured. Please check the project settings.";
        }

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            logger.info("Vertex AI initialized successfully for project: {} in {}", projectId, location);

            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            
            logger.info("Sending request to Vertex AI (Model: {})", modelName);
            GenerateContentResponse response = model.generateContent(prompt);
            logger.info("Received response from Vertex AI.");

            String text = ResponseHandler.getText(response);
            return text != null ? text.trim() : "I'm sorry, I couldn't generate a response at this time.";

        } catch (Exception e) {
            logger.error("Vertex AI Generation Failed: {}", e.getMessage());
            return "I'm having trouble connecting to my enterprise AI engine. Please try again in a moment.";
        }
    }
}
