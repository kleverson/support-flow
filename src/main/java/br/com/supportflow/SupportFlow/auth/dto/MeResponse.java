package br.com.supportflow.SupportFlow.auth.dto;

import br.com.supportflow.SupportFlow.user.entity.User;

import java.util.UUID;

public record MeResponse(
        UUID userId,
        String name,
        String email,
        String role,
        boolean active
) {
    public static MeResponse from(User user) {
        return new MeResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.isActive()
        );
    }
}
