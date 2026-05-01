package com.electiq.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssistantRequest {
    @NotBlank(message = "Query cannot be blank")
    @Size(max = 500, message = "Query is too long (max 500 characters)")
    private String query;
}
