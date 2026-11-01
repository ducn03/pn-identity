package com.premiernuts.identity.features.auth.service.impl;

import com.premiernuts.identity.features.auth.service.LoginLogService;
import com.premiernuts.identity.jpa.domain.auth.LoginLog;
import com.premiernuts.identity.jpa.repository.auth.LoginLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogRepository loginLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginLog(Long userId, String username, String ip, String ua, String status, String reason) {
        try {
            LoginLog logEntity = new LoginLog();
            logEntity.setUserId(userId);
            logEntity.setUsername(username);
            logEntity.setIpAddress(ip);
            logEntity.setUserAgent(ua);
            logEntity.setStatus(status);
            logEntity.setFailureReason(reason);
            loginLogRepository.save(logEntity);
        } catch (Exception e) {
            log.error("Failed to persist LoginLog: {}", e.getMessage(), e);
        }
    }
}
