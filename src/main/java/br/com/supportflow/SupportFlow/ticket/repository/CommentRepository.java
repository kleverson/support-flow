package br.com.supportflow.SupportFlow.ticket.repository;

import br.com.supportflow.SupportFlow.ticket.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    Page<Comment> findByTicketId(UUID ticketId, Pageable pageable);

    Page<Comment> findByTicketIdAndMessageContainingIgnoreCase(UUID ticketId, String message, Pageable pageable);
}
