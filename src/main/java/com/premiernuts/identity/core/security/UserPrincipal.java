package com.premiernuts.identity.core.security;

import com.premiernuts.identity.core.enums.ERole;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class UserPrincipal {

    private Long userId;
    private String username;
    private Long companyId;
    private List<String> roles;
    private List<String> permissions;

    public UserPrincipal() {
    }

    public UserPrincipal(Long userId, String username, Long companyId, List<String> roles) {
        this.userId = userId;
        this.username = username;
        this.companyId = companyId;
        this.roles = roles;
        this.permissions = Collections.emptyList();
    }

    public UserPrincipal(Long userId, String username, Long companyId, List<String> roles, List<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.companyId = companyId;
        this.roles = roles;
        this.permissions = permissions;
    }

    public boolean hasRole(String role) {
        if (this.roles == null || role == null) {
            return false;
        }
        return this.roles.contains(role);
    }

    public boolean hasPermission(String permission) {
        if (hasRole(ERole.ROLE_ADMIN.name()) || hasRole(ERole.SUPER_ADMIN.name())) {
            return true;
        }
        if (this.permissions == null || permission == null) {
            return false;
        }
        return this.permissions.contains(permission);
    }
}
