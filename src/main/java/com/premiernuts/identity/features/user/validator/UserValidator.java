package com.premiernuts.identity.features.user.validator;

import com.premiernuts.identity.core.enums.EStatus;
import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.core.utils.MessageHelper;
import com.premiernuts.identity.core.utils.StringHelper;
import com.premiernuts.identity.features.user.dto.ChangePasswordDTO;
import com.premiernuts.identity.features.user.dto.CreateUserDTO;
import com.premiernuts.identity.features.user.dto.UpdateStatusDTO;
import com.premiernuts.identity.features.user.dto.UpdateUserDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class UserValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public void validateCreate(CreateUserDTO request) {
        if (request == null) {
            throw new PnException(ErrorCodes.VALIDATION.INVALID_INPUT);
        }

        List<String> errors = new ArrayList<>();

        if (StringHelper.isNotBlank(request.getUsername())) {
            String trimmedUsername = StringHelper.trim(request.getUsername());
            if (trimmedUsername.length() < 3 || trimmedUsername.length() > 50) {
                errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.USERNAME_LENGTH));
            }
        }

        if (StringHelper.isBlank(request.getPassword())) {
            errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.PASSWORD_REQUIRED));
        } else {
            if (request.getPassword().length() < 6) {
                errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.PASSWORD_LENGTH));
            }
        }

        if (StringHelper.isBlank(request.getFullName())) {
            errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.FULL_NAME_REQUIRED));
        } else {
            String[] words = request.getFullName().trim().split("\\s+");
            if (words.length < 2) {
                errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.FULL_NAME_MIN_WORDS));
            }
        }

        if (StringHelper.isNotBlank(request.getEmail())) {
            if (!EMAIL_PATTERN.matcher(StringHelper.trim(request.getEmail())).matches()) {
                errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.EMAIL_INVALID));
            }
        }

        if (!errors.isEmpty()) {
            throw new PnException(ErrorCodes.VALIDATION.INVALID_INPUT, errors);
        }
    }

    public void validateUpdate(UpdateUserDTO request) {
        if (request == null) {
            throw new PnException(ErrorCodes.VALIDATION.INVALID_INPUT);
        }

        List<String> errors = new ArrayList<>();

        if (StringHelper.isBlank(request.getFullName())) {
            errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.FULL_NAME_REQUIRED));
        }

        if (StringHelper.isNotBlank(request.getEmail())) {
            if (!EMAIL_PATTERN.matcher(StringHelper.trim(request.getEmail())).matches()) {
                errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.EMAIL_INVALID));
            }
        }

        if (!errors.isEmpty()) {
            throw new PnException(ErrorCodes.VALIDATION.INVALID_INPUT, errors);
        }
    }

    public void validateChangePassword(ChangePasswordDTO request) {
        if (request == null) {
            throw new PnException(ErrorCodes.VALIDATION.INVALID_INPUT);
        }

        List<String> errors = new ArrayList<>();

        if (StringHelper.isBlank(request.getCurrentPassword())) {
            errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.PASSWORD_REQUIRED));
        }

        if (StringHelper.isBlank(request.getNewPassword())) {
            errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.PASSWORD_REQUIRED));
        } else {
            if (request.getNewPassword().length() < 6) {
                errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.PASSWORD_LENGTH));
            }
        }

        if (!errors.isEmpty()) {
            throw new PnException(ErrorCodes.VALIDATION.INVALID_INPUT, errors);
        }
    }

    public void validateUpdateStatus(UpdateStatusDTO request) {
        if (request == null || StringHelper.isBlank(request.getStatus())) {
            throw new PnException(ErrorCodes.VALIDATION.INVALID_INPUT);
        }

        String status = StringHelper.toUpperCase(request.getStatus());
        boolean valid = false;
        if (EStatus.ACTIVE.name().equals(status)) {
            valid = true;
        } else if (EStatus.INACTIVE.name().equals(status)) {
            valid = true;
        } else if (EStatus.DELETED.name().equals(status)) {
            valid = true;
        } else if ("LOCKED".equals(status)) {
            valid = true;
        }

        if (!valid) {
            throw new PnException(ErrorCodes.VALIDATION.STATUS_INVALID);
        }
    }
}
