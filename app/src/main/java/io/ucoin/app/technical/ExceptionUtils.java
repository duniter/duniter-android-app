package io.ucoin.app.technical;

/**
 * Created by eis on 01/03/15.
 */
public class ExceptionUtils {

    public static String getMessage(Throwable t) {
        String message = t.getMessage();
        if (message == null || message.length() == 0) {
            return t.getClass().getSimpleName();
        }
        return message;
    }
}
