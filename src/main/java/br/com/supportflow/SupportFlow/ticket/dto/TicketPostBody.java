package br.com.supportflow.SupportFlow.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public record TicketPostBody(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String priority,
        Optional<UUID> categoryId,
        @NotNull UUID clientId,

        List<String> files

) {
}
