package br.com.supportflow.SupportFlow.user.dto;

import br.com.supportflow.SupportFlow.user.entity.UserClientAccess;
import br.com.supportflow.SupportFlow.user.entity.enums.UserClientRole;

import java.time.Instant;
import java.util.UUID;

public record UserClientAccessResponse(
        UUID userId,
        UUID clientId,
        UserClientRole role,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserClientAccessResponse from(UserClientAccess access) {
        return new UserClientAccessResponse(
                access.getUser().getId(),
                access.getClient().getId(),
                access.getRoleInClient(),
                access.isActive(),
                access.getCreatedAt(),
                access.getUpdatedAt()
        );
    }
}
