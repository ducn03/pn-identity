package com.premiernuts.identity.features.role.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoleDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer level;
    private String status;
    private List<Long> permissionIds;
    private List<String> permissionCodes;
}
