package br.com.supportflow.SupportFlow.ticket.controller;

import br.com.supportflow.SupportFlow.ticket.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Category",description = "Endpoints for managing categories")
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }


    @Operation(summary = "Get all categories", description = "Returns a paginated list of categories, optionally filtered by a search term.")
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(value="term", defaultValue = "") String term,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ){
        return ResponseEntity.ok("Get all categories");
    }

    @Operation(summary = "List all categories", description = "Returns a list of all categories without pagination.")
    @GetMapping("/list")
    public ResponseEntity<?> listAll(){
        return ResponseEntity.ok(categoryService.listAll());
    }

    @Operation(summary = "Create a new category", description = "Creates a new category with the provided details.")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody String categoryDetails) {
        return ResponseEntity.ok("Create a new category");
    }

}
