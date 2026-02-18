package br.com.supportflow.SupportFlow.client.repository;

import br.com.supportflow.SupportFlow.client.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    Page<Client> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
