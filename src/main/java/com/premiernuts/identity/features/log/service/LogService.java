package com.premiernuts.identity.features.log.service;

import com.premiernuts.identity.features.log.dto.LogSearchDTO;
import com.premiernuts.identity.features.log.dto.LoginLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LogService {

    Page<LoginLogDTO> getLoginLogs(LogSearchDTO searchDTO, Pageable pageable);
}
