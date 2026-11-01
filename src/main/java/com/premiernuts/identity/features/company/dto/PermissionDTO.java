package com.premiernuts.identity.features.company.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionDTO {

    private Long id;
    private String code;
    private String name;
    private String module;
    private String description;
    private String status;
}
