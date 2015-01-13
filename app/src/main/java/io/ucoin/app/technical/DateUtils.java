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

    private static DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat();

	protected DateUtils() {
		// Helper class
	}

    public static void setDefaultDateFormat(DateFormat defaultDateFormat) {
        DEFAULT_DATE_FORMAT = defaultDateFormat;
    }


    public static String format(Date date) {
        return DEFAULT_DATE_FORMAT.format(date);
    }
    public static String format(long timestamp) {
        return DEFAULT_DATE_FORMAT.format(new Date(timestamp * 1000));
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
