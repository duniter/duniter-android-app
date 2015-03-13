package io.ucoin.app.technical;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.ucoin.app.R;

/**
 * Helper class for date
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>
 * @since 1.0
 */
public class DateUtils {

    private static DateFormat DEFAULT_MEDIUM_DATE_FORMAT = new SimpleDateFormat();
    private static DateFormat DEFAULT_LONG_DATE_FORMAT = new SimpleDateFormat();
    private static DateFormat DEFAULT_SHORT_DATE_FORMAT = new SimpleDateFormat();
    private static DateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat();

    private static long COMPARE_CACHE_TIME = -1;
    private static String COMPARE_YEAR;
    private static long COMPARE_THIS_YEAR;
    private static long COMPARE_TODAY;
    private static long COMPARE_YESTERDAY;
    static {
        refreshCompareVars();
    }

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

    public static void setDefaultTimeFormat(DateFormat defaultTimeFormat) {
        DEFAULT_TIME_FORMAT = defaultTimeFormat;
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

    public static String formatTime(long timestamp) {
        return timestamp > 0
                ? DEFAULT_TIME_FORMAT.format(new Date(timestamp * 1000))
                : null;
    }

    public static String formatFriendlyDateTime(Context context, long timestamp) {
        if (timestamp <= 0) {
            return null;
        }
        long timeInMillis = timestamp * 1000;
        Date date =  new Date(timeInMillis);

        // Last year
        if (timeInMillis < COMPARE_THIS_YEAR) {
            return DEFAULT_SHORT_DATE_FORMAT.format(date);
        }

        // This year
        if (timeInMillis < COMPARE_YESTERDAY) {
            return DEFAULT_MEDIUM_DATE_FORMAT.format(date).replace(COMPARE_YEAR, "");
        }

        // TODO : This week

        // yesterday
        if (timeInMillis < COMPARE_TODAY) {
            return context.getString(R.string.yesterday)+ " " + DEFAULT_TIME_FORMAT.format(date);
        }

        return DEFAULT_TIME_FORMAT.format(date);
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

    /* -- internal  methods -- */

    // TODO : this method should be called some times !!
    // when opening wallet fragment ?
    protected static void refreshCompareVars() {
        if (COMPARE_CACHE_TIME != -1
                && (System.currentTimeMillis() - COMPARE_CACHE_TIME) < 300000/*5min*/) {
            return;
        }

        // Compute today à 0 hour
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        COMPARE_TODAY = calendar.getTimeInMillis();

        // Compute yesterday à 0 hour
        calendar.add(Calendar.DATE, -1);
        COMPARE_YESTERDAY = calendar.getTimeInMillis();

        // Compute yesterday à 0 hour
        calendar.add(Calendar.DATE, 1); // revert to today
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        COMPARE_THIS_YEAR = calendar.getTimeInMillis();

        // The actual year
        COMPARE_YEAR = String.valueOf(calendar.get(Calendar.YEAR));

        // update the cache time
        COMPARE_CACHE_TIME = System.currentTimeMillis();
    }
}
