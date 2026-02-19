package br.com.supportflow.SupportFlow.user.controller;

import br.com.supportflow.SupportFlow.common.dto.GenericResponse;
import br.com.supportflow.SupportFlow.user.dto.UserEnable;
import br.com.supportflow.SupportFlow.user.dto.UserRegister;
import br.com.supportflow.SupportFlow.user.entity.User;
import br.com.supportflow.SupportFlow.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
