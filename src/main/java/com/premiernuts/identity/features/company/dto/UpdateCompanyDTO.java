package com.premiernuts.identity.features.company.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCompanyDTO {

    @NotBlank(message = "Tên công ty không được để trống")
    private String name;

    private String taxCode;
    private String email;
    private String phone;
    private String address;
    private String website;
}
