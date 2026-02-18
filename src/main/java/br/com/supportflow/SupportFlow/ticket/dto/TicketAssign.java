package br.com.supportflow.SupportFlow.ticket.dto;

import java.util.UUID;

public record TicketAssign(
        UUID ticketId,
        UUID agentId
) {
}
