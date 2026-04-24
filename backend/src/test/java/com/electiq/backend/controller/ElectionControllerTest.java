package com.electiq.backend.controller;

import com.electiq.backend.dto.ElectionTimelineResponse;
import com.electiq.backend.service.ElectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
@WebMvcTest(ElectionController.class)
public class ElectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ElectionService electionService;

    @Test
    void shouldReturnTimelineForState() throws Exception {
        ElectionTimelineResponse mockResponse = new ElectionTimelineResponse(
                "Andhra Pradesh",
                "2026-05-10",
                "2026-06-01",
                "2026-06-05"
        );

        when(electionService.getTimeline("Andhra Pradesh")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/election/timeline")
                .param("state", "Andhra Pradesh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("Andhra Pradesh"))
                .andExpect(jsonPath("$.registrationDeadline").value("2026-05-10"))
                .andExpect(jsonPath("$.pollingDate").value("2026-06-01"))
                .andExpect(jsonPath("$.resultDate").value("2026-06-05"));
    }
}
