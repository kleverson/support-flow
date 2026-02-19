package br.com.supportflow.SupportFlow.ticket.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Optional;
import java.util.UUID;

public record TicketPutBody(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String priority,
        Optional<UUID> categoryId,
        @NotBlank UUID clientId

) {
}