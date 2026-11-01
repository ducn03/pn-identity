package com.premiernuts.identity.features.log.service.impl;

import com.premiernuts.identity.core.utils.StringHelper;
import com.premiernuts.identity.features.log.dto.LogSearchDTO;
import com.premiernuts.identity.features.log.dto.LoginLogDTO;
import com.premiernuts.identity.features.log.service.LogService;
import com.premiernuts.identity.jpa.domain.auth.LoginLog;
import com.premiernuts.identity.jpa.repository.auth.LoginLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LoginLogRepository loginLogRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<LoginLogDTO> getLoginLogs(LogSearchDTO searchDTO, Pageable pageable) {
        Specification<LoginLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringHelper.isNotBlank(searchDTO.getUsername())) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + searchDTO.getUsername().trim().toLowerCase() + "%"));
            }

            if (StringHelper.isNotBlank(searchDTO.getStatus())) {
                predicates.add(cb.equal(root.get("status"), StringHelper.toUpperCase(searchDTO.getStatus())));
            }

            if (StringHelper.isNotBlank(searchDTO.getKeyword())) {
                String kw = "%" + searchDTO.getKeyword().trim().toLowerCase() + "%";
                Predicate userPredicate = cb.like(cb.lower(root.get("username")), kw);
                Predicate ipPredicate = cb.like(cb.lower(root.get("ipAddress")), kw);
                Predicate reasonPredicate = cb.like(cb.lower(root.get("failureReason")), kw);
                predicates.add(cb.or(userPredicate, ipPredicate, reasonPredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<LoginLog> page = loginLogRepository.findAll(spec, pageable);
        return page.map(l -> {
            LoginLogDTO dto = new LoginLogDTO();
            dto.setId(l.getId());
            dto.setUserId(l.getUserId());
            dto.setUsername(l.getUsername());
            dto.setIpAddress(l.getIpAddress());
            dto.setUserAgent(l.getUserAgent());
            dto.setFailureReason(l.getFailureReason());
            dto.setStatus(l.getStatus());
            dto.setCreatedAt(l.getCreatedAt());
            return dto;
        });
    }
}
