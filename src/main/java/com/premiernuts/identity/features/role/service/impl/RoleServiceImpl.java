package com.premiernuts.identity.features.role.service.impl;

import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.core.security.UserContext;
import com.premiernuts.identity.core.security.UserPrincipal;
import com.premiernuts.identity.features.role.dto.RoleDTO;
import com.premiernuts.identity.features.role.dto.UpdateRolePermissionsDTO;
import com.premiernuts.identity.features.role.mapper.RoleMapper;
import com.premiernuts.identity.features.role.service.RoleService;
import com.premiernuts.identity.features.role.validator.RoleValidator;
import com.premiernuts.identity.jpa.domain.role.Permission;
import com.premiernuts.identity.jpa.domain.role.Role;
import com.premiernuts.identity.jpa.domain.role.RolePermission;
import com.premiernuts.identity.jpa.domain.user.User;
import com.premiernuts.identity.jpa.repository.role.PermissionRepository;
import com.premiernuts.identity.jpa.repository.role.RolePermissionRepository;
import com.premiernuts.identity.jpa.repository.role.RoleRepository;
import com.premiernuts.identity.jpa.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;
    private final RoleValidator roleValidator;

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getRoles() {
        List<Role> list = roleRepository.findAll();
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        List<Permission> allPermissions = permissionRepository.findAll();
        Map<Long, Permission> permMap = new HashMap<>();
        for (Permission p : allPermissions) {
            permMap.put(p.getId(), p);
        }

        List<Long> roleIds = list.stream().map(Role::getId).toList();
        List<RolePermission> allRolePerms = rolePermissionRepository.findAllByRoleIdIn(roleIds);

        Map<Long, List<RolePermission>> rolePermMap = allRolePerms.stream()
                .collect(Collectors.groupingBy(RolePermission::getRoleId));

        List<RoleDTO> dtos = new ArrayList<>();
        for (Role r : list) {
            List<RolePermission> rolePerms = rolePermMap.getOrDefault(r.getId(), Collections.emptyList());
            List<Long> pIds = new ArrayList<>();
            List<String> pCodes = new ArrayList<>();
            for (RolePermission rp : rolePerms) {
                pIds.add(rp.getPermissionId());
                Permission p = permMap.get(rp.getPermissionId());
                if (p != null) {
                    pCodes.add(p.getCode());
                }
            }
            dtos.add(roleMapper.toDTO(r, pIds, pCodes));
        }
        return dtos;
    }

    @Override
    @Transactional
    public RoleDTO updateRolePermissions(Long roleId, UpdateRolePermissionsDTO request) {
        UserPrincipal principal = UserContext.get();
        if (principal == null) {
            throw new PnException(ErrorCodes.UNAUTHORIZED.TOKEN_INVALID);
        }

        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            throw new PnException(ErrorCodes.COMMON.NOT_FOUND);
        }
        Role targetRole = roleOpt.get();

        // Lấy cấp bậc của người thao tác (actor)
        Integer actorLevel = userRepository.resolveActorLevel(principal);

        List<Permission> permsToAssign = Collections.emptyList();
        if (request != null && request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Long> uniqueIds = new LinkedHashSet<>(request.getPermissionIds());
            permsToAssign = permissionRepository.findAllById(uniqueIds);
        }

        // Validate nghiệp vụ phân quyền và cấp bậc (Item 6, 7, 9)
        roleValidator.validateRolePermissionUpdate(principal, targetRole, actorLevel, permsToAssign);

        // Xóa phân quyền cũ của Role template này
        rolePermissionRepository.deleteAllByRoleId(roleId);
        rolePermissionRepository.flush();

        List<Long> pIds = new ArrayList<>();
        List<String> pCodes = new ArrayList<>();

        if (!permsToAssign.isEmpty()) {
            for (Permission p : permsToAssign) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(p.getId());
                rolePermissionRepository.save(rp);
                pIds.add(p.getId());
                pCodes.add(p.getCode());
            }
            rolePermissionRepository.flush();
        }

        return roleMapper.toDTO(targetRole, pIds, pCodes);
    }
}
