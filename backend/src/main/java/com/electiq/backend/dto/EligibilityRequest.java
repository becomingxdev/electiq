package com.electiq.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class EligibilityRequest {
    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 150, message = "Age must be at most 150")
    private int age;
    
    private boolean citizen;
    private boolean hasIdProof;
}
