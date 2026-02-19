package br.com.supportflow.SupportFlow.ticket.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateBody(
        @NotBlank String message
) {
}
