package com.electiq.backend.service;

import com.electiq.backend.config.AppConstants;
import com.electiq.backend.dto.EligibilityRequest;
import com.electiq.backend.dto.EligibilityResponse;
import org.springframework.stereotype.Service;

/**
 * Determines whether a citizen meets the statutory eligibility criteria to vote.
 *
 * <p>Rules evaluated in order:
 * <ol>
 *   <li>Must be a citizen.</li>
 *   <li>Must be at least 18 years old.</li>
 *   <li>Eligible with a note if valid ID proof is absent.</li>
 *   <li>Fully eligible otherwise.</li>
 * </ol>
 */
@Service
public class EligibilityService {

    public EligibilityResponse checkEligibility(EligibilityRequest request) {
        if (!request.isCitizen()) {
            return new EligibilityResponse(false, AppConstants.MSG_NOT_A_CITIZEN);
        }
        if (request.getAge() < 18) {
            return new EligibilityResponse(false, AppConstants.MSG_UNDER_AGE);
        }
        if (!request.isHasIdProof()) {
            return new EligibilityResponse(true, AppConstants.MSG_ELIGIBLE_NO_ID);
        }
        return new EligibilityResponse(true, AppConstants.MSG_ELIGIBLE);
    }
}
