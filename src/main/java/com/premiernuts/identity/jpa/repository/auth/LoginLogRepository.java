package com.premiernuts.identity.jpa.repository.auth;

import com.premiernuts.identity.jpa.domain.auth.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    List<LoginLog> findAllByUserId(Long userId);
}
