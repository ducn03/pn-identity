package com.premiernuts.identity.features.auth.controller;

import com.premiernuts.identity.core.aop.LogActivity;
import com.premiernuts.identity.core.constants.CommonConstants;
import com.premiernuts.identity.core.dto.ResponseData;
import com.premiernuts.identity.core.security.UserContext;
import com.premiernuts.identity.core.security.UserPrincipal;
import com.premiernuts.identity.core.utils.MessageHelper;
import com.premiernuts.identity.core.utils.ResponseUtils;
import com.premiernuts.identity.features.auth.dto.LoginRequestDTO;
import com.premiernuts.identity.features.auth.dto.LoginResponseDTO;
import com.premiernuts.identity.features.auth.dto.RefreshTokenRequestDTO;
import com.premiernuts.identity.features.auth.service.AuthService;
import com.premiernuts.identity.features.auth.validator.AuthValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthValidator authValidator;

    @PostMapping("/login")
    @LogActivity(requireAuth = false)
    public ResponseEntity<ResponseData<LoginResponseDTO>> login(
            @RequestBody LoginRequestDTO request,
            HttpServletRequest servletRequest
    ) {
        authValidator.validateLogin(request);
        String ipAddress = extractClientIp(servletRequest);
        String userAgent = servletRequest.getHeader("User-Agent");
        LoginResponseDTO response = authService.login(request, ipAddress, userAgent);
        return ResponseUtils.toSuccess(response);
    }

    @PostMapping("/refresh")
    @LogActivity(requireAuth = false)
    public ResponseEntity<ResponseData<LoginResponseDTO>> refreshToken(
            @RequestBody RefreshTokenRequestDTO request
    ) {
        authValidator.validateRefreshToken(request);
        LoginResponseDTO response = authService.refreshToken(request);
        return ResponseUtils.toSuccess(response);
    }

    @PostMapping("/logout")
    @LogActivity(requireAuth = true)
    public ResponseEntity<ResponseData<String>> logout() {
        UserPrincipal principal = UserContext.get();
        if (principal != null) {
            authService.logout(principal.getUserId());
        }
        return ResponseUtils.toSuccess(MessageHelper.getMessage(CommonConstants.MSG_LOGOUT_SUCCESS));
    }

    @GetMapping("/me")
    @LogActivity(requireAuth = true)
    public ResponseEntity<ResponseData<UserPrincipal>> getMe() {
        UserPrincipal principal = UserContext.get();
        if (principal != null) {
            UserPrincipal freshPrincipal = authService.getMe(principal.getUserId());
            return ResponseUtils.toSuccess(freshPrincipal);
        }
        return ResponseUtils.toSuccess(null);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
