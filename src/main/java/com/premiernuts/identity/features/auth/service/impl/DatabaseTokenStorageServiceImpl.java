package com.premiernuts.identity.features.auth.service.impl;

import com.premiernuts.identity.core.enums.EStatus;
import com.premiernuts.identity.core.enums.ETokenType;
import com.premiernuts.identity.features.auth.service.TokenStorageService;
import com.premiernuts.identity.jpa.domain.auth.Token;
import com.premiernuts.identity.jpa.repository.auth.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseTokenStorageServiceImpl implements TokenStorageService {

    private final TokenRepository tokenRepository;

    @Override
    @Transactional
    public void saveRefreshToken(Long userId, String token, Instant expiredAt) {
        Token tokenEntity = new Token();
        tokenEntity.setUserId(userId);
        tokenEntity.setToken(token);
        tokenEntity.setTokenType(ETokenType.REFRESH.name());
        tokenEntity.setExpiredAt(expiredAt);
        tokenEntity.setStatus(EStatus.ACTIVE.name());
        tokenRepository.save(tokenEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Token> findActiveRefreshToken(String token) {
        return tokenRepository.findByTokenAndStatus(token, EStatus.ACTIVE.name());
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        if (userId != null) {
            tokenRepository.deleteAllByUserId(userId);
            log.info("Revoked all active tokens in database for user id: {}", userId);
        }
    }

    @Override
    @Transactional
    public void expireToken(Token token) {
        if (token != null) {
            token.setStatus(EStatus.INACTIVE.name());
            tokenRepository.save(token);
        }
    }
}
