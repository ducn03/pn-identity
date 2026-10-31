package com.premiernuts.identity.core.security;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserPrincipal {

    private Long userId;
    private String username;
    private Long companyId;
    private List<String> roles;

    public UserPrincipal() {
    }

    public UserPrincipal(Long userId, String username, Long companyId, List<String> roles) {
        this.userId = userId;
        this.username = username;
        this.companyId = companyId;
        this.roles = roles;
    }

    public boolean hasRole(String role) {
        if (this.roles == null || role == null) {
            return false;
        }
        return this.roles.contains(role);
    }
}
