package com.premiernuts.identity.core.constants;

public final class CommonConstants {

    private CommonConstants() {
    }

    public static final String DEFAULT_PAGE_INDEX = "1";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final int MAX_PAGE_SIZE = 100;
    public static final Long DEFAULT_COMPANY_ID = 1L;

    public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String PARAM_LANG = "lang";
    public static final String DEFAULT_LANGUAGE = "vi";

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_DELETED = "DELETED";
    public static final String STATUS_LOCKED = "LOCKED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    // Success Message Codes
    public static final String MSG_LOGOUT_SUCCESS = "20001001";
    public static final String MSG_USER_CREATED = "20002001";
    public static final String MSG_USER_UPDATED = "20002002";
    public static final String MSG_USER_STATUS_UPDATED = "20002003";
    public static final String MSG_PASSWORD_CHANGED = "20002004";
}
