package com.electiq.backend.controller;

import com.electiq.backend.dto.AssistantRequest;
import com.electiq.backend.dto.AssistantResponse;
import com.electiq.backend.service.AssistantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.electiq.backend.config.AppConstants.API_KEY_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
@WebMvcTest(AssistantController.class)
@TestPropertySource(properties = "API_KEY=test-key")
public class AssistantControllerTest {

    private static final String TEST_API_KEY = "test-key";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssistantService assistantService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SuppressWarnings("null")
    void shouldReturnAnswerForQuestion() throws Exception {
        AssistantRequest request = new AssistantRequest();
        request.setQuery("How do I register to vote?");

        AssistantResponse mockResponse = new AssistantResponse("You can register online via the NVSP portal.");
        when(assistantService.askQuestion(any(AssistantRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/assistant/ask")
                .with(csrf())
                .header(API_KEY_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("You can register online via the NVSP portal."));
    }
}
