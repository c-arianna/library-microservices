package mentoring.acomi.sharedcorelibrary.common;

public final class CardNumberUtils {

    private static final String PREFIX = "LIB-";

    private CardNumberUtils() {}

    public static String normalizeCardNumber(String value) {

        if (value == null || value.isBlank()) {
            return value;
        }

        String normalized = value.trim().toUpperCase();

        if (normalized.startsWith(PREFIX)) {
            return normalized;
        }

        String digits = normalized.replaceAll("\\D", "");

        if (digits.isBlank()) {
            return normalized;
        }

        return "%s%06d".formatted(PREFIX, Long.parseLong(digits));
    }
}
