package com.electiq.backend.service;

import com.electiq.backend.cache.CacheService;
import com.electiq.backend.config.AppConstants;
import com.electiq.backend.dto.AssistantRequest;
import com.electiq.backend.dto.AssistantResponse;
import com.electiq.backend.dto.ElectionTimelineResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates AI assistant responses for ElectIQ.
 *
 * <h2>Routing strategy (priority order)</h2>
 * <ol>
 *   <li>Return cached response immediately if available.</li>
 *   <li>Reject non-election-related questions.</li>
 *   <li>Return static responses for well-known query types.</li>
 *   <li>Fall back to Vertex AI (Gemini) for complex or open-ended election questions.</li>
 * </ol>
 *
 * <p>All inputs are normalised (trimmed, lowercased) before processing.
 * Results are stored in the cache for subsequent identical queries.
 */
@Service
public class AssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AssistantService.class);

    // -------------------------------------------------------------------------
    // Intent keyword lists
    // -------------------------------------------------------------------------

    private static final List<String> ELECTION_KEYWORDS = List.of(
            "vote", "voting", "election", "poll", "ballot", "candidate",
            "nota", "democracy", "voter", "campaign", "politics",
            "government", "parliament", "assembly", "president", "minister",
            "mayor", "mla", "mp"
    );

    private static final List<String> TIMELINE_TRIGGERS = List.of(
            "next election", "upcoming election", "election date",
            "polling date", "schedule", "when is election"
    );

    private static final List<String> INDIAN_STATES = List.of(
            "andhra", "arunachal", "assam", "bihar", "chhattisgarh", "goa", "gujarat",
            "haryana", "himachal", "jharkhand", "karnataka", "kerala", "madhya pradesh",
            "maharashtra", "manipur", "meghalaya", "mizoram", "nagaland", "odisha",
            "punjab", "rajasthan", "sikkim", "tamil nadu", "telangana", "tripura",
            "uttar pradesh", "uttarakhand", "west bengal", "delhi", "kashmir", "jammu"
    );

    // -------------------------------------------------------------------------
    // Dependencies (constructor-injected)
    // -------------------------------------------------------------------------

    private final ElectionService electionService;
    private final VertexAIService vertexAIService;
    private final CacheService    cacheService;

    public AssistantService(ElectionService electionService,
                            VertexAIService vertexAIService,
                            CacheService cacheService) {
        this.electionService = electionService;
        this.vertexAIService = vertexAIService;
        this.cacheService    = cacheService;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Processes an assistant request and returns an appropriate response.
     *
     * @param request the incoming assistant request; may be {@code null}
     * @return a structured {@link AssistantResponse} — never {@code null}
     */
    public AssistantResponse askQuestion(AssistantRequest request) {
        if (isBlank(request)) {
            return respond(AppConstants.MSG_PROVIDE_QUESTION);
        }

        String query = normalise(request.getQuestion());

        // 1. Cache-first lookup
        String cached = cacheService.get(query);
        if (cached != null) {
            logger.info("Cache hit for query");
            return respond(cached);
        }
        logger.info("Cache miss — processing query");

        // 2. Relevance filter
        if (!isElectionRelated(query)) {
            return respond(AppConstants.MSG_ONLY_ELECTION_TOPICS);
        }

        // 3. Static response routing
        String staticResponse = resolveStaticResponse(query);
        if (staticResponse != null) {
            cacheService.set(query, staticResponse);
            return respond(staticResponse);
        }

        // 4. Vertex AI fallback
        String aiResponse = vertexAIService.generateResponse(
                AppConstants.SYSTEM_PROMPT + request.getQuestion().trim()
        );
        cacheService.set(query, aiResponse);
        return respond(aiResponse);
    }

    // -------------------------------------------------------------------------
    // Input validation & normalisation
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} when the request or its question field is null/blank.
     */
    private boolean isBlank(AssistantRequest request) {
        return request == null
                || request.getQuestion() == null
                || request.getQuestion().isBlank();
    }

    /**
     * Normalises raw user input: trims whitespace and converts to lowercase.
     *
     * @param input raw question string
     * @return normalised string, safe for intent matching
     */
    private String normalise(String input) {
        return input.trim().toLowerCase();
    }

    // -------------------------------------------------------------------------
    // Intent detection
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the query contains at least one election-domain keyword.
     */
    private boolean isElectionRelated(String query) {
        return ELECTION_KEYWORDS.stream().anyMatch(query::contains);
    }

    /**
     * Returns {@code true} if the query expresses intent to learn about an election timeline.
     */
    private boolean hasTimelineIntent(String query) {
        if (TIMELINE_TRIGGERS.stream().anyMatch(query::contains)) {
            return true;
        }
        return query.contains("election") && INDIAN_STATES.stream().anyMatch(query::contains);
    }

    // -------------------------------------------------------------------------
    // Static response routing
    // -------------------------------------------------------------------------

    /**
     * Resolves a pre-built response for common, well-understood query types.
     *
     * @param query normalised query string
     * @return a static response string, or {@code null} if none matches
     */
    private String resolveStaticResponse(String query) {
        if (hasTimelineIntent(query))                                         return buildTimelineResponse(query);
        if (query.contains("register") || query.contains("registration"))     return buildRegistrationResponse();
        if (query.contains("how to vote") || query.contains("process of voting")) return buildHowToVoteResponse();
        if (query.contains("voter id") || query.contains("id proof"))         return buildVoterIdResponse();
        if (query.contains("polling booth") || query.contains("polling station")
                || query.contains("where to vote"))                           return buildPollingBoothResponse();
        if (query.contains("nota"))                                           return buildNotaResponse();
        if (query.contains("election types") || query.contains("types of election")) return buildElectionTypesResponse();
        return null;
    }

    // -------------------------------------------------------------------------
    // Static response builders
    // -------------------------------------------------------------------------

    private String buildRegistrationResponse() {
        return "You can register to vote online through the official Election Commission portal " +
               "or offline by submitting the required forms to your Electoral Registration Officer.";
    }

    private String buildHowToVoteResponse() {
        return "To vote, verify your name on the electoral roll. On election day, go to your " +
               "designated polling booth with valid ID, press the button next to your chosen " +
               "candidate on the EVM, and listen for the beep.";
    }

    private String buildVoterIdResponse() {
        return "Valid ID proofs for voting include Voter ID (EPIC), Aadhar card, Passport, " +
               "Driving License, PAN card, and other officially recognised government IDs.";
    }

    private String buildPollingBoothResponse() {
        return "You can find your polling booth on the official Election Commission website or app " +
               "by entering your Voter ID (EPIC) number.";
    }

    private String buildNotaResponse() {
        return "NOTA stands for 'None Of The Above'. It allows voters to reject all candidates " +
               "in their constituency while still exercising their democratic right to vote.";
    }

    private String buildElectionTypesResponse() {
        return "Main election types include General Elections (Lok Sabha), State Assembly Elections, " +
               "and Local Body Elections (such as Panchayats and Municipalities).";
    }

    /**
     * Builds a timeline response by extracting the mentioned state from the query
     * and delegating to {@link ElectionService}.
     *
     * @param query normalised query containing a state name
     * @return human-readable election date string
     */
    private String buildTimelineResponse(String query) {
        String detectedState = INDIAN_STATES.stream()
                .filter(query::contains)
                .findFirst()
                .orElse(null);

        if (detectedState == null) {
            return AppConstants.MSG_SCHEDULE_NOT_ANNOUNCED;
        }

        ElectionTimelineResponse timeline = electionService.getTimeline(detectedState);
        String displayName = capitaliseWords(detectedState);

        return String.format(
                "The next election in %s is expected around %s.%s",
                displayName, timeline.getPollingDate(), AppConstants.SOURCE_NOTE
        );
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    /**
     * Capitalises the first letter of each word in a space-separated string.
     *
     * @param str input string
     * @return title-cased string
     */
    private String capitaliseWords(String str) {
        if (str == null || str.isEmpty()) return str;
        return Arrays.stream(str.split("\\s+"))
                .map(token -> Character.toUpperCase(token.charAt(0)) + token.substring(1))
                .collect(Collectors.joining(" "));
    }

    /**
     * Wraps a plain string in an {@link AssistantResponse}.
     */
    private AssistantResponse respond(String message) {
        return new AssistantResponse(message);
    }
}
