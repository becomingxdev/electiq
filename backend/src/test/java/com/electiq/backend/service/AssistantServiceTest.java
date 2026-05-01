package com.electiq.backend.service;

import com.electiq.backend.cache.CacheService;
import com.electiq.backend.dto.AssistantRequest;
import com.electiq.backend.dto.AssistantResponse;
import com.electiq.backend.dto.ElectionTimelineResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AssistantService using Mockito.
 * Validates query routing, cache integration, and intent detection.
 */
class AssistantServiceTest {

    @Mock
    private ElectionService electionService;

    @Mock
    private VertexAIService vertexAIService;

    @Mock
    private CacheService cacheService;

    private AssistantService assistantService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        assistantService = new AssistantService(electionService, vertexAIService, cacheService);
    }

    @Test
    void testCacheHitBehavior() {
        // Arrange
        String query = "when is the election in up?";
        String normalizedQuery = "when is the election in up?";
        String cachedAnswer = "The cached election date is 2027.";
        
        when(cacheService.get(normalizedQuery)).thenReturn(cachedAnswer);

        AssistantRequest request = new AssistantRequest();
        request.setQuestion(query);

        // Act
        AssistantResponse response = assistantService.askQuestion(request);

        // Assert
        assertEquals(cachedAnswer, response.getAnswer());
        verify(cacheService).get(normalizedQuery);
        // Verify no other services were called
        verifyNoInteractions(electionService, vertexAIService);
    }

    @Test
    void testElectionTimelineQueryDetection() {
        // Arrange
        String query = "election date in tamil nadu";
        String normalizedQuery = "election date in tamil nadu";
        
        when(cacheService.get(normalizedQuery)).thenReturn(null);
        when(electionService.getTimeline("tamil nadu"))
            .thenReturn(new ElectionTimelineResponse("Tamil Nadu", "TBA", "2026-04-06", "TBA"));

        AssistantRequest request = new AssistantRequest();
        request.setQuestion(query);

        // Act
        AssistantResponse response = assistantService.askQuestion(request);

        // Assert
        assertTrue(response.getAnswer().contains("2026-04-06"));
        verify(electionService).getTimeline("tamil nadu");
        verify(cacheService).set(eq(normalizedQuery), anyString());
    }

    @Test
    void testNonElectionQueryRefusal() {
        // Arrange
        String query = "what is the weather today?";
        String normalizedQuery = "what is the weather today?";
        
        when(cacheService.get(normalizedQuery)).thenReturn(null);

        AssistantRequest request = new AssistantRequest();
        request.setQuestion(query);

        // Act
        AssistantResponse response = assistantService.askQuestion(request);

        // Assert
        assertEquals("I can only assist with election-related topics.", response.getAnswer());
        verifyNoInteractions(vertexAIService, electionService);
    }

    @Test
    void testAIFallbackForComplexQueries() {
        // Arrange
        String query = "who are the main candidates in the election?";
        String normalizedQuery = "who are the main candidates in the election?";
        String aiGeneratedResponse = "Candidates vary by constituency...";

        when(cacheService.get(normalizedQuery)).thenReturn(null);
        when(vertexAIService.generateResponse(anyString())).thenReturn(aiGeneratedResponse);

        AssistantRequest request = new AssistantRequest();
        request.setQuestion(query);

        // Act
        AssistantResponse response = assistantService.askQuestion(request);

        // Assert
        assertEquals(aiGeneratedResponse, response.getAnswer());
        verify(vertexAIService).generateResponse(anyString());
        verify(cacheService).set(normalizedQuery, aiGeneratedResponse);
    }
}
