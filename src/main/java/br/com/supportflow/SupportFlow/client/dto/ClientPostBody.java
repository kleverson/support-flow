package br.com.supportflow.SupportFlow.client.dto;

import jakarta.validation.constraints.NotBlank;

public record ClientPostBody(
        @NotBlank String name,
        String description,
        @NotBlank String phone,
        @NotBlank String email
) {
}
