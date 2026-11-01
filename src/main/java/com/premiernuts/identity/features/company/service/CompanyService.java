package com.premiernuts.identity.features.company.service;

import com.premiernuts.identity.features.company.dto.CompanyDTO;
import com.premiernuts.identity.features.company.dto.DepartmentDTO;
import com.premiernuts.identity.features.company.dto.UpdateCompanyDTO;

import java.util.List;

public interface CompanyService {

    CompanyDTO getCompanyById(Long id);

    CompanyDTO updateCompany(Long id, UpdateCompanyDTO request);

    List<DepartmentDTO> getDepartments(Long companyId);
}
