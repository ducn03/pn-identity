package com.premiernuts.identity.features.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateUserDTO {

    private String fullName;
    private String email;
    private String phone;
    private Long departmentId;
    private String avatarUrl;
    private List<Long> roleIds;
}
