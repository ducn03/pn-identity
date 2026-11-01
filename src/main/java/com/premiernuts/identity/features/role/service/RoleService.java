package com.premiernuts.identity.features.role.service;

import com.premiernuts.identity.features.role.dto.RoleDTO;
import com.premiernuts.identity.features.role.dto.UpdateRolePermissionsDTO;

import java.util.List;

public interface RoleService {

    List<RoleDTO> getRoles();

    RoleDTO updateRolePermissions(Long roleId, UpdateRolePermissionsDTO request);
}
