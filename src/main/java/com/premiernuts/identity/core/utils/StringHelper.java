package com.premiernuts.identity.core.utils;

public final class StringHelper {

    private StringHelper() {
    }

    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        return str.trim().isEmpty();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String trim(String str) {
        if (str == null) {
            return null;
        }
        return str.trim();
    }

    public static String trimToEmpty(String str) {
        if (str == null) {
            return "";
        }
        return str.trim();
    }

    public static String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        String trimmed = str.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    public static String toLowerCase(String str) {
        if (str == null) {
            return null;
        }
        return str.trim().toLowerCase();
    }

    public static String toUpperCase(String str) {
        if (str == null) {
            return null;
        }
        return str.trim().toUpperCase();
    }

    public static String toLikePattern(String keyword) {
        if (isBlank(keyword)) {
            return null;
        }
        return "%" + keyword.trim().toLowerCase() + "%";
    }
}
