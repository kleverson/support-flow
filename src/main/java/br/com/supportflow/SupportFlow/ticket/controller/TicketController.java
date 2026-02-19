package br.com.supportflow.SupportFlow.ticket.controller;

import br.com.supportflow.SupportFlow.ticket.dto.TicketAssign;
import br.com.supportflow.SupportFlow.ticket.dto.TicketPostBody;
import br.com.supportflow.SupportFlow.ticket.dto.TicketResponse;
import br.com.supportflow.SupportFlow.ticket.dto.TicketStatusUpdateBody;
import br.com.supportflow.SupportFlow.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Ticket", description = "Endpoints for managing tickets")
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Operation(summary = "Get all tickets", description = "Returns a paginated list of tickets, optionally filtered by a search term.")
    @GetMapping
    public ResponseEntity<Page<TicketResponse>> getAll(
            @RequestParam(value = "term", defaultValue = "") String term,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ticketService.getAll(term, page, size));
    }

    @Operation(summary = "Create ticket", description = "Creates a new ticket with the provided details.")
    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody TicketPostBody ticketPostBody,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ticketService.create(ticketPostBody, authentication));
    }

    @Operation(summary = "Update ticket", description = "Updates an OPEN ticket.")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable UUID id,
            @Valid @RequestBody TicketPostBody ticketPostBody
    ) {
        return ResponseEntity.ok(ticketService.update(id, ticketPostBody));
    }

    @Operation(summary = "Assign ticket", description = "Assigns an OPEN ticket to a support agent.")
    @PatchMapping("/{id}/assign")
    public ResponseEntity<?> assigned(@PathVariable("id") UUID id, @Valid @RequestBody TicketAssign assign) {
        return ResponseEntity.ok(ticketService.assign(id, assign));
    }

    @Operation(summary = "Change status", description = "Changes ticket status.")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TicketStatusUpdateBody body
    ) {
        return ResponseEntity.ok(ticketService.changeStatus(id, body.status()));
    }
}
