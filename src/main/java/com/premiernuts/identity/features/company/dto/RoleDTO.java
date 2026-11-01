package com.premiernuts.identity.features.company.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String status;
    private java.util.List<Long> permissionIds;
    private java.util.List<String> permissionCodes;
}
