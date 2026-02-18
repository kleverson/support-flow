package br.com.supportflow.SupportFlow.ticket.dto;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String title
) {
}
