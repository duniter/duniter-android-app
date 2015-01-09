package io.ucoin.app.technical;

/**
 * Created by eis on 22/12/14.
 */
public class ObjectUtils {

    public static void checkNotNull(Object value, String message) {
        if (value == null) {
            throw new UCoinTechnicalException(message);
        }
    }
}
