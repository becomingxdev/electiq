package com.electiq.backend.service;

import com.electiq.backend.dto.AssistantRequest;
import com.electiq.backend.dto.AssistantResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class AssistantService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final Logger logger = LoggerFactory.getLogger(AssistantService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    public AssistantResponse askQuestion(AssistantRequest request) {
        if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            return new AssistantResponse("Please provide a question.");
        }
        
        String query = request.getQuestion().trim().toLowerCase();
        
        if (!isElectionRelated(query)) {
            return new AssistantResponse("I can only assist with election-related topics.");
        }
        
        String staticResponse = getStaticResponse(query);
        if (staticResponse != null) {
            return new AssistantResponse(staticResponse);
        }
        
        return new AssistantResponse(callGeminiApi(request.getQuestion()));
    }

    private boolean isElectionRelated(String query) {
        List<String> keywords = Arrays.asList(
            "vote", "voting", "election", "poll", "ballot", "candidate", 
            "nota", "ec", "democracy", "voter", "campaign", "politics", 
            "government", "parliament", "assembly", "president", "minister", "mayor", "mla", "mp"
        );
        for (String word : keywords) {
            if (query.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private String getStaticResponse(String query) {
        boolean hasTimelineIntent = query.contains("next election") || 
            query.contains("upcoming election") || 
            query.contains("election date") || 
            query.contains("polling date") || 
            query.contains("schedule") || 
            query.contains("when is election");
            
        if (!hasTimelineIntent && query.contains("election")) {
            List<String> states = Arrays.asList(
                "andhra", "arunachal", "assam", "bihar", "chhattisgarh", "goa", "gujarat", 
                "haryana", "himachal", "jharkhand", "karnataka", "kerala", "madhya pradesh", 
                "maharashtra", "manipur", "meghalaya", "mizoram", "nagaland", "odisha", 
                "punjab", "rajasthan", "sikkim", "tamil nadu", "telangana", "tripura", 
                "uttar pradesh", "uttarakhand", "west bengal", "delhi", "kashmir", "jammu"
            );
            for (String state : states) {
                if (query.contains(state)) {
                    hasTimelineIntent = true;
                    break;
                }
            }
        }

        if (hasTimelineIntent) {
            return "Official schedule has not been announced yet. Please check Election Commission updates.";
        }

        if (query.contains("register") || query.contains("registration")) {
            return "You can register to vote online through the official Election Commission portal or offline by submitting the required forms to your Electoral Registration Officer.";
        }
        if (query.contains("how to vote") || query.contains("process of voting")) {
            return "To vote, verify your name on the electoral roll. On election day, go to your designated polling booth with valid ID, press the button next to your chosen candidate on the EVM, and listen for the beep.";
        }
        if (query.contains("voter id") || query.contains("documents") || query.contains("id proof")) {
            return "Valid ID proofs for voting include Voter ID (EPIC), Aadhar card, Passport, Driving License, PAN card, and other officially recognized government IDs.";
        }
        if (query.contains("polling booth") || query.contains("polling station") || query.contains("where to vote")) {
            return "You can find your polling booth on the official Election Commission website or app by entering your Voter ID (EPIC) number.";
        }
        if (query.contains("nota")) {
            return "NOTA stands for 'None Of The Above'. It allows voters to reject all candidates in their constituency while still exercising their democratic right to vote.";
        }
        if (query.contains("election types") || query.contains("types of election")) {
            return "Main election types include General Elections (Lok Sabha), State Assembly Elections, and Local Body Elections (such as Panchayats and Municipalities).";
        }
        return null;
    }

    private String callGeminiApi(String query) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            return "I am currently unable to answer complex questions because the API key is not configured.";
        }
        
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String prompt = "You are ElectIQ, an election assistant. Answer only election-related questions. Give one complete clear answer in under 40 words. If unrelated, politely refuse. User asked: " + query;
                
            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> contents = new HashMap<>();
            Map<String, Object> parts = new HashMap<>();
            parts.put("text", prompt);
            contents.put("parts", Arrays.asList(parts));
            requestBody.put("contents", Arrays.asList(contents));
            
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("maxOutputTokens", 180);
            generationConfig.put("temperature", 0.2);
            requestBody.put("generationConfig", generationConfig);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getBody() != null && response.getBody().containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                    if (content != null && content.containsKey("parts")) {
                        List<Map<String, Object>> resParts = (List<Map<String, Object>>) content.get("parts");
                        if (resParts != null && !resParts.isEmpty()) {
                            String text = (String) resParts.get(0).get("text");
                            return text != null ? text.trim() : "AI assistant is temporarily unavailable. Please try again later.";
                        }
                    }
                }
            }
            return "AI assistant is temporarily unavailable. Please try again later.";
        } catch (HttpStatusCodeException e) {
            logger.error("Gemini API HTTP Error: {} - Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "AI assistant is reached its capacity or is temporarily unavailable. Please try again later.";
        } catch (Exception e) {
            logger.error("Gemini API Error: {}", e.getMessage());
            return "AI assistant is temporarily unavailable due to a network error. Please try again later.";
        }
    }
}
