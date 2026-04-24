package com.electiq.backend.service;

import com.electiq.backend.dto.AssistantRequest;
import com.electiq.backend.dto.AssistantResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service responsible for handling AI assistant queries.
 *
 * <p>Routing logic:
 * <ol>
 *   <li>Rejects non-election-related questions immediately.</li>
 *   <li>Returns static responses for common queries (registration, timelines, etc.).</li>
 *   <li>Falls back to Gemini API only for complex election questions.</li>
 * </ol>
 */
@Service
public class AssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AssistantService.class);

    private static final String GEMINI_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private static final String SYSTEM_PROMPT =
            "You're ElectIQ, an election assistant. Answer only election questions in clear sentences under 60 words. " +
            "If unrelated, refuse. Don't hallucinate dates. If unknown, advise checking official updates. User: ";

    private static final int MAX_OUTPUT_TOKENS = 150;
    private static final double TEMPERATURE = 0.2;

    private static final String TIMELINE_RESPONSE =
            "Official schedule has not been announced yet. Please check Election Commission updates.";

    private static final List<String> ELECTION_KEYWORDS = Arrays.asList(
            "vote", "voting", "election", "poll", "ballot", "candidate",
            "nota", "democracy", "voter", "campaign", "politics",
            "government", "parliament", "assembly", "president", "minister", "mayor", "mla", "mp"
    );

    private static final List<String> TIMELINE_TRIGGERS = Arrays.asList(
            "next election", "upcoming election", "election date",
            "polling date", "schedule", "when is election"
    );

    private static final List<String> INDIAN_STATES = Arrays.asList(
            "andhra", "arunachal", "assam", "bihar", "chhattisgarh", "goa", "gujarat",
            "haryana", "himachal", "jharkhand", "karnataka", "kerala", "madhya pradesh",
            "maharashtra", "manipur", "meghalaya", "mizoram", "nagaland", "odisha",
            "punjab", "rajasthan", "sikkim", "tamil nadu", "telangana", "tripura",
            "uttar pradesh", "uttarakhand", "west bengal", "delhi", "kashmir", "jammu"
    );

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;
    
    // Efficiency: Shared maps for rate limiting and response caching
    private final Map<String, AtomicInteger> rateLimitMap = new ConcurrentHashMap<>();
    private final Map<String, String> responseCache = new ConcurrentHashMap<>();
    private long lastCleanup = System.currentTimeMillis();

    public AssistantService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AssistantResponse askQuestion(AssistantRequest request) {
        // Efficiency: Cleanup stale maps every minute
        cleanMaps();
        
        int count = rateLimitMap.computeIfAbsent("global", k -> new AtomicInteger(0)).incrementAndGet();
        if (count > 30) { 
            return new AssistantResponse("Too many requests. Please wait a moment.");
        }

        if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            return new AssistantResponse("Please provide a question.");
        }

        // Cost Opt: Normalize query (trim/lowercase)
        String query = request.getQuestion().trim().toLowerCase();

        // Speed: Check cache first
        if (responseCache.containsKey(query)) {
            return new AssistantResponse(responseCache.get(query));
        }

        if (!isElectionRelated(query)) {
            return new AssistantResponse("I can only assist with election-related topics.");
        }

        // Efficiency: Check static responses before calling LLM
        String staticResponse = getStaticResponse(query);
        if (staticResponse != null) {
            responseCache.put(query, staticResponse);
            return new AssistantResponse(staticResponse);
        }

        String aiAnswer = callGeminiApi(request.getQuestion().trim());
        responseCache.put(query, aiAnswer);
        return new AssistantResponse(aiAnswer);
    }

    // -------------------------------------------------------------------------
    // Intent Detection
    // -------------------------------------------------------------------------

    private boolean isElectionRelated(String query) {
        return ELECTION_KEYWORDS.stream().anyMatch(query::contains);
    }

    private boolean hasTimelineIntent(String query) {
        if (TIMELINE_TRIGGERS.stream().anyMatch(query::contains)) {
            return true;
        }
        // Treat "[state name] + election" as a timeline query
        return query.contains("election") && INDIAN_STATES.stream().anyMatch(query::contains);
    }

    // -------------------------------------------------------------------------
    // Static Response Routing
    // -------------------------------------------------------------------------

    private String getStaticResponse(String query) {
        if (hasTimelineIntent(query)) {
            return TIMELINE_RESPONSE;
        }
        if (query.contains("register") || query.contains("registration")) {
            return "You can register to vote online through the official Election Commission portal " +
                   "or offline by submitting the required forms to your Electoral Registration Officer.";
        }
        if (query.contains("how to vote") || query.contains("process of voting")) {
            return "To vote, verify your name on the electoral roll. On election day, go to your designated " +
                   "polling booth with valid ID, press the button next to your chosen candidate on the EVM, " +
                   "and listen for the beep.";
        }
        if (query.contains("voter id") || query.contains("id proof")) {
            return "Valid ID proofs for voting include Voter ID (EPIC), Aadhar card, Passport, Driving License, " +
                   "PAN card, and other officially recognized government IDs.";
        }
        if (query.contains("polling booth") || query.contains("polling station") || query.contains("where to vote")) {
            return "You can find your polling booth on the official Election Commission website or app " +
                   "by entering your Voter ID (EPIC) number.";
        }
        if (query.contains("nota")) {
            return "NOTA stands for 'None Of The Above'. It allows voters to reject all candidates in their " +
                   "constituency while still exercising their democratic right to vote.";
        }
        if (query.contains("election types") || query.contains("types of election")) {
            return "Main election types include General Elections (Lok Sabha), State Assembly Elections, " +
                   "and Local Body Elections (such as Panchayats and Municipalities).";
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Gemini API Integration
    // -------------------------------------------------------------------------

    private String callGeminiApi(String query) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            return "I am currently unable to answer complex questions because the API key is not configured.";
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = buildGeminiRequestBody(query);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    GEMINI_ENDPOINT + geminiApiKey, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);

            return extractGeminiText(response.getBody());

        } catch (HttpStatusCodeException e) {
            logger.error("Gemini API HTTP Error: {} — Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "AI assistant has reached its capacity or is temporarily unavailable. Please try again later.";
        } catch (Exception e) {
            logger.error("Gemini API Error: {}", e.getMessage());
            return "AI assistant is temporarily unavailable due to a network error. Please try again later.";
        }
    }

    private Map<String, Object> buildGeminiRequestBody(String query) {
        Map<String, Object> part = new HashMap<>();
        part.put("text", SYSTEM_PROMPT + query);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("maxOutputTokens", MAX_OUTPUT_TOKENS);
        generationConfig.put("temperature", TEMPERATURE);

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(content));
        body.put("generationConfig", generationConfig);
        return body;
    }

    @SuppressWarnings("unchecked")
    private String extractGeminiText(Map<?, ?> responseBody) {
        if (responseBody == null || !responseBody.containsKey("candidates")) {
            return "AI assistant is temporarily unavailable. Please try again later.";
        }
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            return "AI assistant is temporarily unavailable. Please try again later.";
        }
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null || !content.containsKey("parts")) {
            return "AI assistant is temporarily unavailable. Please try again later.";
        }
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            return "AI assistant is temporarily unavailable. Please try again later.";
        }
        String text = (String) parts.get(0).get("text");
        return text != null ? text.trim() : "AI assistant is temporarily unavailable. Please try again later.";
    }

    private void cleanMaps() {
        if (System.currentTimeMillis() - lastCleanup > 60000) {
            rateLimitMap.clear();
            responseCache.clear();
            lastCleanup = System.currentTimeMillis();
        }
    }
}
