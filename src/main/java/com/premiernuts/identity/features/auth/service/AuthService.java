package com.premiernuts.identity.features.auth.service;

import com.premiernuts.identity.core.security.UserPrincipal;
import com.premiernuts.identity.features.auth.dto.LoginRequestDTO;
import com.premiernuts.identity.features.auth.dto.LoginResponseDTO;
import com.premiernuts.identity.features.auth.dto.RefreshTokenRequestDTO;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO request, String ipAddress, String userAgent);

    LoginResponseDTO refreshToken(RefreshTokenRequestDTO request);

    void logout(Long userId);

    UserPrincipal getMe(Long userId);
}
