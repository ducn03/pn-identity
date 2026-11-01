package com.premiernuts.identity.features.log.controller;

import com.premiernuts.identity.core.aop.LogActivity;
import com.premiernuts.identity.core.dto.PagingDTO;
import com.premiernuts.identity.core.dto.ResponseData;
import com.premiernuts.identity.core.enums.ERole;
import com.premiernuts.identity.core.utils.ResponseUtils;
import com.premiernuts.identity.features.log.constants.LogEndpoints;
import com.premiernuts.identity.features.log.dto.LogSearchDTO;
import com.premiernuts.identity.features.log.dto.LoginLogDTO;
import com.premiernuts.identity.features.log.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(LogEndpoints.BASE)
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping(LogEndpoints.LOGIN_LOGS)
    @LogActivity(requireAuth = true, roles = {ERole.Names.SUPER_ADMIN, ERole.Names.ROLE_ADMIN})
    public ResponseEntity<ResponseData<List<LoginLogDTO>>> getLoginLogs(@ModelAttribute LogSearchDTO searchDTO) {
        PageRequest pageRequest = PageRequest.of(
                searchDTO.getNormalizedPage(),
                searchDTO.getNormalizedSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<LoginLogDTO> logPage = logService.getLoginLogs(searchDTO, pageRequest);
        PagingDTO paging = new PagingDTO();
        paging.update(logPage);

        return ResponseUtils.toSuccessArray(logPage.getContent(), paging);
    }
}
