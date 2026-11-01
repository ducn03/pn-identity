package com.premiernuts.identity.features.role.constants;

import com.premiernuts.identity.core.constants.ApiConstants;

public final class RoleEndpoints {

    private RoleEndpoints() {
    }

    public static final String BASE = ApiConstants.API_V1 + "/roles";
    public static final String ID = "/{id}";
    public static final String PERMISSIONS = "/{roleId}/permissions";
}
