package com.premiernuts.identity.jpa.repository.auth;

import com.premiernuts.identity.jpa.domain.auth.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenAndStatus(String token, String status);

    void deleteAllByUserId(Long userId);
}
