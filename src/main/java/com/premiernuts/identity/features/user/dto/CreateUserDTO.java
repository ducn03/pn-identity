package com.premiernuts.identity.features.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateUserDTO {

    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private Long departmentId;
    private Long companyId;
    private Long roleId;
    private List<Long> roleIds;
    private List<Long> permissionIds;
}
