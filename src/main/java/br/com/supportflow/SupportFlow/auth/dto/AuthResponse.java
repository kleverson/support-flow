package br.com.supportflow.SupportFlow.auth.dto;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UUID userId,
        String name,
        String email,
        String role
) {
    public static AuthResponse from(String accessToken, String refreshToken, UUID userId, String name, String email, String role) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", userId, name, email, role);
    }
}
