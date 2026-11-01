package com.premiernuts.identity.features.company.mapper;

import com.premiernuts.identity.core.utils.StringHelper;
import com.premiernuts.identity.features.company.dto.CompanyDTO;
import com.premiernuts.identity.features.company.dto.DepartmentDTO;
import com.premiernuts.identity.features.company.dto.UpdateCompanyDTO;
import com.premiernuts.identity.jpa.domain.company.Company;
import com.premiernuts.identity.jpa.domain.company.Department;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public CompanyDTO toDTO(Company entity) {
        if (entity == null) {
            return null;
        }

        CompanyDTO dto = new CompanyDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setTaxCode(entity.getTaxCode());
        dto.setAddress(entity.getAddress());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setWebsite(entity.getWebsite());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public DepartmentDTO toDepartmentDTO(Department entity) {
        if (entity == null) {
            return null;
        }

        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(entity.getId());
        dto.setCompanyId(entity.getCompanyId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public void updateEntity(Company company, UpdateCompanyDTO request) {
        if (company == null || request == null) {
            return;
        }

        if (StringHelper.isNotBlank(request.getName())) {
            company.setName(StringHelper.trim(request.getName()));
        }
        if (request.getTaxCode() != null) {
            company.setTaxCode(StringHelper.trim(request.getTaxCode()));
        }
        if (request.getEmail() != null) {
            company.setEmail(StringHelper.trim(request.getEmail()));
        }
        if (request.getPhone() != null) {
            company.setPhone(StringHelper.trim(request.getPhone()));
        }
        if (request.getAddress() != null) {
            company.setAddress(StringHelper.trim(request.getAddress()));
        }
        if (request.getWebsite() != null) {
            company.setWebsite(StringHelper.trim(request.getWebsite()));
        }
    }
}
