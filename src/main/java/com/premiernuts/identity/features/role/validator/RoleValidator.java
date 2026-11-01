package com.premiernuts.identity.features.role.validator;

import com.premiernuts.identity.core.enums.EPermission;
import com.premiernuts.identity.core.enums.ERole;
import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.core.security.UserPrincipal;
import com.premiernuts.identity.jpa.domain.role.Permission;
import com.premiernuts.identity.jpa.domain.role.Role;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleValidator {

    public void validateRolePermissionUpdate(
            UserPrincipal principal,
            Role targetRole,
            Integer actorRoleLevel,
            List<Permission> permissionsToAssign
    ) {
        if (principal == null) {
            throw new PnException(ErrorCodes.UNAUTHORIZED.TOKEN_INVALID);
        }

        boolean isAdmin = principal.hasRole(ERole.ROLE_ADMIN.name()) || principal.hasRole(ERole.SUPER_ADMIN.name());
        boolean hasRoleAssign = principal.hasPermission(EPermission.Names.ROLE_ASSIGN);

        if (!isAdmin && !hasRoleAssign) {
            throw new PnException(ErrorCodes.FORBIDDEN.ROLE_NOT_ALLOWED);
        }

        // Cấp bậc phân quyền: Actor không được phép sửa role có level <= actorRoleLevel (ngoại trừ SuperAdmin level 1)
        if (actorRoleLevel != null && targetRole.getLevel() != null) {
            if (actorRoleLevel > 1 && targetRole.getLevel() <= actorRoleLevel) {
                throw new PnException(ErrorCodes.FORBIDDEN.ROLE_NOT_ALLOWED);
            }
        }

        // Không cho phép tài khoản không phải Admin chỉnh sửa khuôn mẫu của ROLE_ADMIN / SUPER_ADMIN
        boolean isTargetAdminRole = ERole.ROLE_ADMIN.name().equalsIgnoreCase(targetRole.getCode())
                || ERole.SUPER_ADMIN.name().equalsIgnoreCase(targetRole.getCode());
        if (!isAdmin && isTargetAdminRole) {
            throw new PnException(ErrorCodes.FORBIDDEN.ROLE_NOT_ALLOWED);
        }

        // Nếu không phải Admin, chặn không cho gán quyền hệ thống (DASHBOARD_VIEW, LOG_VIEW, HEALTH_VIEW)
        if (!isAdmin && permissionsToAssign != null) {
            for (Permission p : permissionsToAssign) {
                if (EPermission.isSystemPermission(p.getCode())) {
                    throw new PnException(ErrorCodes.FORBIDDEN.ROLE_NOT_ALLOWED);
                }
            }
        }
    }
}
