package br.com.supportflow.SupportFlow.common.security;

import br.com.supportflow.SupportFlow.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ClientContextResolver {
    public UUID resolveRequired(String clientIdHeader) {
        if (clientIdHeader == null || clientIdHeader.isBlank()) {
            throw new BusinessException("CLIENT_CONTEXT_REQUIRED", "X-Client-Id header is required", HttpStatus.BAD_REQUEST);
        }
        try {
            return UUID.fromString(clientIdHeader.trim());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("CLIENT_CONTEXT_INVALID", "X-Client-Id is invalid", HttpStatus.BAD_REQUEST);
        }
    }
}
