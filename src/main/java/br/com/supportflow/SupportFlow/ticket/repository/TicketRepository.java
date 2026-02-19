package br.com.supportflow.SupportFlow.ticket.repository;

import br.com.supportflow.SupportFlow.ticket.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    Page<Ticket> findByClientId(UUID clientId, Pageable pageable);

    Page<Ticket> findByClientIdAndTitleContainingIgnoreCaseOrClientIdAndDescriptionContainingIgnoreCase(
            UUID clientIdTitle,
            String title,
            UUID clientIdDescription,
            String description,
            Pageable pageable
    );

    Page<Ticket> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title,
            String description,
            Pageable pageable
    );
}
