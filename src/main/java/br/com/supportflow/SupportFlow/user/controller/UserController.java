package br.com.supportflow.SupportFlow.user.controller;

import br.com.supportflow.SupportFlow.common.dto.GenericResponse;
import br.com.supportflow.SupportFlow.user.dto.UserClientAccessAssign;
import br.com.supportflow.SupportFlow.user.dto.UserEnable;
import br.com.supportflow.SupportFlow.user.dto.UserRegister;
import br.com.supportflow.SupportFlow.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User", description = "Endpoints for managing users")
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @SecurityRequirements
    @Operation(summary = "Register a new user", description = "Creates a new user with the provided information")
    @PostMapping("/register")
    public ResponseEntity<GenericResponse> register(@Valid @RequestBody UserRegister userRegister) {
        return ResponseEntity.ok(userService.register(userRegister));
    }

    @SecurityRequirements
    @Operation(summary = "Enable a user account", description = "Enables a user account using a valid token")
    @PutMapping("/enable")
    public ResponseEntity<GenericResponse> enable(@Valid @RequestBody UserEnable userEnable) {
        return ResponseEntity.ok(userService.userEnable(userEnable));
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Returns a list of all users")
    public ResponseEntity<?> getAll(
            @RequestParam(value = "term", defaultValue = "") String term,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userService.getAll(term, page, size));
    }

    @Operation(summary = "Assign user to client", description = "Creates or reactivates access for a user in a client.")
    @PostMapping("/{userId}/clients/{clientId}")
    public ResponseEntity<?> assignClient(
            @PathVariable UUID userId,
            @PathVariable UUID clientId,
            @Valid @RequestBody UserClientAccessAssign assign
    ) {
        return ResponseEntity.ok(userService.assignClient(userId, clientId, assign));
    }

    @Operation(summary = "Remove user from client", description = "Deactivates user access for a client.")
    @DeleteMapping("/{userId}/clients/{clientId}")
    public ResponseEntity<?> removeClient(
            @PathVariable UUID userId,
            @PathVariable UUID clientId
    ) {
        return ResponseEntity.ok(userService.removeClient(userId, clientId));
    }

    @Operation(summary = "List clients by user", description = "Lists active client memberships for a user.")
    @GetMapping("/{userId}/clients")
    public ResponseEntity<?> listClientsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.listClientsByUser(userId));
    }

    @Operation(summary = "List users by client", description = "Lists active users for a client.")
    @GetMapping("/clients/{clientId}/users")
    public ResponseEntity<?> listUsersByClient(@PathVariable UUID clientId) {
        return ResponseEntity.ok(userService.listUsersByClient(clientId));
    }

}
