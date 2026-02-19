package br.com.supportflow.SupportFlow.ticket.service;

import br.com.supportflow.SupportFlow.common.exception.BusinessException;
import br.com.supportflow.SupportFlow.common.security.ClientAccessGuard;
import br.com.supportflow.SupportFlow.common.security.ClientContextResolver;
import br.com.supportflow.SupportFlow.ticket.dto.CommentCreateBody;
import br.com.supportflow.SupportFlow.ticket.dto.CommentResponse;
import br.com.supportflow.SupportFlow.ticket.entity.Comment;
import br.com.supportflow.SupportFlow.ticket.mapper.TicketMapper;
import br.com.supportflow.SupportFlow.ticket.repository.CommentRepository;
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

import java.util.UUID;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ClientContextResolver clientContextResolver;
    private final ClientAccessGuard clientAccessGuard;

    public CommentService(
            CommentRepository commentRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository,
            ClientContextResolver clientContextResolver,
            ClientAccessGuard clientAccessGuard
    ) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.clientContextResolver = clientContextResolver;
        this.clientAccessGuard = clientAccessGuard;
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> listByTicket(
            UUID ticketId,
            String term,
            int page,
            int size,
            String clientIdHeader,
            Authentication authentication
    ) {
        UUID clientId = clientContextResolver.resolveRequired(clientIdHeader);
        clientAccessGuard.assertUserHasAccess(authentication, clientId);

        var ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("TICKET_NOT_FOUND", "Ticket not found", HttpStatus.NOT_FOUND));

        if (!ticket.getClient().getId().equals(clientId)) {
            throw new BusinessException("ACCESS_DENIED_CLIENT", "Ticket does not belong to this client", HttpStatus.FORBIDDEN);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String searchTerm = term == null ? "" : term.trim();

        Page<Comment> comments = searchTerm.isBlank()
                ? commentRepository.findByTicketId(ticketId, pageable)
                : commentRepository.findByTicketIdAndMessageContainingIgnoreCase(ticketId, searchTerm, pageable);

        return comments.map(TicketMapper::toCommentResponse);
    }

    @Transactional
    public CommentResponse create(
            UUID ticketId,
            CommentCreateBody body,
            String clientIdHeader,
            Authentication authentication
    ) {
        UUID clientId = clientContextResolver.resolveRequired(clientIdHeader);
        clientAccessGuard.assertUserHasAccess(authentication, clientId);

        var ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("TICKET_NOT_FOUND", "Ticket not found", HttpStatus.NOT_FOUND));

        if (!ticket.getClient().getId().equals(clientId)) {
            throw new BusinessException("ACCESS_DENIED_CLIENT", "Ticket does not belong to this client", HttpStatus.FORBIDDEN);
        }

        var author = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setMessage(body.message().trim());

        return TicketMapper.toCommentResponse(commentRepository.save(comment));
    }
}
