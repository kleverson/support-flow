package br.com.supportflow.SupportFlow.ticket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Comment",description = "Endpoints for managing comments")
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    @Operation(summary = "Get all categories", description = "Returns a paginated list of categories, optionally filtered by a search term.")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAll(
            @PathVariable UUID id,
            @RequestParam(value="term", defaultValue = "") String term,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ){
        return ResponseEntity.ok("Get all categories");
    }
}
