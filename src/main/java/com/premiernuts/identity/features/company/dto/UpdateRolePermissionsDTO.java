package com.premiernuts.identity.features.company.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateRolePermissionsDTO {

    private List<Long> permissionIds;
}
