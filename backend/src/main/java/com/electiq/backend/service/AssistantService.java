package com.electiq.backend.service;

import com.electiq.backend.dto.AssistantRequest;
import com.electiq.backend.dto.AssistantResponse;
import org.springframework.stereotype.Service;

@Service
public class AssistantService {

    public AssistantResponse askQuestion(AssistantRequest request) {
        // Smart mock response as requested
        return new AssistantResponse("To vote, first verify your registration, carry valid ID, and visit your polling booth on election day.");
    }
}
