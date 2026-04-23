package com.electiq.backend.service;

import com.electiq.backend.dto.EligibilityRequest;
import com.electiq.backend.dto.EligibilityResponse;
import org.springframework.stereotype.Service;

@Service
public class EligibilityService {

    public EligibilityResponse checkEligibility(EligibilityRequest request) {
        if (!request.isCitizen()) {
            return new EligibilityResponse(false, "You must be a citizen to register and vote.");
        }
        if (request.getAge() < 18) {
            return new EligibilityResponse(false, "You must be at least 18 years old to register and vote.");
        }
        if (!request.isHasIdProof()) {
            return new EligibilityResponse(true, "You are eligible to register and vote, but registration may require valid ID documents.");
        }
        return new EligibilityResponse(true, "You are eligible to register and vote.");
    }
}
