package br.com.supportflow.SupportFlow.ticket.service;

import br.com.supportflow.SupportFlow.client.repository.ClientRepository;
import br.com.supportflow.SupportFlow.common.dto.GenericResponse;
import br.com.supportflow.SupportFlow.common.exception.BusinessException;
import br.com.supportflow.SupportFlow.common.util.StorageFile;
import br.com.supportflow.SupportFlow.ticket.dto.TicketAssign;
import br.com.supportflow.SupportFlow.ticket.dto.TicketPostBody;
import br.com.supportflow.SupportFlow.ticket.dto.TicketResponse;
import br.com.supportflow.SupportFlow.ticket.entity.Attachment;
import br.com.supportflow.SupportFlow.ticket.entity.Category;
import br.com.supportflow.SupportFlow.ticket.entity.Ticket;
import br.com.supportflow.SupportFlow.ticket.entity.enums.AttachmentOwnerType;
import br.com.supportflow.SupportFlow.ticket.entity.enums.TicketPriority;
import br.com.supportflow.SupportFlow.ticket.entity.enums.TicketStatus;
import br.com.supportflow.SupportFlow.ticket.mapper.TicketMapper;
import br.com.supportflow.SupportFlow.ticket.repository.AttachmentRepository;
import br.com.supportflow.SupportFlow.ticket.repository.CategoryRepository;
import br.com.supportflow.SupportFlow.ticket.repository.TicketRepository;
import br.com.supportflow.SupportFlow.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Service
public class TicketService {
    private static final String DEFAULT_CATEGORY = "UNCATEGORIZED";

    private final TicketRepository ticketRepository;
    private final ClientRepository clientRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private final AttachmentRepository attachmentRepository;
    private final StorageFile storageFile;

    public TicketService(
            TicketRepository ticketRepository,
            ClientRepository clientRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            AttachmentRepository attachmentRepository, StorageFile storageFile) {
        this.ticketRepository = ticketRepository;
        this.clientRepository = clientRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
        this.storageFile = storageFile;
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> getAll(String term, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String searchTerm = term == null ? "" : term.trim();

        Page<Ticket> tickets = searchTerm.isBlank()
                ? ticketRepository.findAll(pageable)
                : ticketRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                searchTerm,
                searchTerm,
                pageable
        );

        return tickets.map(item -> {
            var attachments = attachmentRepository.findByOwnerIdAndOwnerType(item.getId(), AttachmentOwnerType.TICKET);

            return TicketMapper.toTicketResponse(item, attachments);


        });
    }

    @Transactional
    public TicketResponse assign(UUID id, TicketAssign assign)
    {
        var ticket = ticketRepository.findById(id).orElseThrow(() -> new BusinessException("TICKET_NOT_FOUND", "Ticket not found", HttpStatus.NOT_FOUND));

        var assignedUser = userRepository.findById(assign.agentId()).orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        ticket.setAssigned(assignedUser);

        if(ticket.getStatus().equals(TicketStatus.OPEN)){
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        ticketRepository.save(ticket);

        var attachments = attachmentRepository.findByOwnerIdAndOwnerType(ticket.getId(), AttachmentOwnerType.TICKET);
        return TicketMapper.toTicketResponse(ticket, attachments);
    }

    @Transactional
    public GenericResponse create(TicketPostBody body, Authentication userDetails) {
        Ticket ticket = new Ticket();
        ticket.setTitle(body.title());
        ticket.setDescription(body.description());
        ticket.setPriority(resolvePriority(body.priority()));
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCategory(resolveCategory(body.categoryId()));
        ticket.setClient(clientRepository.findById(body.clientId())
                .orElseThrow(() -> new BusinessException("CLIENT_NOT_FOUND", "Client not found", HttpStatus.NOT_FOUND)));


        ticket.setRequester(userRepository.findByEmail(userDetails.getName())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND)));


        ticketRepository.save(ticket);

        List<String> files = body.files() == null ? List.of() : body.files();
        if(!files.isEmpty()){
            files.forEach(item -> {
                String contentType = resolveContentType(item);
                long sizeBytes = storageFile.decodeBase64(item).length;
                var file = storageFile.uploadFile(item, "tickets/" + ticket.getId() + "/", contentType);

                var attachment = new Attachment();
                attachment.setOwnerId(ticket.getId());
                attachment.setOwnerType(AttachmentOwnerType.TICKET);
                attachment.setStoragePath(file);
                attachment.setContentType(contentType);
                attachment.setSizeBytes(sizeBytes);
                attachment.setFileName(resolveFileName(file));

                attachmentRepository.save(attachment);


            });
        }

        return new GenericResponse("Ticket created successfully!");
    }

    @Transactional
    public GenericResponse update(UUID id, TicketPostBody body) {
        Ticket currentTicket = ticketRepository.findById(id)
                .orElseThrow(() -> new BusinessException("TICKET_NOT_FOUND", "Ticket not found", HttpStatus.NOT_FOUND));

        if (!TicketStatus.OPEN.equals(currentTicket.getStatus())) {
            throw new BusinessException("INVALID_STATUS", "Only OPEN tickets can be updated", HttpStatus.BAD_REQUEST);
        }

        currentTicket.setTitle(body.title());
        currentTicket.setDescription(body.description());
        currentTicket.setPriority(resolvePriority(body.priority()));
        currentTicket.setCategory(resolveCategory(body.categoryId()));

        ticketRepository.save(currentTicket);
        return new GenericResponse("Ticket updated successfully!");
    }

    @Transactional
    public GenericResponse changeStatus(UUID id, TicketStatus status) {
        Ticket currentTicket = ticketRepository.findById(id)
                .orElseThrow(() -> new BusinessException("TICKET_NOT_FOUND", "Ticket not found", HttpStatus.NOT_FOUND));

        currentTicket.setStatus(status);
        if (TicketStatus.CLOSED.equals(status)) {
            if (currentTicket.getClosedAt() == null) {
                currentTicket.setClosedAt(Instant.now());
            }
        } else {
            currentTicket.setClosedAt(null);
        }

        ticketRepository.save(currentTicket);
        return new GenericResponse("Ticket status updated successfully!");
    }

    private Category resolveCategory(Optional<UUID> categoryId) {
        if (categoryId != null && categoryId.isPresent()) {
            return categoryRepository.findById(categoryId.get())
                    .orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND));
        }

        return categoryRepository.findByTitle(DEFAULT_CATEGORY)
                .orElseThrow(() -> new BusinessException(
                        "DEFAULT_CATEGORY_NOT_FOUND",
                        "Default category UNCATEGORIZED not found",
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));
    }

    private TicketPriority resolvePriority(String rawPriority) {
        try {
            return TicketPriority.valueOf(rawPriority.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new BusinessException("INVALID_PRIORITY", "Invalid priority value", HttpStatus.BAD_REQUEST);
        }
    }

    private String resolveContentType(String base64) {
        if (base64 == null || base64.isBlank()) {
            throw new BusinessException("INVALID_FILE", "File payload is empty", HttpStatus.BAD_REQUEST);
        }
        if (base64.startsWith("data:")) {
            int start = "data:".length();
            int end = base64.indexOf(';');
            if (end > start) {
                return base64.substring(start, end);
            }
        }
        return "application/octet-stream";
    }

    private String resolveFileName(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            return UUID.randomUUID().toString();
        }
        int idx = storagePath.lastIndexOf('/');
        if (idx < 0 || idx == storagePath.length() - 1) {
            return UUID.randomUUID().toString();
        }
        return storagePath.substring(idx + 1);
    }
}
