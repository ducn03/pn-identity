package com.premiernuts.identity.features.company.validator;

import com.premiernuts.identity.core.enums.ERole;
import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.core.security.UserPrincipal;
import com.premiernuts.identity.core.utils.StringHelper;
import com.premiernuts.identity.features.company.dto.UpdateCompanyDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CompanyValidator {

    public void validateUpdateCompany(UserPrincipal principal, UpdateCompanyDTO request) {
        if (principal == null) {
            throw new PnException(ErrorCodes.UNAUTHORIZED.TOKEN_INVALID);
        }

        boolean isAdmin = principal.hasRole(ERole.ROLE_ADMIN.name()) || principal.hasRole(ERole.SUPER_ADMIN.name());
        if (!isAdmin) {
            throw new PnException(ErrorCodes.FORBIDDEN.ROLE_NOT_ALLOWED);
        }

        if (request == null) {
            throw new PnException(ErrorCodes.COMMON.BAD_REQUEST);
        }

        List<String> errors = new ArrayList<>();
        if (StringHelper.isBlank(request.getName())) {
            errors.add("Tên công ty không được để trống");
        }

        if (!errors.isEmpty()) {
            throw new PnException(ErrorCodes.VALIDATION.INVALID_INPUT, errors);
        }
    }
}
