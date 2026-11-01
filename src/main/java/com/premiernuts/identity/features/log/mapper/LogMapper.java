package com.premiernuts.identity.features.log.mapper;

import com.premiernuts.identity.features.log.dto.LoginLogDTO;
import com.premiernuts.identity.jpa.domain.auth.LoginLog;
import org.springframework.stereotype.Component;

@Component
public class LogMapper {

    public LoginLogDTO toDTO(LoginLog log) {
        if (log == null) {
            return null;
        }

        LoginLogDTO dto = new LoginLogDTO();
        dto.setId(log.getId());
        dto.setUserId(log.getUserId());
        dto.setUsername(log.getUsername());
        dto.setIpAddress(log.getIpAddress());
        dto.setUserAgent(log.getUserAgent());
        dto.setFailureReason(log.getFailureReason());
        dto.setStatus(log.getStatus());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
