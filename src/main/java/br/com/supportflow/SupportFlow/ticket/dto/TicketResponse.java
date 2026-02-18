package br.com.supportflow.SupportFlow.ticket.dto;

import br.com.supportflow.SupportFlow.ticket.entity.TicketPriority;
import br.com.supportflow.SupportFlow.ticket.entity.TicketStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String title,
        String description,
        TicketPriority priority,
        TicketStatus status,
        CategoryResponse category,
        UserSummaryResponse requester,
        UserSummaryResponse assigned,
        List<CommentResponse> comments,
        Instant createdAt,
        Instant closedAt,
        Instant updatedAt
) {
}
