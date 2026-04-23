package com.electiq.backend.dto;

import lombok.Data;

@Data
public class EligibilityRequest {
    private int age;
    private boolean citizen;
    private boolean hasIdProof;
}
