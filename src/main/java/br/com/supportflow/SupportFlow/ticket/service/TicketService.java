package br.com.supportflow.SupportFlow.ticket.service;

import br.com.supportflow.SupportFlow.client.entity.Client;
import br.com.supportflow.SupportFlow.client.repository.ClientRepository;
import br.com.supportflow.SupportFlow.common.dto.GenericResponse;
import br.com.supportflow.SupportFlow.common.exception.BusinessException;
import br.com.supportflow.SupportFlow.common.security.ClientAccessGuard;
import br.com.supportflow.SupportFlow.common.security.ClientContextResolver;
import br.com.supportflow.SupportFlow.common.util.Base64Utils;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketService {
    private static final String DEFAULT_CATEGORY = "UNCATEGORIZED";

    private final TicketRepository ticketRepository;
    private final ClientRepository clientRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final StorageFile storageFile;
    private final ClientContextResolver clientContextResolver;
    private final ClientAccessGuard clientAccessGuard;

    public TicketService(
            TicketRepository ticketRepository,
            ClientRepository clientRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            AttachmentRepository attachmentRepository,
            StorageFile storageFile,
            ClientContextResolver clientContextResolver,
            ClientAccessGuard clientAccessGuard
    ) {
        this.ticketRepository = ticketRepository;
        this.clientRepository = clientRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
        this.storageFile = storageFile;
        this.clientContextResolver = clientContextResolver;
        this.clientAccessGuard = clientAccessGuard;
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> getAll(String term, int page, int size, String clientIdHeader, Authentication authentication) {
        UUID clientId = clientContextResolver.resolveRequired(clientIdHeader);
        clientAccessGuard.assertUserHasAccess(authentication, clientId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String searchTerm = term == null ? "" : term.trim();

        Page<Ticket> tickets = searchTerm.isBlank()
                ? ticketRepository.findByClientId(clientId, pageable)
                : ticketRepository.findByClientIdAndTitleContainingIgnoreCaseOrClientIdAndDescriptionContainingIgnoreCase(
                clientId,
                searchTerm,
                clientId,
                searchTerm,
                pageable
        );

        return tickets.map(this::toResponseWithAttachments);
    }

    @Transactional
    public TicketResponse assign(UUID id, TicketAssign assign, String clientIdHeader, Authentication authentication) {
        UUID clientId = clientContextResolver.resolveRequired(clientIdHeader);
        clientAccessGuard.assertUserHasAccess(authentication, clientId);

        Ticket ticket = findTicketInClient(id, clientId);
        var assignedUser = userRepository.findById(assign.agentId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        ticket.setAssigned(assignedUser);
        if (ticket.getStatus().equals(TicketStatus.OPEN)) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        ticketRepository.save(ticket);
        return toResponseWithAttachments(ticket);
    }

    @Transactional
    public GenericResponse create(TicketPostBody body, String clientIdHeader, Authentication authentication) {
        UUID clientId = clientContextResolver.resolveRequired(clientIdHeader);
        clientAccessGuard.assertUserHasAccess(authentication, clientId);

        if (body.clientId() != null && !body.clientId().equals(clientId)) {
            throw new BusinessException("CLIENT_MISMATCH", "Payload clientId differs from X-Client-Id", HttpStatus.BAD_REQUEST);
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException("CLIENT_NOT_FOUND", "Client not found", HttpStatus.NOT_FOUND));

        Ticket ticket = new Ticket();
        ticket.setTitle(body.title());
        ticket.setDescription(body.description());
        ticket.setPriority(resolvePriority(body.priority()));
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCategory(resolveCategory(body.categoryId(), clientId));
        ticket.setClient(client);
        ticket.setRequester(userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND)));

        ticketRepository.save(ticket);
        saveAttachments(ticket, body.files());
        return new GenericResponse("Ticket created successfully!");
    }

    @Transactional
    public GenericResponse update(UUID id, TicketPostBody body, String clientIdHeader, Authentication authentication) {
        UUID clientId = clientContextResolver.resolveRequired(clientIdHeader);
        clientAccessGuard.assertUserHasAccess(authentication, clientId);

        Ticket currentTicket = findTicketInClient(id, clientId);
        if (!TicketStatus.OPEN.equals(currentTicket.getStatus())) {
            throw new BusinessException("INVALID_STATUS", "Only OPEN tickets can be updated", HttpStatus.BAD_REQUEST);
        }

        currentTicket.setTitle(body.title());
        currentTicket.setDescription(body.description());
        currentTicket.setPriority(resolvePriority(body.priority()));
        currentTicket.setCategory(resolveCategory(body.categoryId(), clientId));

        ticketRepository.save(currentTicket);
        return new GenericResponse("Ticket updated successfully!");
    }

    @Transactional
    public GenericResponse changeStatus(UUID id, TicketStatus status, String clientIdHeader, Authentication authentication) {
        UUID clientId = clientContextResolver.resolveRequired(clientIdHeader);
        clientAccessGuard.assertUserHasAccess(authentication, clientId);

        Ticket currentTicket = findTicketInClient(id, clientId);
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

    private Ticket findTicketInClient(UUID ticketId, UUID clientId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("TICKET_NOT_FOUND", "Ticket not found", HttpStatus.NOT_FOUND));
        if (!ticket.getClient().getId().equals(clientId)) {
            throw new BusinessException("ACCESS_DENIED_CLIENT", "Ticket does not belong to this client", HttpStatus.FORBIDDEN);
        }
        return ticket;
    }

    private TicketResponse toResponseWithAttachments(Ticket ticket) {
        var attachments = attachmentRepository.findByOwnerIdAndOwnerType(ticket.getId(), AttachmentOwnerType.TICKET);
        return TicketMapper.toTicketResponse(ticket, attachments);
    }

    private void saveAttachments(Ticket ticket, List<String> files) {
        List<String> safeFiles = files == null ? List.of() : files;
        if (safeFiles.isEmpty()) {
            return;
        }

        safeFiles.forEach(item -> {
            String contentType = Base64Utils.resolveContentType(item);
            long sizeBytes = storageFile.decodeBase64(item).length;
            String file = storageFile.uploadFile(item, "tickets/" + ticket.getId() + "/", contentType);

            Attachment attachment = new Attachment();
            attachment.setOwnerId(ticket.getId());
            attachment.setOwnerType(AttachmentOwnerType.TICKET);
            attachment.setStoragePath(file);
            attachment.setContentType(contentType);
            attachment.setSizeBytes(sizeBytes);
            attachment.setFileName(Base64Utils.resolveFileName(file));
            attachmentRepository.save(attachment);
        });
    }

    private Category resolveCategory(Optional<UUID> categoryId, UUID clientId) {
        if (categoryId != null && categoryId.isPresent()) {
            Category category = categoryRepository.findById(categoryId.get())
                    .orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND));

            if (!category.isGlobal() && (category.getClient() == null || !category.getClient().getId().equals(clientId))) {
                throw new BusinessException("ACCESS_DENIED_CLIENT", "Category does not belong to this client", HttpStatus.FORBIDDEN);
            }
            return category;
        }

        return categoryRepository.findByTitleAndGlobalTrue(DEFAULT_CATEGORY)
                .orElseThrow(() -> new BusinessException(
                        "DEFAULT_CATEGORY_NOT_FOUND",
                        "Default global category UNCATEGORIZED not found",
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

}
