package com.premiernuts.identity.features.user.constants;

import com.premiernuts.identity.core.constants.ApiConstants;

public final class UserEndpoints {

    private UserEndpoints() {
    }

    public static final String BASE = ApiConstants.API_V1 + "/users";
    public static final String ID = "/{id}";
    public static final String STATUS = "/{id}/status";
    public static final String CHANGE_PASSWORD = "/change-password";
    public static final String GENERATE_USERNAME = "/generate-username";
}
