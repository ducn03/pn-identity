package com.premiernuts.identity.features.company.service;

import com.premiernuts.identity.features.company.dto.CompanyDTO;
import com.premiernuts.identity.features.company.dto.DepartmentDTO;
import com.premiernuts.identity.features.company.dto.RoleDTO;

import java.util.List;

public interface CompanyService {

    CompanyDTO getCompanyById(Long id);

    List<DepartmentDTO> getDepartments(Long companyId);

    List<RoleDTO> getRoles();

    List<com.premiernuts.identity.features.company.dto.PermissionDTO> getPermissions();
}
