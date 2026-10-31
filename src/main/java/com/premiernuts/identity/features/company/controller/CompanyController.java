package com.premiernuts.identity.features.company.controller;

import com.premiernuts.identity.core.aop.LogActivity;
import com.premiernuts.identity.core.constants.CommonConstants;
import com.premiernuts.identity.core.dto.ResponseData;
import com.premiernuts.identity.core.security.UserContext;
import com.premiernuts.identity.core.security.UserPrincipal;
import com.premiernuts.identity.core.utils.ResponseUtils;
import com.premiernuts.identity.features.company.dto.CompanyDTO;
import com.premiernuts.identity.features.company.dto.DepartmentDTO;
import com.premiernuts.identity.features.company.dto.RoleDTO;
import com.premiernuts.identity.features.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.premiernuts.identity.features.company.constants.CompanyEndpoints;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(CompanyEndpoints.BASE)
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping(CompanyEndpoints.COMPANY)
    @LogActivity(requireAuth = true)
    public ResponseEntity<ResponseData<CompanyDTO>> getMyCompany() {
        UserPrincipal principal = UserContext.get();
        Long companyId = CommonConstants.DEFAULT_COMPANY_ID;
        if (principal != null && principal.getCompanyId() != null) {
            companyId = principal.getCompanyId();
        }

        CompanyDTO company = companyService.getCompanyById(companyId);
        return ResponseUtils.toSuccess(company);
    }

    @GetMapping(CompanyEndpoints.DEPARTMENTS)
    @LogActivity(requireAuth = true)
    public ResponseEntity<ResponseData<List<DepartmentDTO>>> getDepartments() {
        UserPrincipal principal = UserContext.get();
        Long companyId = null;
        if (principal != null) {
            companyId = principal.getCompanyId();
        }

        List<DepartmentDTO> list = companyService.getDepartments(companyId);
        return ResponseUtils.toSuccess(list);
    }

    @GetMapping(CompanyEndpoints.ROLES)
    @LogActivity(requireAuth = true)
    public ResponseEntity<ResponseData<List<RoleDTO>>> getRoles() {
        List<RoleDTO> list = companyService.getRoles();
        return ResponseUtils.toSuccess(list);
    }
}
