package com.electiq.backend.service;

import com.electiq.backend.dto.ElectionTimelineResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ElectionService.
 * Validates data loading, normalization, and fallback logic.
 */
class ElectionServiceTest {

    private ElectionService electionService;

    @BeforeEach
    void setUp() {
        // ElectionService requires an ObjectMapper for initialization
        electionService = new ElectionService(new ObjectMapper());
        // Manually trigger PostConstruct logic for unit testing to load data from elections.json
        electionService.loadElections(); 
    }

    @Test
    void testValidStateReturnsCorrectDate() {
        ElectionTimelineResponse response = electionService.getTimeline("Uttar Pradesh");
        assertNotNull(response);
        assertEquals("2027-02-18", response.getPollingDate());
        assertEquals("Uttar Pradesh", response.getState());
    }

    @Test
    void testInvalidStateReturnsFallbackMessage() {
        ElectionTimelineResponse response = electionService.getTimeline("Mars State");
        assertNotNull(response);
        assertTrue(response.getPollingDate().contains("couldn't find election data"));
    }

    @Test
    void testNormalizationOfAliases() {
        // Test abbreviation "UP"
        ElectionTimelineResponse upResponse = electionService.getTimeline("UP");
        assertEquals("2027-02-18", upResponse.getPollingDate());

        // Test abbreviation "TN"
        ElectionTimelineResponse tnResponse = electionService.getTimeline("tn");
        assertEquals("2026-04-06", tnResponse.getPollingDate());
    }

    @Test
    void testCaseInsensitivity() {
        ElectionTimelineResponse response = electionService.getTimeline("kErAlA");
        assertEquals("2026-05-01", response.getPollingDate());
    }
}
