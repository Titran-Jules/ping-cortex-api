package com.titran.pingcortex.services;

import com.titran.pingcortex.dto.request.RefreshToken;
import com.titran.pingcortex.exceptions.InvalidRefreshTokenException;
import com.titran.pingcortex.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final Duration refreshTokenValidity;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenValidity = Duration.ofDays(refreshTokenExpirationDays);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public String issue(UUID userId) {
        String rawToken = generateRawToken();
        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID(),
                userId,
                hash(rawToken),
                Instant.now().plus(refreshTokenValidity),
                false,
                Instant.now()
        );
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    public RotationResult validateAndRotate(String rawToken) {
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(InvalidRefreshTokenException::new);
        if (existing.revoked() || existing.expiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }
        refreshTokenRepository.revoke(existing.id());
        String newRawToken = issue(existing.userId());
        return new RotationResult(existing.userId(), newRawToken);
    }

    public void revoke(UUID expectedUserId, String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(token -> {
            if (!token.userId().equals(expectedUserId)) {
                throw new InvalidRefreshTokenException();
            }
            refreshTokenRepository.revoke(token.id());
        });
    }


    public record RotationResult(UUID userId, String newRawToken) {}
}
