package io.ucoin.app.technical;

/**
 * Created by eis on 21/12/14.
 */
public class StringUtils {

    public static boolean isNotBlank(String value) {
        return value != null && value.trim().length() > 0;
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }

    public static String truncate(String value, int maxLength) {
        if (value != null && value.length() > maxLength && maxLength >= 1) {
            return value.substring(0, maxLength - 1);
        }
        else {
            return value;
        }
    }

    public static String truncateWithIndicator(String value, int maxLength) {
        if (value != null && value.length() > maxLength && maxLength >= 4) {
            return value.substring(0, maxLength - 3) + "...";
        }
        else {
            return value;
        }
    }
}
