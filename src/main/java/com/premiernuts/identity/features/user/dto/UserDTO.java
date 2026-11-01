package com.premiernuts.identity.features.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class UserDTO {

    private Long id;
    private Long companyId;
    private Long departmentId;
    private String departmentName;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String status;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Long roleId;
    private String roleCode;
    private String roleName;
    private List<String> roles;
    private List<Long> roleIds;
    private List<Long> permissionIds;
    private List<String> permissions;
}
