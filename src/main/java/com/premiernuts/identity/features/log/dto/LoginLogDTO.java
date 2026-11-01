package com.premiernuts.identity.features.log.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class LoginLogDTO {

    private Long id;
    private Long userId;
    private String username;
    private String ipAddress;
    private String userAgent;
    private String failureReason;
    private String status;
    private Instant createdAt;
}
