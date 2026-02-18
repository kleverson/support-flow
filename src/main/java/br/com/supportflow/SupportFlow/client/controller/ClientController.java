package br.com.supportflow.SupportFlow.client.controller;

import br.com.supportflow.SupportFlow.client.dto.ClientPostBody;
import br.com.supportflow.SupportFlow.client.service.ClientService;
import br.com.supportflow.SupportFlow.common.dto.GenericResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name="Client", description = "Endpoints for managing clients")
@RestController
@RequestMapping("/api/v1/client")
public class ClientController {

    private ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @Operation(summary = "Register a new client", description = "Creates a new client with the provided information")
    @PostMapping
    public ResponseEntity<GenericResponse> register(@Valid @RequestBody ClientPostBody clientPostBody){
        return ResponseEntity.ok(clientService.create(clientPostBody));
    }

    @Operation(summary = "Update an existing client", description = "Updates the information of an existing client by ID")
    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse> update(@PathVariable UUID id, @Valid @RequestBody ClientPostBody clientPostBody){
        return ResponseEntity.ok(clientService.update(id,clientPostBody));
    }

    @Operation(summary = "Remove a client", description = "Deletes an existing client by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse> remove(@PathVariable UUID id){
        return ResponseEntity.ok(clientService.deleteOrRestore(id));
    }

    @Operation(summary = "Get client by ID", description = "Retrieves the details of a client by their unique ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id){
        return ResponseEntity.ok(clientService.get(id));
    }


    @Operation(summary = "Get all clients", description = "Retrieves a paginated list of clients, optionally filtered by a search term")
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(value="term", defaultValue = "") String term,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ){
        return ResponseEntity.ok(clientService.getAll(term, page, size));
    }


}
