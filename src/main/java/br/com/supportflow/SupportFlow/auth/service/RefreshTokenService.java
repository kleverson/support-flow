package br.com.supportflow.SupportFlow.auth.service;

import br.com.supportflow.SupportFlow.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RefreshTokenService {
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, RefreshTokenData> tokens = new ConcurrentHashMap<>();
    private final long refreshExpSeconds;

    public RefreshTokenService(@Value("${app.jwt.refresh-exp-seconds:2592000}") long refreshExpSeconds) {
        this.refreshExpSeconds = refreshExpSeconds;
    }

    public String issue(UUID userId) {
        byte[] data = new byte[48];
        secureRandom.nextBytes(data);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(data);
        String hash = sha256(rawToken);
        Instant expiresAt = Instant.now().plusSeconds(refreshExpSeconds);
        tokens.put(hash, new RefreshTokenData(userId, expiresAt, false));
        return rawToken;
    }

    public UUID validateAndRotate(String refreshTokenRaw) {
        String hash = sha256(refreshTokenRaw);
        RefreshTokenData token = tokens.get(hash);
        if (token == null || token.revoked()) {
            throw new BusinessException("INVALID_REFRESH_TOKEN", "Refresh token invalido", HttpStatus.UNAUTHORIZED);
        }
        if (token.expiresAt().isBefore(Instant.now())) {
            tokens.remove(hash);
            throw new BusinessException("EXPIRED_REFRESH_TOKEN", "Refresh token expirado", HttpStatus.UNAUTHORIZED);
        }

        tokens.put(hash, token.revoke());
        return token.userId();
    }

    public void revoke(String refreshTokenRaw) {
        String hash = sha256(refreshTokenRaw);
        RefreshTokenData token = tokens.get(hash);
        if (token != null) {
            tokens.put(hash, token.revoke());
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 nao disponivel", ex);
        }
    }

    private record RefreshTokenData(UUID userId, Instant expiresAt, boolean revoked) {
        private RefreshTokenData revoke() {
            return new RefreshTokenData(userId, expiresAt, true);
        }
    }
}
