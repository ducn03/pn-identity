package com.premiernuts.identity.features.auth.validator;

import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.core.utils.StringHelper;
import com.premiernuts.identity.features.auth.dto.LoginRequestDTO;
import com.premiernuts.identity.features.auth.dto.RefreshTokenRequestDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AuthValidator {

    public void validateLogin(LoginRequestDTO request) {
        if (request == null) {
            throw new PnException(ErrorCodes.COMMON.BAD_REQUEST);
        }

        List<String> errors = new ArrayList<>();
        if (StringHelper.isBlank(request.getUsername())) {
            errors.add("Tên đăng nhập không được để trống");
        }
        if (StringHelper.isBlank(request.getPassword())) {
            errors.add("Mật khẩu không được để trống");
        }

        if (!errors.isEmpty()) {
            throw new PnException(ErrorCodes.VALIDATION.INVALID_INPUT, errors);
        }
    }

    public void validateRefreshToken(RefreshTokenRequestDTO request) {
        if (request == null || StringHelper.isBlank(request.getRefreshToken())) {
            throw new PnException(ErrorCodes.VALIDATION.REFRESH_TOKEN_REQUIRED);
        }
    }
}
