package com.electiq.backend.service;

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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service to manage election timeline data.
 * Loads state-wise election dates from a JSON resource file.
 */
@Service
public class ElectionService {

    private static final Logger logger = LoggerFactory.getLogger(ElectionService.class);
    
    // Mapping common abbreviations to full state names
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
    private Map<String, String> elections = new HashMap<>();

    public ElectionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadElections() {
        try {
            InputStream inputStream = new ClassPathResource("data/elections.json").getInputStream();
            Map<String, String> rawData = objectMapper.readValue(inputStream, new TypeReference<Map<String, String>>() {});
            
            // Store keys in lowercase for case-insensitive lookup
            rawData.forEach((key, value) -> elections.put(key.toLowerCase().trim(), value));
            
            logger.info("Successfully loaded {} election dates from JSON", elections.size());
        } catch (Exception e) {
            logger.error("Failed to load elections.json: {}", e.getMessage());
        }
    }

    public ElectionTimelineResponse getTimeline(String state) {
        String normalized = normalizeState(state);
        
        if (normalized == null || normalized.isEmpty()) {
            return new ElectionTimelineResponse("Unknown", "N/A", "Please provide a state name.", "N/A");
        }

        if (!elections.containsKey(normalized)) {
            return new ElectionTimelineResponse(
                state,
                "N/A",
                "Sorry, I couldn't find election data for that state. Please try a full state name.",
                "N/A"
            );
        }

        String date = elections.get(normalized);

        return new ElectionTimelineResponse(
                capitalizeWords(normalized),
                "TBA",
                date,
                "TBA"
        );
    }

    private String normalizeState(String state) {
        if (state == null) return null;
        String raw = state.toLowerCase().trim();
        return STATE_ALIASES.getOrDefault(raw, raw);
    }

    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        return Arrays.stream(str.split("\\s+"))
                .map(t -> t.substring(0, 1).toUpperCase() + t.substring(1))
                .collect(Collectors.joining(" "));
    }
}
