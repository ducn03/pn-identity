package com.premiernuts.identity.features.role.mapper;

import com.premiernuts.identity.features.role.dto.RoleDTO;
import com.premiernuts.identity.jpa.domain.role.Role;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleMapper {

    public RoleDTO toDTO(Role role, List<Long> permissionIds, List<String> permissionCodes) {
        if (role == null) {
            return null;
        }

        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setLevel(role.getLevel());
        dto.setStatus(role.getStatus());
        dto.setPermissionIds(permissionIds);
        dto.setPermissionCodes(permissionCodes);
        return dto;
    }
}
