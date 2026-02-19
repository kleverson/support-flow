package br.com.supportflow.SupportFlow.ticket.mapper;

import br.com.supportflow.SupportFlow.ticket.dto.AttachmentResponse;
import br.com.supportflow.SupportFlow.ticket.dto.CategoryResponse;
import br.com.supportflow.SupportFlow.ticket.dto.CommentResponse;
import br.com.supportflow.SupportFlow.ticket.dto.TicketResponse;
import br.com.supportflow.SupportFlow.ticket.dto.UserSummaryResponse;
import br.com.supportflow.SupportFlow.ticket.entity.Attachment;
import br.com.supportflow.SupportFlow.ticket.entity.Category;
import br.com.supportflow.SupportFlow.ticket.entity.Comment;
import br.com.supportflow.SupportFlow.ticket.entity.Ticket;
import br.com.supportflow.SupportFlow.user.entity.User;

import java.util.List;

public final class TicketMapper {
    private TicketMapper() {
    }

    public static TicketResponse toTicketResponse(Ticket ticket, List<Attachment> files) {
        if (ticket == null) {
            return null;
        }

        List<CommentResponse> comments = ticket.getComments() == null
                ? List.of()
                : ticket.getComments().stream().map(TicketMapper::toCommentResponse).toList();

        List<AttachmentResponse> attachments = files == null
                ? List.of()
                : files.stream().map(TicketMapper::toAttachmentResponse).toList();

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                toCategoryResponse(ticket.getCategory()),
                toUserSummary(ticket.getRequester()),
                toUserSummary(ticket.getAssigned()),
                comments,
                attachments,
                ticket.getCreatedAt(),
                ticket.getClosedAt(),
                ticket.getUpdatedAt()
        );
    }

    public static CommentResponse toCommentResponse(Comment comment) {
        if (comment == null) {
            return null;
        }

        return new CommentResponse(
                comment.getId(),
                comment.getTicket() != null ? comment.getTicket().getId() : null,
                comment.getMessage(),
                toUserSummary(comment.getAuthor()),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    public static CategoryResponse toCategoryResponse(Category category) {
        if (category == null) {
            return null;
        }

        return new CategoryResponse(category.getId(), category.getTitle());
    }

    public static UserSummaryResponse toUserSummary(User user) {
        if (user == null) {
            return null;
        }

        return new UserSummaryResponse(user.getId(), user.getName(), user.getEmail());
    }

    public static AttachmentResponse toAttachmentResponse(Attachment attachment) {
        if (attachment == null) {
            return null;
        }

        return new AttachmentResponse(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getStoragePath(),
                attachment.getCreatedAt()
        );
    }
}
