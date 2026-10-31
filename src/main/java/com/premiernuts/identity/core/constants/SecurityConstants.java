package com.premiernuts.identity.core.constants;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_COMPANY_ID = "companyId";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_TYPE = "type";

    public static final String REQ_ATTR_USER_PRINCIPAL = "USER_PRINCIPAL";
    public static final String REQ_ATTR_USER_ID = "USER_ID";

    public static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 86400L; // 24 hours
    public static final long REFRESH_TOKEN_EXPIRATION_DAYS = 7L;       // 7 days
}
