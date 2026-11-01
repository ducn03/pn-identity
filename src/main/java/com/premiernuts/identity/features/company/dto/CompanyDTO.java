package com.premiernuts.identity.features.company.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyDTO {

    private Long id;
    private String code;
    private String name;
    private String taxCode;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String status;
}
