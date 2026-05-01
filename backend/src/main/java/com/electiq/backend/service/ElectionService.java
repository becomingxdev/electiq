package com.electiq.backend.service;

import com.electiq.backend.config.AppConstants;
import com.electiq.backend.dto.ElectionTimelineResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages election timeline data for Indian state assembly elections.
 *
 * <p>On startup, election dates are loaded from
 * {@value AppConstants#ELECTIONS_JSON_PATH} into an in-memory map. All state
 * lookups are case-insensitive and support common two-letter abbreviations
 * (e.g. "UP" → "uttar pradesh", "TN" → "tamil nadu") defined in
 * {@link #STATE_ALIASES}.
 */
@Service
public class ElectionService {

    private static final Logger logger = LoggerFactory.getLogger(ElectionService.class);

    /**
     * Known state abbreviations mapped to their canonical lowercase names.
     */
    private static final Map<String, String> STATE_ALIASES = Map.of(
            "up", "uttar pradesh",
            "tn", "tamil nadu",
            "mp", "madhya pradesh",
            "ap", "andhra pradesh",
            "wb", "west bengal",
            "hp", "himachal pradesh",
            "jk", "jammu kashmir"
    );

    private final ObjectMapper objectMapper;

    /** Populated at startup via {@link #loadElections()}. */
    private Map<String, String> electionDates = Collections.emptyMap();

    public ElectionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    /**
     * Loads election dates from the JSON data file on application startup.
     * All keys are normalised to lowercase for case-insensitive lookups.
     */
    @PostConstruct
    public void loadElections() {
        try {
            InputStream stream = new ClassPathResource(AppConstants.ELECTIONS_JSON_PATH).getInputStream();
            Map<String, String> raw = objectMapper.readValue(stream, new TypeReference<Map<String, String>>() {});

            Map<String, String> normalised = new HashMap<>(raw.size());
            raw.forEach((key, value) -> normalised.put(key.trim().toLowerCase(), value));
            this.electionDates = Collections.unmodifiableMap(normalised);

            logger.info("Loaded {} election entries from {}", electionDates.size(), AppConstants.ELECTIONS_JSON_PATH);
        } catch (Exception ex) {
            logger.error("Failed to load election data from {}: {}", AppConstants.ELECTIONS_JSON_PATH, ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the election timeline for the given state name or abbreviation.
     *
     * @param state raw state input (e.g. "UP", "Tamil Nadu", "maharashtra")
     * @return a {@link ElectionTimelineResponse} with polling date; never {@code null}
     */
    public ElectionTimelineResponse getTimeline(String state) {
        String normalised = normaliseState(state);

        if (normalised == null || normalised.isEmpty()) {
            return fallback("Unknown", AppConstants.MSG_PROVIDE_QUESTION);
        }

        String date = electionDates.get(normalised);
        if (date == null) {
            logger.warn("Election data not found for state: [{}]", normalised);
            return fallback(state, AppConstants.MSG_STATE_NOT_FOUND);
        }

        return new ElectionTimelineResponse(capitaliseWords(normalised), "TBA", date, "TBA");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Normalises a raw state name: trims, lowercases, and resolves abbreviations.
     *
     * @param state raw input; may be {@code null}
     * @return normalised state name, or {@code null} if input is null
     */
    private String normaliseState(String state) {
        if (state == null) return null;
        String cleaned = state.trim().toLowerCase();
        return STATE_ALIASES.getOrDefault(cleaned, cleaned);
    }

    /**
     * Builds a fallback response when a state is not found in the dataset.
     */
    private ElectionTimelineResponse fallback(String state, String message) {
        return new ElectionTimelineResponse(state, "N/A", message, "N/A");
    }

    /**
     * Capitalises the first letter of every word in a space-delimited string.
     *
     * @param str input string (expected lowercase)
     * @return title-cased string
     */
    private String capitaliseWords(String str) {
        if (str == null || str.isEmpty()) return str;
        return Arrays.stream(str.split("\\s+"))
                .map(token -> Character.toUpperCase(token.charAt(0)) + token.substring(1))
                .collect(Collectors.joining(" "));
    }
}
