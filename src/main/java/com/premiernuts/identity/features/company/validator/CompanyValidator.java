package com.premiernuts.identity.features.company.validator;

import com.premiernuts.identity.core.enums.EPermission;
import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.core.security.UserPrincipal;
import com.premiernuts.identity.core.utils.StringHelper;
import com.premiernuts.identity.features.company.dto.UpdateCompanyDTO;
import org.springframework.stereotype.Component;

@Component
public class CompanyValidator {

    public void validateUpdateCompany(UserPrincipal principal, UpdateCompanyDTO request) {
        if (principal == null) {
            throw new PnException(ErrorCodes.UNAUTHORIZED.TOKEN_INVALID);
        }

        if (!principal.hasPermission(EPermission.Names.BRANCH_MANAGE)) {
            throw new PnException(ErrorCodes.FORBIDDEN.ACCESS_DENIED);
        }

        if (request == null || StringHelper.isBlank(request.getName())) {
            throw new PnException(ErrorCodes.VALIDATION.INVALID_INPUT);
        }
    }
}
