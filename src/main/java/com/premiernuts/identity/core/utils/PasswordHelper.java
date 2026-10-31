package com.premiernuts.identity.core.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHelper {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static String hash(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        return ENCODER.encode(rawPassword);
    }

    public static String encode(String rawPassword) {
        return hash(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
