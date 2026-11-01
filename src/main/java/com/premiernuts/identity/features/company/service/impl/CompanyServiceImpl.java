package com.premiernuts.identity.features.company.service.impl;

import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.core.security.UserContext;
import com.premiernuts.identity.core.security.UserPrincipal;
import com.premiernuts.identity.features.company.dto.CompanyDTO;
import com.premiernuts.identity.features.company.dto.DepartmentDTO;
import com.premiernuts.identity.features.company.dto.UpdateCompanyDTO;
import com.premiernuts.identity.features.company.mapper.CompanyMapper;
import com.premiernuts.identity.features.company.service.CompanyService;
import com.premiernuts.identity.features.company.validator.CompanyValidator;
import com.premiernuts.identity.jpa.domain.company.Company;
import com.premiernuts.identity.jpa.domain.company.Department;
import com.premiernuts.identity.jpa.repository.company.CompanyRepository;
import com.premiernuts.identity.jpa.repository.company.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final CompanyMapper companyMapper;
    private final CompanyValidator companyValidator;

    @Override
    @Transactional(readOnly = true)
    public CompanyDTO getCompanyById(Long id) {
        Optional<Company> companyOpt = companyRepository.findById(id);
        if (companyOpt.isEmpty()) {
            throw new PnException(ErrorCodes.COMMON.NOT_FOUND);
        }
        return companyMapper.toDTO(companyOpt.get());
    }

    @Override
    @Transactional
    public CompanyDTO updateCompany(Long id, UpdateCompanyDTO request) {
        UserPrincipal principal = UserContext.get();
        companyValidator.validateUpdateCompany(principal, request);

        Optional<Company> companyOpt = companyRepository.findById(id);
        if (companyOpt.isEmpty()) {
            throw new PnException(ErrorCodes.COMMON.NOT_FOUND);
        }
        Company company = companyOpt.get();

        companyMapper.updateEntity(company, request);
        company = companyRepository.save(company);
        return companyMapper.toDTO(company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getDepartments(Long companyId) {
        List<Department> list;
        if (companyId != null) {
            list = departmentRepository.findAllByCompanyId(companyId);
        } else {
            list = departmentRepository.findAll();
        }

        List<DepartmentDTO> dtos = new ArrayList<>();
        for (Department d : list) {
            dtos.add(companyMapper.toDepartmentDTO(d));
        }
        return dtos;
    }
}
