package io.ucoin.app.technical;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper class for date
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>
 * @since 1.0
 */
public class DateUtils {

    private static DateFormat DEFAULT_MEDIUM_DATE_FORMAT = new SimpleDateFormat();
    private static DateFormat DEFAULT_LONG_DATE_FORMAT = new SimpleDateFormat();
    private static DateFormat DEFAULT_SHORT_DATE_FORMAT = new SimpleDateFormat();

	protected DateUtils() {
		// Helper class
	}

    public static void setDefaultMediumDateFormat(DateFormat defaultDateFormat) {
        DEFAULT_MEDIUM_DATE_FORMAT = defaultDateFormat;
    }

    public static void setDefaultLongDateFormat(DateFormat defaultDateFormat) {
        DEFAULT_LONG_DATE_FORMAT = defaultDateFormat;
    }
    public static void setDefaultShortDateFormat(DateFormat defaultDateFormat) {
        DEFAULT_SHORT_DATE_FORMAT = defaultDateFormat;
    }


    public static String format(Date date) {
        return DEFAULT_MEDIUM_DATE_FORMAT.format(date);
    }

    public static String format(long timestamp) {
        return timestamp > 0
                ? DEFAULT_MEDIUM_DATE_FORMAT.format(new Date(timestamp * 1000))
                : null;
    }
    public static String formatShort(long timestamp) {
        return timestamp > 0
                ? DEFAULT_SHORT_DATE_FORMAT.format(new Date(timestamp * 1000))
                : null;
    }

    public static String formatLongFormat(long timestamp) {
        return timestamp > 0
                ? DEFAULT_LONG_DATE_FORMAT.format(new Date(timestamp * 1000))
                : null;
    }

    public static long getCurrentTimestamp() {
        return (long)Math.floor(System.currentTimeMillis() / 1000);
    }

    public static long toTimestamp(Date aDate) {
        return (long)Math.floor(aDate.getTime() / 1000);
    }

    public static long toTimestamp(long timestampInMillis) {
        return (long)Math.floor(timestampInMillis / 1000);
    }
}
