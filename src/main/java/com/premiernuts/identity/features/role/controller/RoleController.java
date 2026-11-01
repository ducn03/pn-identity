package com.premiernuts.identity.features.role.controller;

import com.premiernuts.identity.core.aop.LogActivity;
import com.premiernuts.identity.core.dto.ResponseData;
import com.premiernuts.identity.core.enums.EPermission;
import com.premiernuts.identity.core.utils.ResponseUtils;
import com.premiernuts.identity.features.role.constants.RoleEndpoints;
import com.premiernuts.identity.features.role.dto.RoleDTO;
import com.premiernuts.identity.features.role.dto.UpdateRolePermissionsDTO;
import com.premiernuts.identity.features.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(RoleEndpoints.BASE)
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @LogActivity(requireAuth = true)
    public ResponseEntity<ResponseData<List<RoleDTO>>> getRoles() {
        List<RoleDTO> list = roleService.getRoles();
        return ResponseUtils.toSuccess(list);
    }

    @PutMapping(RoleEndpoints.PERMISSIONS)
    @LogActivity(requireAuth = true, permissions = {EPermission.Names.ROLE_ASSIGN})
    public ResponseEntity<ResponseData<RoleDTO>> updateRolePermissions(
            @PathVariable Long roleId,
            @RequestBody UpdateRolePermissionsDTO request) {
        RoleDTO updated = roleService.updateRolePermissions(roleId, request);
        return ResponseUtils.toSuccess(updated);
    }
}
