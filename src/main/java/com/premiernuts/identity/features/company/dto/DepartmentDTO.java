package com.premiernuts.identity.features.company.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentDTO {

    private Long id;
    private Long companyId;
    private String code;
    private String name;
    private String description;
    private String status;
}
