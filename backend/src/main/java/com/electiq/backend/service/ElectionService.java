package com.electiq.backend.service;

import com.electiq.backend.dto.ElectionTimelineResponse;
import org.springframework.stereotype.Service;

@Service
public class ElectionService {

    public ElectionTimelineResponse getTimeline(String state) {
        // Mocking the response as requested
        return new ElectionTimelineResponse(
                state != null ? state : "Unknown",
                "2026-09-01",
                "2026-09-20",
                "2026-09-25"
        );
    }
}
