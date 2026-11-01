package com.premiernuts.identity.features.log.service.impl;

import com.premiernuts.identity.features.log.dto.LogSearchDTO;
import com.premiernuts.identity.features.log.dto.LoginLogDTO;
import com.premiernuts.identity.features.log.mapper.LogMapper;
import com.premiernuts.identity.features.log.service.LogService;
import com.premiernuts.identity.jpa.domain.auth.LoginLog;
import com.premiernuts.identity.jpa.repository.auth.LoginLogRepository;
import com.premiernuts.identity.features.log.spec.LoginLogSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LoginLogRepository loginLogRepository;
    private final LogMapper logMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<LoginLogDTO> getLoginLogs(LogSearchDTO searchDTO, Pageable pageable) {
        Specification<LoginLog> spec = LoginLogSpecification.filter(searchDTO);
        Page<LoginLog> page = loginLogRepository.findAll(spec, pageable);
        return page.map(logMapper::toDTO);
    }
}
