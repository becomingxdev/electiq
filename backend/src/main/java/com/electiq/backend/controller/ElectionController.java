package com.electiq.backend.controller;

import com.electiq.backend.dto.ElectionTimelineResponse;
import com.electiq.backend.service.ElectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/elections")
public class ElectionController {

    private final ElectionService electionService;

    public ElectionController(ElectionService electionService) {
        this.electionService = electionService;
    }

    @GetMapping("/timeline")
    public ResponseEntity<ElectionTimelineResponse> getElectionTimeline(@RequestParam(required = false) String state) {
        ElectionTimelineResponse response = electionService.getTimeline(state);
        return ResponseEntity.ok(response);
    }
}
