package io.tebex.sdk.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class StringUtil {
    private static final DateTimeFormatter LEGACY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MODERN_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");

    public static String pluralise(int count, String singular, String plural) {
        return count == 1 ? singular : plural;
    }
    public static String pluralise(int count, String word) {
        return pluralise(count, word, word + "s");
    }

    public static ZonedDateTime toLegacyDate(String date) {
        return LocalDateTime.parse(date, LEGACY_FORMATTER).atZone(ZoneId.of("UTC"));
    }

    public static ZonedDateTime toModernDate(String date) {
        return LocalDateTime.parse(date, MODERN_FORMATTER).atZone(ZoneId.of("UTC"));
    }
}
