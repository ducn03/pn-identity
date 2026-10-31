package com.premiernuts.identity.features.auth.constants;

import com.premiernuts.identity.core.constants.ApiConstants;

public final class AuthEndpoints {

    private AuthEndpoints() {
    }

    public static final String BASE = ApiConstants.API_V1 + "/auth";
    public static final String LOGIN = "/login";
    public static final String REFRESH = "/refresh";
    public static final String LOGOUT = "/logout";
    public static final String ME = "/me";
}
