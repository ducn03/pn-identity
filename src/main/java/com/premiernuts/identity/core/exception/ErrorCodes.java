package com.premiernuts.identity.core.exception;

public interface ErrorCodes {

    interface COMMON {
        String BAD_REQUEST = "40000001";
        String INTERNAL_SERVER_ERROR = "50000001";
        String NOT_FOUND = "40400001";
        String LOG_ACTIVITY_ERROR = "40000002";
    }

    interface UNAUTHORIZED {
        String TOKEN_EXPIRED = "40100001";
        String UNAUTHORIZED = "40100002";
        String TOKEN_INVALID = "40100003";
    }

    interface FORBIDDEN {
        String ACCESS_DENIED = "40300001";
        String ROLE_NOT_ALLOWED = "40300002";
    }

    interface VALIDATION {
        String INVALID_INPUT = "40002001";
        String USERNAME_REQUIRED = "40002002";
        String PASSWORD_REQUIRED = "40002003";
        String FULL_NAME_REQUIRED = "40002004";
        String USERNAME_LENGTH = "40002005";
        String PASSWORD_LENGTH = "40002006";
        String EMAIL_INVALID = "40002007";
        String REFRESH_TOKEN_REQUIRED = "40002008";
        String STATUS_INVALID = "40002009";
        String FULL_NAME_MIN_WORDS = "40002010";
    }

    interface AUTH {
        String USER_NOT_FOUND = "40003001";
        String PASSWORD_INCORRECT = "40003002";
        String ACCOUNT_LOCKED = "40003003";
        String TOKEN_REVOKED = "40003004";
    }

    interface USER {
        String USERNAME_EXISTS = "40004001";
        String USERNAME_ALREADY_EXISTS = "40004001";
        String EMAIL_EXISTS = "40004002";
        String EMAIL_ALREADY_EXISTS = "40004002";
        String USER_NOT_FOUND = "40004003";
        String DEPARTMENT_NOT_FOUND = "40004004";
    }
}
