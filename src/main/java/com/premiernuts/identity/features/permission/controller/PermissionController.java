package com.premiernuts.identity.features.permission.controller;

import com.premiernuts.identity.core.aop.LogActivity;
import com.premiernuts.identity.core.dto.ResponseData;
import com.premiernuts.identity.core.utils.ResponseUtils;
import com.premiernuts.identity.features.permission.constants.PermissionEndpoints;
import com.premiernuts.identity.features.permission.dto.PermissionDTO;
import com.premiernuts.identity.features.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(PermissionEndpoints.BASE)
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @LogActivity(requireAuth = true)
    public ResponseEntity<ResponseData<List<PermissionDTO>>> getPermissions() {
        List<PermissionDTO> list = permissionService.getPermissions();
        return ResponseUtils.toSuccess(list);
    }
}
