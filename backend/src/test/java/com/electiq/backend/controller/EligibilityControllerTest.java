package com.electiq.backend.controller;

import com.electiq.backend.dto.EligibilityRequest;
import com.electiq.backend.dto.EligibilityResponse;
import com.electiq.backend.service.EligibilityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WithMockUser
@WebMvcTest(EligibilityController.class)
public class EligibilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EligibilityService eligibilityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn200ForValidEligibleRequest() throws Exception {
        EligibilityRequest request = new EligibilityRequest();
        request.setAge(25);
        request.setCitizen(true);
        request.setHasIdProof(true);

        EligibilityResponse mockResponse = new EligibilityResponse(true, "You are eligible to vote.");
        when(eligibilityService.checkEligibility(any(EligibilityRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/eligibility/check")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(true))
                .andExpect(jsonPath("$.message").value("You are eligible to vote."));
    }

    @Test
    void shouldReturnProperResponseForUnderageRequest() throws Exception {
        EligibilityRequest request = new EligibilityRequest();
        request.setAge(16);
        request.setCitizen(true);
        request.setHasIdProof(true);

        EligibilityResponse mockResponse = new EligibilityResponse(false, "You must be at least 18 years old to vote.");
        when(eligibilityService.checkEligibility(any(EligibilityRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/eligibility/check")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(false))
                .andExpect(jsonPath("$.message").value("You must be at least 18 years old to vote."));
    }
}
