package com.electiq.backend.service;

import com.electiq.backend.dto.AssistantRequest;
import com.electiq.backend.dto.AssistantResponse;
import com.electiq.backend.dto.ElectionTimelineResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service responsible for handling AI assistant queries.
 *
 * <p>Routing logic:
 * <ol>
 *   <li>Rejects non-election-related questions immediately.</li>
 *   <li>Returns static responses for common queries (registration, timelines, etc.).</li>
 *   <li>Falls back to Vertex AI (Gemini) only for complex election questions.</li>
 * </ol>
 */
@Service
public class AssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AssistantService.class);

    private static final String SYSTEM_PROMPT =
            "You're ElectIQ, an election assistant. Answer only election questions in clear sentences under 60 words. " +
            "If unrelated, refuse. Don't hallucinate dates. If unknown, advise checking official updates. User: ";

    private static final String SOURCE_NOTE = "\n\nSource: Election dataset (based on publicly available schedules and estimates).";

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

    private final ElectionService electionService;
    private final VertexAIService vertexAIService;
    
    // Efficiency: Shared maps for rate limiting and response caching
    private final Map<String, AtomicInteger> rateLimitMap = new ConcurrentHashMap<>();
    private final Map<String, String> responseCache = new ConcurrentHashMap<>();
    private long lastCleanup = System.currentTimeMillis();

    public AssistantService(ElectionService electionService, VertexAIService vertexAIService) {
        this.electionService = electionService;
        this.vertexAIService = vertexAIService;
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

        // Fallback to Vertex AI for complex queries
        String aiAnswer = vertexAIService.generateResponse(SYSTEM_PROMPT + request.getQuestion().trim());
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
            return getTimelineResponse(query);
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

    private String getTimelineResponse(String query) {
        String detectedState = INDIAN_STATES.stream()
                .filter(query::contains)
                .findFirst()
                .orElse(null);

        if (detectedState == null) {
            return "Official schedule for the upcoming elections has not been fully announced yet. Please specify your state for more details.";
        }

        ElectionTimelineResponse timeline = electionService.getTimeline(detectedState);
        String stateName = capitalizeWords(detectedState);
        
        return String.format(
            "The next election in %s is expected around %s.%s",
            stateName, timeline.getPollingDate(), SOURCE_NOTE
        );
    }

    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        return Arrays.stream(str.split("\\s+"))
                .map(t -> t.substring(0, 1).toUpperCase() + t.substring(1))
                .collect(Collectors.joining(" "));
    }

    private void cleanMaps() {
        if (System.currentTimeMillis() - lastCleanup > 60000) {
            rateLimitMap.clear();
            responseCache.clear();
            lastCleanup = System.currentTimeMillis();
        }
    }
}
