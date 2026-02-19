package br.com.supportflow.SupportFlow.ticket.repository;

import br.com.supportflow.SupportFlow.ticket.entity.Attachment;
import br.com.supportflow.SupportFlow.ticket.entity.enums.AttachmentOwnerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findByOwnerIdAndOwnerType(UUID ownerId, AttachmentOwnerType ownerType);
}
