package com.premiernuts.identity.features.auth.service;

import com.premiernuts.identity.jpa.domain.auth.Token;

import java.time.Instant;
import java.util.Optional;

public interface TokenStorageService {

    void saveRefreshToken(Long userId, String token, Instant expiredAt);

    Optional<Token> findActiveRefreshToken(String token);

    void revokeAllUserTokens(Long userId);

    void expireToken(Token token);
}
