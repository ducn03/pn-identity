package com.premiernuts.identity.features.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDetailContext {

    private String departmentName;
    private Long primaryRoleId;
    private String primaryRoleCode;
    private String primaryRoleName;
    private List<String> roleCodes;
    private List<Long> roleIds;
    private List<Long> permissionIds;
    private List<String> permissionCodes;

    public static UserDetailContext of(
            String departmentName,
            Long primaryRoleId,
            String primaryRoleCode,
            String primaryRoleName,
            List<String> roleCodes,
            List<Long> roleIds,
            List<Long> permissionIds,
            List<String> permissionCodes
    ) {
        UserDetailContext ctx = new UserDetailContext();
        ctx.departmentName = departmentName;
        ctx.primaryRoleId = primaryRoleId;
        ctx.primaryRoleCode = primaryRoleCode;
        ctx.primaryRoleName = primaryRoleName;
        ctx.roleCodes = roleCodes;
        ctx.roleIds = roleIds;
        ctx.permissionIds = permissionIds;
        ctx.permissionCodes = permissionCodes;
        return ctx;
    }
}
