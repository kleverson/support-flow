package br.com.supportflow.SupportFlow.ticket.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateBody(
        @NotBlank String title,
        boolean global
) {
}
