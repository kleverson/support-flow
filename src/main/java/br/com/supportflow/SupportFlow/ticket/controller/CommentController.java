package br.com.supportflow.SupportFlow.ticket.controller;

import br.com.supportflow.SupportFlow.ticket.dto.CommentCreateBody;
import br.com.supportflow.SupportFlow.ticket.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Comment", description = "Endpoints for managing comments")
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "List comments by ticket", description = "Requires X-Client-Id header.")
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<?> listByTicket(
            @PathVariable UUID ticketId,
            @RequestParam(value = "term", defaultValue = "") String term,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestHeader("X-Client-Id") String clientId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(commentService.listByTicket(ticketId, term, page, size, clientId, authentication));
    }

    @Operation(summary = "Create comment", description = "Creates comment in a ticket under active client context.")
    @PostMapping("/tickets/{ticketId}")
    public ResponseEntity<?> create(
            @PathVariable UUID ticketId,
            @Valid @RequestBody CommentCreateBody body,
            @RequestHeader("X-Client-Id") String clientId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(commentService.create(ticketId, body, clientId, authentication));
    }
}
