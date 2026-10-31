package com.premiernuts.identity.features.user.mapper;

import com.premiernuts.identity.core.constants.CommonConstants;
import com.premiernuts.identity.core.enums.EStatus;
import com.premiernuts.identity.core.utils.PasswordHelper;
import com.premiernuts.identity.core.utils.StringHelper;
import com.premiernuts.identity.features.user.dto.CreateUserDTO;
import com.premiernuts.identity.features.user.dto.UpdateUserDTO;
import com.premiernuts.identity.features.user.dto.UserDTO;
import com.premiernuts.identity.jpa.domain.user.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public UserDTO toDTO(User user, String deptName, List<String> roleCodes, List<Long> roleIds) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setCompanyId(user.getCompanyId());
        dto.setDepartmentId(user.getDepartmentId());
        dto.setDepartmentName(deptName);
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setStatus(user.getStatus());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setRoles(roleCodes);
        dto.setRoleIds(roleIds);
        return dto;
    }

    public User toEntity(CreateUserDTO request, Long defaultCompanyId) {
        if (request == null) {
            return null;
        }

        Long companyId = defaultCompanyId;
        if (request.getCompanyId() != null) {
            companyId = request.getCompanyId();
        }
        if (companyId == null) {
            companyId = CommonConstants.DEFAULT_COMPANY_ID;
        }

        User user = new User();
        user.setCompanyId(companyId);
        user.setDepartmentId(request.getDepartmentId());
        user.setUsername(StringHelper.toLowerCase(request.getUsername()));
        user.setPasswordHash(PasswordHelper.hash(request.getPassword()));
        user.setFullName(StringHelper.trim(request.getFullName()));
        if (StringHelper.isNotBlank(request.getEmail())) {
            user.setEmail(StringHelper.toLowerCase(request.getEmail()));
        }
        if (StringHelper.isNotBlank(request.getPhone())) {
            user.setPhone(StringHelper.trim(request.getPhone()));
        }
        user.setStatus(EStatus.ACTIVE.name());
        return user;
    }

    public void updateEntity(User user, UpdateUserDTO request) {
        if (user == null || request == null) {
            return;
        }

        user.setFullName(StringHelper.trim(request.getFullName()));
        if (StringHelper.isNotBlank(request.getPhone())) {
            user.setPhone(StringHelper.trim(request.getPhone()));
        }
        user.setDepartmentId(request.getDepartmentId());
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
    }
}
