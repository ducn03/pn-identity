package com.premiernuts.identity.core.aop;

import com.premiernuts.identity.core.constants.SecurityConstants;
import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.core.security.JwtHelper;
import com.premiernuts.identity.core.security.UserContext;
import com.premiernuts.identity.core.security.UserPrincipal;
import com.premiernuts.identity.core.utils.DateTimeHelper;
import com.premiernuts.identity.core.utils.JsonHelper;
import com.premiernuts.identity.core.utils.StringHelper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Aspect
@Order(1)
@Component
public class LogActivityAspect {

    private final JwtHelper jwtHelper;

    public LogActivityAspect(JwtHelper jwtHelper) {
        this.jwtHelper = jwtHelper;
    }

    @Around("@annotation(logActivity)")
    public Object handle(ProceedingJoinPoint point, LogActivity logActivity) throws Throwable {
        long startTime = DateTimeHelper.currentTimeMillis();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }

        String path = "UNKNOWN";
        String method = "UNKNOWN";
        if (request != null) {
            path = request.getRequestURI();
            method = request.getMethod();
        }

        try {
            if (logActivity.requireAuth() && request != null) {
                authorizeRequest(request, logActivity.roles());
            }

            Object result = point.proceed();
            long duration = DateTimeHelper.currentTimeMillis() - startTime;

            log.info("[LogActivity] [{} {}] duration={}ms | response={}",
                    method, path, duration, JsonHelper.toJson(result));

            return result;
        } catch (PnException e) {
            log.warn("[LogActivity] [{} {}] PnException: [code={}] {}", method, path, e.getErrorCode(), e.getErrorMessage());
            throw e;
        } catch (Throwable t) {
            log.error("[LogActivity] [{} {}] Unhandled error: {}", method, path, t.getMessage());
            throw t;
        } finally {
            UserContext.clear();
        }
    }

    private void authorizeRequest(HttpServletRequest request, String[] requiredRoles) {
        String authHeader = request.getHeader(SecurityConstants.HEADER_AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            throw new PnException(ErrorCodes.UNAUTHORIZED.UNAUTHORIZED);
        }

        String token = StringHelper.trim(authHeader.substring(SecurityConstants.BEARER_PREFIX.length()));
        try {
            Long userId = jwtHelper.extractUserId(token);
            String username = jwtHelper.extractUsername(token);
            Long companyId = jwtHelper.extractCompanyId(token);
            List<String> roles = jwtHelper.extractRoles(token);
            List<String> permissions = jwtHelper.extractPermissions(token);

            if (userId == null || username == null) {
                throw new PnException(ErrorCodes.UNAUTHORIZED.TOKEN_INVALID);
            }

            // Kiểm tra vai trò nếu được chỉ định
            if (requiredRoles != null && requiredRoles.length > 0) {
                boolean hasRole = false;
                if (roles != null) {
                    for (String requiredRole : requiredRoles) {
                        if (requiredRole != null && roles.contains(requiredRole)) {
                            hasRole = true;
                            break;
                        }
                    }
                }
                if (!hasRole) {
                    throw new PnException(ErrorCodes.FORBIDDEN.ACCESS_DENIED);
                }
            }

            UserPrincipal principal = new UserPrincipal(userId, username, companyId, roles, permissions);

            UserContext.set(principal);
            request.setAttribute(SecurityConstants.REQ_ATTR_USER_PRINCIPAL, principal);
            request.setAttribute(SecurityConstants.REQ_ATTR_USER_ID, userId);
        } catch (ExpiredJwtException e) {
            throw new PnException(ErrorCodes.UNAUTHORIZED.TOKEN_EXPIRED);
        } catch (PnException e) {
            throw e;
        } catch (Exception e) {
            throw new PnException(ErrorCodes.UNAUTHORIZED.TOKEN_INVALID);
        }
    }
}
