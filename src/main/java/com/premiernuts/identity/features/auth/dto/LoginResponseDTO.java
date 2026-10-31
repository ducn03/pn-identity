package com.premiernuts.identity.features.auth.dto;

import com.premiernuts.identity.core.security.UserPrincipal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserPrincipal user;
}
