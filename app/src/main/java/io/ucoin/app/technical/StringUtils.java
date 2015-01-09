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
}
