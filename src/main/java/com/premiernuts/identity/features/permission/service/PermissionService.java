package com.premiernuts.identity.features.permission.service;

import com.premiernuts.identity.features.permission.dto.PermissionDTO;

import java.util.List;

public interface PermissionService {

    List<PermissionDTO> getPermissions();
}
