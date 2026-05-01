package com.electiq.backend.config;

/**
 * Central constants class for the ElectIQ application.
 * <p>
 * Consolidates all magic strings, message literals, header names, and
 * environment variable keys in a single, auditable location. No business
 * logic should live here.
 */
public final class AppConstants {

    // -------------------------------------------------------------------------
    // Prevent instantiation
    // -------------------------------------------------------------------------
    private AppConstants() {}

    // -------------------------------------------------------------------------
    // Security
    // -------------------------------------------------------------------------

    /** HTTP header name used for API key authentication. */
    public static final String API_KEY_HEADER = "x-api-key";

    /** Paths excluded from API key validation. */
    public static final String HEALTH_PATH       = "/health";
    public static final String ACTUATOR_PATH     = "/actuator";
    public static final String SWAGGER_UI_PATH   = "/swagger-ui";
    public static final String API_DOCS_PATH     = "/v3/api-docs";

    // -------------------------------------------------------------------------
    // Environment variable keys
    // -------------------------------------------------------------------------

    public static final String ENV_GOOGLE_CLOUD_PROJECT  = "GOOGLE_CLOUD_PROJECT";
    public static final String ENV_GOOGLE_CLOUD_LOCATION = "GOOGLE_CLOUD_LOCATION";
    public static final String ENV_API_KEY               = "API_KEY";

    // -------------------------------------------------------------------------
    // AI Model
    // -------------------------------------------------------------------------

    public static final String VERTEX_MODEL_NAME     = "gemini-2.5-flash";
    public static final String VERTEX_DEFAULT_LOCATION = "asia-south1";

    // -------------------------------------------------------------------------
    // Cache
    // -------------------------------------------------------------------------

    /** Default time-to-live for cache entries, in milliseconds (5 minutes). */
    public static final long CACHE_TTL_MS = 5L * 60 * 1_000;

    // -------------------------------------------------------------------------
    // User-facing messages
    // -------------------------------------------------------------------------

    public static final String MSG_ONLY_ELECTION_TOPICS =
            "I can only assist with election-related topics.";

    public static final String MSG_SCHEDULE_NOT_ANNOUNCED =
            "The official schedule for the upcoming elections has not been fully announced yet. " +
            "Please specify your state for more details.";

    public static final String MSG_STATE_NOT_FOUND =
            "Sorry, I couldn't find election data for that state. Please try a full state name.";

    public static final String MSG_PROVIDE_QUESTION =
            "Please provide a question.";

    public static final String MSG_AI_UNCONFIGURED =
            "I'm sorry, my AI processing engine is currently unconfigured. " +
            "Please check the project settings.";

    public static final String MSG_AI_UNAVAILABLE =
            "I'm having trouble connecting to my AI engine. Please try again in a moment.";

    public static final String MSG_GENERIC_ERROR =
            "Something went wrong. Please try again.";

    public static final String MSG_UNAUTHORIZED =
            "Unauthorized: Invalid API key";

    // -------------------------------------------------------------------------
    // Data source
    // -------------------------------------------------------------------------

    public static final String ELECTIONS_JSON_PATH = "data/elections.json";

    public static final String SOURCE_NOTE =
            "\n\nSource: Election dataset (based on publicly available schedules and estimates).";

    // -------------------------------------------------------------------------
    // AI system prompt
    // -------------------------------------------------------------------------

    public static final String SYSTEM_PROMPT =
            "You're ElectIQ, an election assistant. Answer only election questions in clear sentences under 60 words. " +
            "If unrelated, refuse. Don't hallucinate dates. If unknown, advise checking official updates. User: ";
}
