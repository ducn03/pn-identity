package com.premiernuts.identity.features.permission.mapper;

import com.premiernuts.identity.features.permission.dto.PermissionDTO;
import com.premiernuts.identity.jpa.domain.role.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionDTO toDTO(Permission entity) {
        if (entity == null) {
            return null;
        }

        PermissionDTO dto = new PermissionDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setModule(entity.getModule());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
