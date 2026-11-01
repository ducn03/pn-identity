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

    public void validateRoleAssignment(
            com.premiernuts.identity.core.security.UserPrincipal actor,
            Integer actorLevel,
            com.premiernuts.identity.jpa.domain.role.Role targetRole
    ) {
        if (actor == null) {
            throw new PnException(ErrorCodes.UNAUTHORIZED.TOKEN_INVALID);
        }

        boolean isSuperAdmin = actor.hasRole(com.premiernuts.identity.core.enums.ERole.Names.SUPER_ADMIN);
        if (isSuperAdmin) {
            return;
        }

        boolean isAdmin = actor.hasRole(com.premiernuts.identity.core.enums.ERole.Names.ROLE_ADMIN);

        if (targetRole != null) {
            boolean isTargetAdmin = com.premiernuts.identity.core.enums.ERole.ROLE_ADMIN.name().equalsIgnoreCase(targetRole.getCode())
                    || com.premiernuts.identity.core.enums.ERole.SUPER_ADMIN.name().equalsIgnoreCase(targetRole.getCode());
            if (isTargetAdmin && !isAdmin) {
                throw new PnException(ErrorCodes.FORBIDDEN.ROLE_NOT_ALLOWED);
            }

            // Quy tắc cấp bậc: Người có cấp bậc actorLevel chỉ được thao tác với cấp bậc strictly lớn hơn (quyền hạn thấp hơn)
            if (actorLevel != null && targetRole.getLevel() != null) {
                if (targetRole.getLevel() <= actorLevel) {
                    throw new PnException(ErrorCodes.FORBIDDEN.ROLE_NOT_ALLOWED);
                }
            }
        }
    }

    public void validateUserModification(
            com.premiernuts.identity.core.security.UserPrincipal actor,
            Integer actorLevel,
            com.premiernuts.identity.jpa.domain.user.User targetUser,
            com.premiernuts.identity.jpa.domain.role.Role targetUserRole
    ) {
        if (actor == null) {
            throw new PnException(ErrorCodes.UNAUTHORIZED.TOKEN_INVALID);
        }

        boolean isSuperAdmin = actor.hasRole(com.premiernuts.identity.core.enums.ERole.Names.SUPER_ADMIN);
        if (isSuperAdmin) {
            return;
        }

        boolean isAdmin = actor.hasRole(com.premiernuts.identity.core.enums.ERole.Names.ROLE_ADMIN);

        if (targetUserRole != null) {
            boolean isTargetAdmin = com.premiernuts.identity.core.enums.ERole.ROLE_ADMIN.name().equalsIgnoreCase(targetUserRole.getCode())
                    || com.premiernuts.identity.core.enums.ERole.SUPER_ADMIN.name().equalsIgnoreCase(targetUserRole.getCode());
            if (isTargetAdmin && !isAdmin) {
                throw new PnException(ErrorCodes.FORBIDDEN.ROLE_NOT_ALLOWED);
            }

            if (actorLevel != null && targetUserRole.getLevel() != null) {
                if (targetUserRole.getLevel() <= actorLevel) {
                    throw new PnException(ErrorCodes.FORBIDDEN.ROLE_NOT_ALLOWED);
                }
            }
        }
    }

    public void validateAdminPermissionAssignment(
            com.premiernuts.identity.core.security.UserPrincipal actor,
            List<com.premiernuts.identity.jpa.domain.role.Permission> perms
    ) {
        if (perms == null || perms.isEmpty()) {
            return;
        }
        if (actor != null && (actor.hasRole(com.premiernuts.identity.core.enums.ERole.ROLE_ADMIN.name())
                || actor.hasRole(com.premiernuts.identity.core.enums.ERole.SUPER_ADMIN.name()))) {
            return;
        }

        for (com.premiernuts.identity.jpa.domain.role.Permission p : perms) {
            if (com.premiernuts.identity.core.enums.EPermission.isSystemPermission(p.getCode())) {
                throw new PnException(ErrorCodes.FORBIDDEN.ROLE_NOT_ALLOWED);
            }
        }
    }
}
