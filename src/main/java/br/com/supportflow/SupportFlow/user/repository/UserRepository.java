package br.com.supportflow.SupportFlow.user.repository;

import br.com.supportflow.SupportFlow.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByToken(String token);
}
