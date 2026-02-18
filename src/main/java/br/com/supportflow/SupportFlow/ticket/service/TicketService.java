package br.com.supportflow.SupportFlow.ticket.service;

import br.com.supportflow.SupportFlow.client.repository.ClientRepository;
import br.com.supportflow.SupportFlow.common.dto.GenericResponse;
import br.com.supportflow.SupportFlow.common.exception.BusinessException;
import br.com.supportflow.SupportFlow.ticket.dto.TicketAssign;
import br.com.supportflow.SupportFlow.ticket.dto.TicketPostBody;
import br.com.supportflow.SupportFlow.ticket.dto.TicketPutBody;
import br.com.supportflow.SupportFlow.ticket.dto.TicketResponse;
import br.com.supportflow.SupportFlow.ticket.entity.Category;
import br.com.supportflow.SupportFlow.ticket.entity.Ticket;
import br.com.supportflow.SupportFlow.ticket.entity.TicketPriority;
import br.com.supportflow.SupportFlow.ticket.entity.TicketStatus;
import br.com.supportflow.SupportFlow.ticket.mapper.TicketMapper;
import br.com.supportflow.SupportFlow.ticket.repository.CategoryRepository;
import br.com.supportflow.SupportFlow.ticket.repository.TicketRepository;
import br.com.supportflow.SupportFlow.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

    public TicketService(
            TicketRepository ticketRepository,
            ClientRepository clientRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.clientRepository = clientRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
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

        return tickets.map(TicketMapper::toTicketResponse);
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

        return TicketMapper.toTicketResponse(ticket);
    }

    @Transactional
    public GenericResponse create(TicketPostBody body, UserDetails userDetails) {
        Ticket ticket = new Ticket();
        ticket.setTitle(body.title());
        ticket.setDescription(body.description());
        ticket.setPriority(resolvePriority(body.priority()));
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCategory(resolveCategory(body.categoryId()));
        ticket.setClient(clientRepository.findById(body.clientId())
                .orElseThrow(() -> new BusinessException("CLIENT_NOT_FOUND", "Client not found", HttpStatus.NOT_FOUND)));

        if (userDetails == null || userDetails.getUsername() == null || userDetails.getUsername().isBlank()) {
            throw new BusinessException("UNAUTHORIZED", "User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        ticket.setRequester(userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND)));

        ticketRepository.save(ticket);
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
}
