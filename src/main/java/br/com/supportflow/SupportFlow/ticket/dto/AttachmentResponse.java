package br.com.supportflow.SupportFlow.ticket.dto;

import java.time.Instant;
import java.util.UUID;

public record AttachmentResponse(
        UUID id,
        String fileName,
        String contentType,
        Long sizeBytes,
        String storagePath,
        Instant createdAt
) {
}
