package com.electiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElectionTimelineResponse {
    private String state;
    private String registrationDeadline;
    private String pollingDate;
    private String resultDate;
}
