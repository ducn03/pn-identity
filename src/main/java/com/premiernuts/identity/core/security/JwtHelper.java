package com.premiernuts.identity.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtHelper {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtHelper(
            @Value("${pn.jwt.secret-key:PremierNutsSecretKeyForJWTAuth2026SecureKeyWithAtLeast256BitsLong!}") String secretKey,
            @Value("${pn.jwt.access-token-expiration:86400000}") long accessTokenExpirationMs,
            @Value("${pn.jwt.refresh-token-expiration:604800000}") long refreshTokenExpirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(Long userId, String username, Long companyId, List<String> roles) {
        return generateAccessToken(userId, username, companyId, roles, java.util.Collections.emptyList());
    }

    public String generateAccessToken(Long userId, String username, Long companyId, List<String> roles, List<String> permissions) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("companyId", companyId)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("type", "ACCESS")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public UserPrincipal extractPrincipal(String token) {
        Claims claims = extractClaims(token);
        Number userIdNum = claims.get("userId", Number.class);
        Long userId = userIdNum != null ? userIdNum.longValue() : null;
        String username = claims.getSubject();
        Number companyIdNum = claims.get("companyId", Number.class);
        Long companyId = companyIdNum != null ? companyIdNum.longValue() : null;
        List<String> roles = claims.get("roles", List.class);
        List<String> permissions = claims.get("permissions", List.class);
        return new UserPrincipal(userId, username, companyId, roles, permissions);
    }

    public Long extractUserId(String token) {
        Claims claims = extractClaims(token);
        Number userIdNum = claims.get("userId", Number.class);
        return userIdNum != null ? userIdNum.longValue() : null;
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public Long extractCompanyId(String token) {
        Claims claims = extractClaims(token);
        Number companyIdNum = claims.get("companyId", Number.class);
        return companyIdNum != null ? companyIdNum.longValue() : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractClaims(token);
        return claims.get("roles", List.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        Claims claims = extractClaims(token);
        return claims.get("permissions", List.class);
    }
}
