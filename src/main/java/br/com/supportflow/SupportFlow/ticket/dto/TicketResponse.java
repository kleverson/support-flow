package br.com.supportflow.SupportFlow.ticket.dto;

import br.com.supportflow.SupportFlow.ticket.entity.enums.TicketPriority;
import br.com.supportflow.SupportFlow.ticket.entity.enums.TicketStatus;

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
        List<AttachmentResponse> files,
        Instant createdAt,
        Instant closedAt,
        Instant updatedAt

) {
}
