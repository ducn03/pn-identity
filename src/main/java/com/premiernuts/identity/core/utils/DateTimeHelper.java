package com.premiernuts.identity.core.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class DateTimeHelper {

    private DateTimeHelper() {
    }

    public static Instant now() {
        return Instant.now();
    }

    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static Instant plusDays(long days) {
        return Instant.now().plus(days, ChronoUnit.DAYS);
    }

    public static Instant plusDays(Instant from, long days) {
        if (from == null) {
            return null;
        }
        return from.plus(days, ChronoUnit.DAYS);
    }

    public static Instant plusSeconds(long seconds) {
        return Instant.now().plus(seconds, ChronoUnit.SECONDS);
    }

    public static Instant plusSeconds(Instant from, long seconds) {
        if (from == null) {
            return null;
        }
        return from.plus(seconds, ChronoUnit.SECONDS);
    }

    public static boolean isExpired(Instant expiredAt) {
        if (expiredAt == null) {
            return true;
        }
        return expiredAt.isBefore(Instant.now());
    }

    public static boolean isNotExpired(Instant expiredAt) {
        return !isExpired(expiredAt);
    }

    public static String formatIso(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.toString();
    }
}
