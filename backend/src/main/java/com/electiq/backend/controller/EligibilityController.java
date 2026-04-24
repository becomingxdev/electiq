package com.electiq.backend.controller;

import com.electiq.backend.dto.EligibilityRequest;
import com.electiq.backend.dto.EligibilityResponse;
import com.electiq.backend.service.EligibilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/eligibility")
public class EligibilityController {

    private final EligibilityService eligibilityService;

    public EligibilityController(EligibilityService eligibilityService) {
        this.eligibilityService = eligibilityService;
    }

    @PostMapping("/check")
    public ResponseEntity<EligibilityResponse> checkEligibility(@Valid @RequestBody EligibilityRequest request) {
        EligibilityResponse response = eligibilityService.checkEligibility(request);
        return ResponseEntity.ok(response);
    }
}
