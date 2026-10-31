package com.premiernuts.identity.features.user.validator;

import com.premiernuts.identity.core.enums.EStatus;
import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.core.utils.MessageHelper;
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

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.USERNAME_REQUIRED));
        } else {
            String trimmedUsername = request.getUsername().trim();
            if (trimmedUsername.length() < 3 || trimmedUsername.length() > 50) {
                errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.USERNAME_LENGTH));
            }
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.PASSWORD_REQUIRED));
        } else {
            if (request.getPassword().length() < 6) {
                errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.PASSWORD_LENGTH));
            }
        }

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.FULL_NAME_REQUIRED));
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(request.getEmail().trim()).matches()) {
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

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.FULL_NAME_REQUIRED));
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(request.getEmail().trim()).matches()) {
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

        if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
            errors.add(MessageHelper.getMessage(ErrorCodes.VALIDATION.PASSWORD_REQUIRED));
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
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
        if (request == null || request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new PnException(ErrorCodes.VALIDATION.INVALID_INPUT);
        }

        String status = request.getStatus().trim().toUpperCase();
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
