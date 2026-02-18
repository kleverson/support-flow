package br.com.supportflow.SupportFlow.ticket.dto;

import br.com.supportflow.SupportFlow.ticket.entity.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record TicketStatusUpdateBody(
        @NotNull TicketStatus status
) {
}
