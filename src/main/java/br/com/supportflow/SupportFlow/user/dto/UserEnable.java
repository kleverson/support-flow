package br.com.supportflow.SupportFlow.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserEnable(
        @NotBlank String token
) {
}
