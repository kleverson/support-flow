package br.com.supportflow.SupportFlow.ticket.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID ticketId,
        String message,
        UserSummaryResponse author,
        Instant createdAt,
        Instant updatedAt
) {
}
