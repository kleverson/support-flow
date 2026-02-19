package br.com.supportflow.SupportFlow.user.repository;

import br.com.supportflow.SupportFlow.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByToken(String token);

    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
