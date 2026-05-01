package com.electiq.backend.controller;

import com.electiq.backend.dto.AssistantRequest;
import com.electiq.backend.dto.AssistantResponse;
import com.electiq.backend.service.AssistantService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantController {

    private static final Logger logger = LoggerFactory.getLogger(AssistantController.class);
    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/ask")
    public ResponseEntity<AssistantResponse> askAssistant(@Valid @RequestBody AssistantRequest request) {
        logger.info("Incoming request to /api/v1/assistant/ask");
        AssistantResponse response = assistantService.askQuestion(request);
        return ResponseEntity.ok(response);
    }
}
