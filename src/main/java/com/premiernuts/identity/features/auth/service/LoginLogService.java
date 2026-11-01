package com.premiernuts.identity.features.auth.service;

public interface LoginLogService {

    void recordLoginLog(Long userId, String username, String ip, String ua, String status, String reason);
}
