package com.premiernuts.identity.features.company.service.impl;

import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.features.company.dto.CompanyDTO;
import com.premiernuts.identity.features.company.dto.DepartmentDTO;
import com.premiernuts.identity.features.company.dto.RoleDTO;
import com.premiernuts.identity.features.company.service.CompanyService;
import com.premiernuts.identity.jpa.domain.company.Company;
import com.premiernuts.identity.jpa.domain.company.Department;
import com.premiernuts.identity.jpa.domain.role.Role;
import com.premiernuts.identity.jpa.repository.company.CompanyRepository;
import com.premiernuts.identity.jpa.repository.company.DepartmentRepository;
import com.premiernuts.identity.jpa.repository.role.RoleRepository;
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
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public CompanyDTO getCompanyById(Long id) {
        Optional<Company> companyOpt = companyRepository.findById(id);
        if (companyOpt.isEmpty()) {
            throw new PnException(ErrorCodes.COMMON.NOT_FOUND);
        }
        Company company = companyOpt.get();

        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getId());
        dto.setCode(company.getCode());
        dto.setName(company.getName());
        dto.setTaxCode(company.getTaxCode());
        dto.setAddress(company.getAddress());
        dto.setPhone(company.getPhone());
        dto.setEmail(company.getEmail());
        dto.setStatus(company.getStatus());
        return dto;
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
            DepartmentDTO dto = new DepartmentDTO();
            dto.setId(d.getId());
            dto.setCompanyId(d.getCompanyId());
            dto.setCode(d.getCode());
            dto.setName(d.getName());
            dto.setDescription(d.getDescription());
            dto.setStatus(d.getStatus());
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getRoles() {
        List<Role> list = roleRepository.findAll();
        List<RoleDTO> dtos = new ArrayList<>();
        for (Role r : list) {
            RoleDTO dto = new RoleDTO();
            dto.setId(r.getId());
            dto.setCode(r.getCode());
            dto.setName(r.getName());
            dto.setDescription(r.getDescription());
            dto.setStatus(r.getStatus());
            dtos.add(dto);
        }
        return dtos;
    }
}
