package com.premiernuts.identity.core.security;

public class UserContext {

    private static final ThreadLocal<UserPrincipal> CURRENT_USER = new ThreadLocal<>();

    public static void set(UserPrincipal user) {
        CURRENT_USER.set(user);
    }

    public static UserPrincipal get() {
        return CURRENT_USER.get();
    }

    public static Long getCurrentUserId() {
        UserPrincipal principal = CURRENT_USER.get();
        if (principal != null) {
            return principal.getUserId();
        }
        return null;
    }

    public static String getCurrentUsername() {
        UserPrincipal principal = CURRENT_USER.get();
        if (principal != null) {
            return principal.getUsername();
        }
        return null;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
