package br.com.supportflow.SupportFlow.ticket.controller;

import br.com.supportflow.SupportFlow.ticket.dto.CategoryCreateBody;
import br.com.supportflow.SupportFlow.ticket.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Category", description = "Endpoints for managing categories")
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "List categories by scope", description = "Scope: global, client or all. For client/all send X-Client-Id.")
    @GetMapping
    public ResponseEntity<?> listByScope(
            @RequestParam(value = "scope", defaultValue = "all") String scope,
            @RequestHeader(value = "X-Client-Id", required = false) String clientId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(categoryService.listByScope(scope, clientId, authentication));
    }

    @Operation(summary = "Create category", description = "Create global category (admin only) or client category (requires X-Client-Id).")
    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody CategoryCreateBody body,
            @RequestHeader(value = "X-Client-Id", required = false) String clientId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(categoryService.create(body, clientId, authentication));
    }
}
