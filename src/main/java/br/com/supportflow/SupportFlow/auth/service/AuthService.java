package br.com.supportflow.SupportFlow.auth.service;

import br.com.supportflow.SupportFlow.auth.dto.AuthResponse;
import br.com.supportflow.SupportFlow.auth.dto.LoginRequest;
import br.com.supportflow.SupportFlow.auth.dto.MeResponse;
import br.com.supportflow.SupportFlow.auth.jwt.JwtTokenProvider;
import br.com.supportflow.SupportFlow.common.exception.BusinessException;
import br.com.supportflow.SupportFlow.user.entity.User;
import br.com.supportflow.SupportFlow.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            AuthenticationManager authenticationManager,
            RefreshTokenService refreshTokenService,
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
        } catch (DisabledException ex) {
            throw new BusinessException("USER_INACTIVE", "Usuario inativo", HttpStatus.FORBIDDEN);
        } catch (AuthenticationException ex) {
            throw new BusinessException("INVALID_CREDENTIALS", "Email ou senha invalidos", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Usuario nao encontrado", HttpStatus.NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException("USER_INACTIVE", "Usuario inativo", HttpStatus.FORBIDDEN);
        }

        String access = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name());
        String refresh = refreshTokenService.issue(user.getId());

        return AuthResponse.from(access, refresh, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse refresh(String refreshTokenRaw) {
        UUID userId = refreshTokenService.validateAndRotate(refreshTokenRaw);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Usuario nao encontrado", HttpStatus.NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException("USER_INACTIVE", "Usuario inativo", HttpStatus.FORBIDDEN);
        }

        String access = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name());
        String newRefresh = refreshTokenService.issue(user.getId());
        return AuthResponse.from(access, newRefresh, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public void logout(String refreshTokenRaw) {
        refreshTokenService.revoke(refreshTokenRaw);
    }

    public MeResponse me(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("UNAUTHORIZED", "Usuario nao autenticado", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Usuario nao encontrado", HttpStatus.NOT_FOUND));

        return MeResponse.from(user);
    }
}
