package com.premiernuts.identity.features.auth.service.impl;

import com.premiernuts.identity.core.constants.SecurityConstants;
import com.premiernuts.identity.core.enums.ELoginStatus;
import com.premiernuts.identity.core.enums.EStatus;
import com.premiernuts.identity.core.enums.ETokenType;
import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.core.security.JwtHelper;
import com.premiernuts.identity.core.security.UserPrincipal;
import com.premiernuts.identity.core.utils.PasswordHelper;
import com.premiernuts.identity.features.auth.dto.LoginRequestDTO;
import com.premiernuts.identity.features.auth.dto.LoginResponseDTO;
import com.premiernuts.identity.features.auth.dto.RefreshTokenRequestDTO;
import com.premiernuts.identity.features.auth.service.AuthService;
import com.premiernuts.identity.jpa.domain.auth.LoginLog;
import com.premiernuts.identity.jpa.domain.auth.Token;
import com.premiernuts.identity.jpa.domain.role.Role;
import com.premiernuts.identity.jpa.domain.user.User;
import com.premiernuts.identity.jpa.domain.user.UserRole;
import com.premiernuts.identity.jpa.repository.auth.LoginLogRepository;
import com.premiernuts.identity.jpa.repository.auth.TokenRepository;
import com.premiernuts.identity.jpa.repository.role.RoleRepository;
import com.premiernuts.identity.jpa.repository.user.UserRepository;
import com.premiernuts.identity.jpa.repository.user.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final TokenRepository tokenRepository;
    private final LoginLogRepository loginLogRepository;
    private final JwtHelper jwtHelper;

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request, String ipAddress, String userAgent) {
        String username = request.getUsername().trim();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            recordLoginLog(null, username, ipAddress, userAgent, ELoginStatus.FAILED.name(), "User not found");
            throw new PnException(ErrorCodes.AUTH.USER_NOT_FOUND);
        }

        User user = userOpt.get();

        if (!PasswordHelper.matches(request.getPassword(), user.getPasswordHash())) {
            recordLoginLog(user.getId(), username, ipAddress, userAgent, ELoginStatus.FAILED.name(), "Password incorrect");
            throw new PnException(ErrorCodes.AUTH.PASSWORD_INCORRECT);
        }

        if (!EStatus.ACTIVE.name().equalsIgnoreCase(user.getStatus())) {
            recordLoginLog(user.getId(), username, ipAddress, userAgent, ELoginStatus.FAILED.name(), "Account locked or inactive");
            throw new PnException(ErrorCodes.AUTH.ACCOUNT_LOCKED);
        }

        // Lấy danh sách vai trò
        List<String> roles = getUserRoles(user.getId());

        // Sinh cặp Token
        String accessToken = jwtHelper.generateAccessToken(user.getId(), user.getUsername(), user.getCompanyId(), roles);
        String refreshToken = jwtHelper.generateRefreshToken(user.getId(), user.getUsername());

        // Lưu Refresh Token vào T_TOKEN
        Token tokenEntity = new Token();
        tokenEntity.setUserId(user.getId());
        tokenEntity.setToken(refreshToken);
        tokenEntity.setTokenType(ETokenType.REFRESH.name());
        tokenEntity.setExpiredAt(Instant.now().plus(SecurityConstants.REFRESH_TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS));
        tokenEntity.setStatus(EStatus.ACTIVE.name());
        tokenRepository.save(tokenEntity);

        // Cập nhật last login & ghi log
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        recordLoginLog(user.getId(), username, ipAddress, userAgent, ELoginStatus.SUCCESS.name(), null);

        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), user.getCompanyId(), roles);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType(SecurityConstants.TOKEN_TYPE_BEARER);
        response.setExpiresIn(SecurityConstants.ACCESS_TOKEN_EXPIRATION_SECONDS);
        response.setUser(principal);

        return response;
    }

    @Override
    @Transactional
    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String tokenStr = request.getRefreshToken();
        if (!jwtHelper.validateToken(tokenStr)) {
            throw new PnException(ErrorCodes.UNAUTHORIZED.TOKEN_INVALID);
        }

        Optional<Token> tokenOpt = tokenRepository.findByTokenAndStatus(tokenStr, EStatus.ACTIVE.name());
        if (tokenOpt.isEmpty()) {
            throw new PnException(ErrorCodes.AUTH.TOKEN_REVOKED);
        }

        Token token = tokenOpt.get();
        if (token.getExpiredAt().isBefore(Instant.now())) {
            token.setStatus(EStatus.INACTIVE.name());
            tokenRepository.save(token);
            throw new PnException(ErrorCodes.UNAUTHORIZED.TOKEN_EXPIRED);
        }

        Optional<User> userOpt = userRepository.findById(token.getUserId());
        if (userOpt.isEmpty()) {
            throw new PnException(ErrorCodes.AUTH.USER_NOT_FOUND);
        }

        User user = userOpt.get();
        List<String> roles = getUserRoles(user.getId());
        String newAccessToken = jwtHelper.generateAccessToken(user.getId(), user.getUsername(), user.getCompanyId(), roles);

        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), user.getCompanyId(), roles);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(tokenStr);
        response.setTokenType(SecurityConstants.TOKEN_TYPE_BEARER);
        response.setExpiresIn(SecurityConstants.ACCESS_TOKEN_EXPIRATION_SECONDS);
        response.setUser(principal);

        return response;
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        if (userId != null) {
            tokenRepository.deleteAllByUserId(userId);
            log.info("Logged out user id: {}", userId);
        }
    }

    @Override
    public UserPrincipal getMe(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new PnException(ErrorCodes.AUTH.USER_NOT_FOUND);
        }

        User user = userOpt.get();
        List<String> roles = getUserRoles(user.getId());

        return new UserPrincipal(user.getId(), user.getUsername(), user.getCompanyId(), roles);
    }

    private List<String> getUserRoles(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findAllByUserId(userId);
        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
        List<Role> roles = roleRepository.findAllByIdIn(roleIds);
        return roles.stream().map(Role::getCode).toList();
    }

    private void recordLoginLog(Long userId, String username, String ip, String ua, String status, String reason) {
        LoginLog logEntity = new LoginLog();
        logEntity.setUserId(userId);
        logEntity.setUsername(username);
        logEntity.setIpAddress(ip);
        logEntity.setUserAgent(ua);
        logEntity.setStatus(status);
        logEntity.setFailureReason(reason);
        loginLogRepository.save(logEntity);
    }
}
