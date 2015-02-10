package io.ucoin.app.technical;

/**
 * Created by eis on 22/12/14.
 */
public class ObjectUtils {

    public static void checkNotNull(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
    }

    public static void checkNotNull(Object value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
    }


    public static void checkArgument(boolean value, String message) {
        if (!value) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkArgument(boolean value) {
        if (!value) {
            throw new IllegalArgumentException();
        }
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if ((o1 != null && o2 == null) ||(o1 == null && o2 != null)) {
            return false;
        }
        return o1.equals(o2);
    }
}
