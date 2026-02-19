package br.com.supportflow.SupportFlow.common.security;

import br.com.supportflow.SupportFlow.common.exception.BusinessException;
import br.com.supportflow.SupportFlow.user.entity.Role;
import br.com.supportflow.SupportFlow.user.repository.UserClientAccessRepository;
import br.com.supportflow.SupportFlow.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ClientAccessGuard {
    private final UserRepository userRepository;
    private final UserClientAccessRepository userClientAccessRepository;

    public ClientAccessGuard(UserRepository userRepository, UserClientAccessRepository userClientAccessRepository) {
        this.userRepository = userRepository;
        this.userClientAccessRepository = userClientAccessRepository;
    }

    public void assertUserHasAccess(Authentication authentication, UUID clientId) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new BusinessException("UNAUTHORIZED", "User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        var user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        if (Role.ADMIN.equals(user.getRole())) {
            return;
        }

        boolean hasAccess = userClientAccessRepository.existsByUserIdAndClientIdAndActiveTrue(user.getId(), clientId);
        if (!hasAccess) {
            throw new BusinessException("ACCESS_DENIED_CLIENT", "User has no access to this client", HttpStatus.FORBIDDEN);
        }
    }
}
