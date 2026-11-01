package com.premiernuts.identity.features.permission.service.impl;

import com.premiernuts.identity.core.enums.ERole;
import com.premiernuts.identity.core.security.UserContext;
import com.premiernuts.identity.core.security.UserPrincipal;
import com.premiernuts.identity.features.permission.dto.PermissionDTO;
import com.premiernuts.identity.features.permission.mapper.PermissionMapper;
import com.premiernuts.identity.features.permission.service.PermissionService;
import com.premiernuts.identity.jpa.domain.role.Permission;
import com.premiernuts.identity.jpa.repository.role.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getPermissions() {
        UserPrincipal principal = UserContext.get();
        List<Permission> list;
        if (principal != null && (principal.hasRole(ERole.ROLE_ADMIN.name()) || principal.hasRole(ERole.SUPER_ADMIN.name()))) {
            list = permissionRepository.findAll();
        } else if (principal != null && principal.getPermissions() != null && !principal.getPermissions().isEmpty()) {
            list = permissionRepository.findAllByCodeIn(principal.getPermissions());
        } else {
            list = Collections.emptyList();
        }

        List<PermissionDTO> dtos = new ArrayList<>();
        for (Permission p : list) {
            dtos.add(permissionMapper.toDTO(p));
        }
        return dtos;
    }
}
