package br.com.supportflow.SupportFlow.client.dto;

import jakarta.validation.constraints.NotBlank;

public record ClientPostBody(

        String brand,

        @NotBlank String name,
        String description,
        @NotBlank String phone,
        @NotBlank String email
) {
}
