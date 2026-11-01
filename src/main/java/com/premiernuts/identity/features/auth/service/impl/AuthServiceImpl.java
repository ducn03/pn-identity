package com.premiernuts.identity.features.auth.service.impl;

import com.premiernuts.identity.core.constants.SecurityConstants;
import com.premiernuts.identity.core.enums.ELoginStatus;
import com.premiernuts.identity.core.enums.ERole;
import com.premiernuts.identity.core.enums.EStatus;
import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.core.security.JwtHelper;
import com.premiernuts.identity.core.security.UserPrincipal;
import com.premiernuts.identity.core.utils.DateTimeHelper;
import com.premiernuts.identity.core.utils.PasswordHelper;
import com.premiernuts.identity.core.utils.StringHelper;
import com.premiernuts.identity.features.auth.dto.LoginRequestDTO;
import com.premiernuts.identity.features.auth.dto.LoginResponseDTO;
import com.premiernuts.identity.features.auth.dto.RefreshTokenRequestDTO;
import com.premiernuts.identity.features.auth.service.AuthService;
import com.premiernuts.identity.features.auth.service.TokenStorageService;
import com.premiernuts.identity.jpa.domain.auth.LoginLog;
import com.premiernuts.identity.jpa.domain.auth.Token;
import com.premiernuts.identity.jpa.domain.role.Permission;
import com.premiernuts.identity.jpa.domain.role.Role;
import com.premiernuts.identity.jpa.domain.user.User;
import com.premiernuts.identity.jpa.domain.user.UserPermission;
import com.premiernuts.identity.jpa.repository.auth.LoginLogRepository;
import com.premiernuts.identity.jpa.repository.role.PermissionRepository;
import com.premiernuts.identity.jpa.repository.role.RoleRepository;
import com.premiernuts.identity.jpa.repository.user.UserPermissionRepository;
import com.premiernuts.identity.jpa.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final TokenStorageService tokenStorageService;
    private final LoginLogRepository loginLogRepository;
    private final JwtHelper jwtHelper;

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request, String ipAddress, String userAgent) {
        String username = StringHelper.trim(request.getUsername());
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

        // Lấy vai trò chức danh duy nhất và danh sách quyền hạn
        String roleCode = getUserRoleCode(user);
        List<String> roles;
        if (roleCode != null) {
            roles = List.of(roleCode);
        } else {
            roles = Collections.emptyList();
        }
        List<String> permissions = getUserPermissions(user.getId(), roleCode);

        // Sinh cặp Token chứa role và permissions
        String accessToken = jwtHelper.generateAccessToken(user.getId(), user.getUsername(), user.getCompanyId(), roles, permissions);
        String refreshToken = jwtHelper.generateRefreshToken(user.getId(), user.getUsername());

        // Lưu Refresh Token qua TokenStorageService
        Instant expiredAt = DateTimeHelper.plusDays(SecurityConstants.REFRESH_TOKEN_EXPIRATION_DAYS);
        tokenStorageService.saveRefreshToken(user.getId(), refreshToken, expiredAt);

        // Cập nhật last login & ghi log
        user.setLastLoginAt(DateTimeHelper.now());
        userRepository.save(user);
        recordLoginLog(user.getId(), username, ipAddress, userAgent, ELoginStatus.SUCCESS.name(), null);

        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), user.getCompanyId(), roles, permissions);

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

        Optional<Token> tokenOpt = tokenStorageService.findActiveRefreshToken(tokenStr);
        if (tokenOpt.isEmpty()) {
            throw new PnException(ErrorCodes.AUTH.TOKEN_REVOKED);
        }

        Token token = tokenOpt.get();
        if (DateTimeHelper.isExpired(token.getExpiredAt())) {
            tokenStorageService.expireToken(token);
            throw new PnException(ErrorCodes.UNAUTHORIZED.TOKEN_EXPIRED);
        }

        Optional<User> userOpt = userRepository.findById(token.getUserId());
        if (userOpt.isEmpty()) {
            throw new PnException(ErrorCodes.AUTH.USER_NOT_FOUND);
        }

        User user = userOpt.get();
        String roleCode = getUserRoleCode(user);
        List<String> roles;
        if (roleCode != null) {
            roles = List.of(roleCode);
        } else {
            roles = Collections.emptyList();
        }
        List<String> permissions = getUserPermissions(user.getId(), roleCode);
        String newAccessToken = jwtHelper.generateAccessToken(user.getId(), user.getUsername(), user.getCompanyId(), roles, permissions);

        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), user.getCompanyId(), roles, permissions);

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
            tokenStorageService.revokeAllUserTokens(userId);
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
        String roleCode = getUserRoleCode(user);
        List<String> roles;
        if (roleCode != null) {
            roles = List.of(roleCode);
        } else {
            roles = Collections.emptyList();
        }
        List<String> permissions = getUserPermissions(user.getId(), roleCode);

        return new UserPrincipal(user.getId(), user.getUsername(), user.getCompanyId(), roles, permissions);
    }

    private String getUserRoleCode(User user) {
        if (user.getRoleId() == null) {
            return null;
        }
        Optional<Role> rOpt = roleRepository.findById(user.getRoleId());
        if (rOpt.isPresent()) {
            return rOpt.get().getCode();
        }
        return null;
    }

    private List<String> getUserPermissions(Long userId, String roleCode) {
        // Quản trị viên có toàn bộ quyền hệ thống
        if (ERole.ROLE_ADMIN.name().equals(roleCode) || ERole.SUPER_ADMIN.name().equals(roleCode)) {
            List<Permission> allPerms = permissionRepository.findAll();
            return allPerms.stream().map(Permission::getCode).toList();
        }

        // Lấy danh sách quyền hạn gán trực tiếp từ T_USER_PERMISSION
        List<UserPermission> userPerms = userPermissionRepository.findAllByUserId(userId);
        if (userPerms.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> permIds = userPerms.stream().map(UserPermission::getPermissionId).toList();
        List<Permission> perms = permissionRepository.findAllById(permIds);
        return perms.stream().map(Permission::getCode).toList();
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
