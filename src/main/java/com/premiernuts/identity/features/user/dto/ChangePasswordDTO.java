package com.premiernuts.identity.features.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDTO {

    private String currentPassword;
    private String newPassword;
}
