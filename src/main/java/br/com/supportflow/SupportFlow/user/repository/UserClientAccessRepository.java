package br.com.supportflow.SupportFlow.user.repository;

import br.com.supportflow.SupportFlow.user.entity.UserClientAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserClientAccessRepository extends JpaRepository<UserClientAccess, UUID> {
    boolean existsByUserIdAndClientIdAndActiveTrue(UUID userId, UUID clientId);

    Optional<UserClientAccess> findByUserIdAndClientId(UUID userId, UUID clientId);

    List<UserClientAccess> findByUserIdAndActiveTrue(UUID userId);

    List<UserClientAccess> findByClientIdAndActiveTrue(UUID clientId);
}
