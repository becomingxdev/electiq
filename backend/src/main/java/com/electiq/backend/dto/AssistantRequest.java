package com.electiq.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssistantRequest {
    @NotBlank(message = "Question cannot be blank")
    @Size(max = 500, message = "Question is too long (max 500 characters)")
    private String question;
}
