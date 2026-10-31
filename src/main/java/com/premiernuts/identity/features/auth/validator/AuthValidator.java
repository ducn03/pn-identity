package com.premiernuts.identity.features.auth.validator;

import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.core.utils.MessageHelper;
import com.premiernuts.identity.features.auth.dto.LoginRequestDTO;
import com.premiernuts.identity.features.auth.dto.RefreshTokenRequestDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AuthValidator {

    public void validateLogin(LoginRequestDTO request) {
        if (request == null) {
            throw new PnException(ErrorCodes.VALIDATION.INVALID_INPUT);
        }

        List<String> errors = new ArrayList<>();

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.USERNAME_REQUIRED));
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.PASSWORD_REQUIRED));
        }

        if (!errors.isEmpty()) {
            throw new PnException(ErrorCodes.VALIDATION.INVALID_INPUT, errors);
        }
    }

    public void validateRefreshToken(RefreshTokenRequestDTO request) {
        if (request == null) {
            throw new PnException(ErrorCodes.VALIDATION.INVALID_INPUT);
        }

        if (request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
            throw new PnException(ErrorCodes.VALIDATION.REFRESH_TOKEN_REQUIRED);
        }
    }
}
