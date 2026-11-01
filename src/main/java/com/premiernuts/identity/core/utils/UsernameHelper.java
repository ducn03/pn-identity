package com.premiernuts.identity.core.utils;

import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.jpa.repository.user.UserRepository;

import java.text.Normalizer;
import java.util.Set;
import java.util.regex.Pattern;

public final class UsernameHelper {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private static final Set<String> VIETNAMESE_SURNAMES = Set.of(
            "nguyen", "tran", "le", "pham", "hoang", "huynh", "phan", "vu", "vo",
            "dang", "bui", "do", "ho", "ngo", "duong", "ly", "dinh", "truong",
            "mai", "dao", "doan", "vuong", "trinh", "luong"
    );

    private UsernameHelper() {
    }

    public static String removeAccents(String text) {
        if (text == null) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        String withoutDiacritics = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
        return withoutDiacritics.replace("đ", "d").replace("Đ", "d");
    }

    public static String generateBaseUsername(String fullName) {
        if (StringHelper.isBlank(fullName)) {
            throw new PnException(ErrorCodes.VALIDATION.FULL_NAME_REQUIRED);
        }

        String trimmed = fullName.trim();
        String[] words = trimmed.split("\\s+");
        if (words.length < 2) {
            throw new PnException(ErrorCodes.VALIDATION.FULL_NAME_MIN_WORDS);
        }

        String firstWord = words[0];
        String lastWord = words[words.length - 1];

        String firstWordClean = removeAccents(firstWord).toLowerCase().replaceAll("[^a-z0-9]", "");
        String lastWordClean = removeAccents(lastWord).toLowerCase().replaceAll("[^a-z0-9]", "");

        boolean isVietnamese = words.length >= 3
                || VIETNAMESE_SURNAMES.contains(firstWordClean)
                || hasVietnameseChars(trimmed);

        String base;
        if (isVietnamese) {
            base = lastWordClean + "." + firstWordClean;
        } else {
            base = firstWordClean + "." + lastWordClean;
        }

        return base;
    }

    public static String generateUniqueUsername(String fullName, UserRepository userRepository) {
        String base = generateBaseUsername(fullName);
        String candidate = base;
        int counter = 2;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + counter;
            counter++;
        }
        return candidate;
    }

    private static boolean hasVietnameseChars(String text) {
        if (text == null) {
            return false;
        }
        for (char c : text.toCharArray()) {
            if ("àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđĐ".indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }
}
