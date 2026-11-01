package com.premiernuts.identity.features.role.validator;

import com.premiernuts.identity.core.enums.EPermission;
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

        if (!principal.hasPermission(EPermission.Names.ROLE_ASSIGN)) {
            throw new PnException(ErrorCodes.FORBIDDEN.ACCESS_DENIED);
        }

        if (targetRole != null && !Role.canManage(actorRoleLevel, targetRole.getLevel())) {
            throw new PnException(ErrorCodes.FORBIDDEN.ROLE_NOT_ALLOWED);
        }
    }
}
