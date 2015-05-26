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

    private static long CACHE_REFRESH_TIME = -1;
    private static String YEAR_AS_STRING_CACHE;
    private static long FIRST_DAY_OF_YEAR_CACHE;
    private static long TODAY_CACHE;
    private static long YESTERDAY_CACHE;
    private static long FIRST_DAY_OF_WEEK_CACHE;

    static {
        refreshDateCache();
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

        // Make sure cached variable are up to date
        refreshDateCache();

        long timeInMillis = timestamp * 1000;
        Date date =  new Date(timeInMillis);

        // Last year or before
        if (timeInMillis < FIRST_DAY_OF_YEAR_CACHE) {
            return DEFAULT_SHORT_DATE_FORMAT.format(date);
        }

        // This year
        if (timeInMillis < YESTERDAY_CACHE) {
            return DEFAULT_MEDIUM_DATE_FORMAT.format(date).replace(YEAR_AS_STRING_CACHE, "");
        }

        // TODO : This week
        // This week
        //if (timeInMillis < FIRST_DAY_OF_WEEK_CACHE) {
        //    return DEFAULT_LONG_DATE_FORMAT.format(date).replace(YEAR_AS_STRING_CACHE, "");
        //}

        // yesterday
        if (timeInMillis < TODAY_CACHE) {
            return context.getString(R.string.yesterday)+ " " + DEFAULT_TIME_FORMAT.format(date);
        }

        return DEFAULT_TIME_FORMAT.format(date);
    }

    public static String formatFriendlyTime(Context context, long timeInMillis) {
        if (timeInMillis <= 0) {
            return null;
        }

        // more than 1 min
        if (timeInMillis > 1000 * 60) {
            // TODO BLA
            return Long.toString(timeInMillis) + " ms";
        }

        // more than 1 seconds
        if (timeInMillis > 1000) {
            return Integer.toString((int)Math.floor(timeInMillis / 1000)) + "s";
        }

        // less than a seconds
        return Long.toString(timeInMillis) + " ms";
    }

    public static long getCurrentTimestampSeconds() {
        return (long)Math.floor(System.currentTimeMillis() / 1000);
    }

    public static long toTimestamp(Date aDate) {
        return (long)Math.floor(aDate.getTime() / 1000);
    }

    public static long toTimestamp(long timestampInMillis) {
        return (long)Math.floor(timestampInMillis / 1000);
    }

    /* -- internal  methods -- */

    protected static void refreshDateCache() {
        if (CACHE_REFRESH_TIME != -1
                && (System.currentTimeMillis() - CACHE_REFRESH_TIME) < 300000/*5min*/) {
            return;
        }

        // Compute today à 0 hour
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        TODAY_CACHE = calendar.getTimeInMillis();

        // Compute yesterday at 0 hour
        calendar.add(Calendar.DATE, -1);
        YESTERDAY_CACHE = calendar.getTimeInMillis();

        // Compute first day of week, at 0 hour
        calendar.setTimeInMillis(TODAY_CACHE); // revert to today
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        FIRST_DAY_OF_WEEK_CACHE = calendar.getTimeInMillis();

        // Compute yesterday à 0 hour
        calendar.setTimeInMillis(TODAY_CACHE); // revert to today
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        FIRST_DAY_OF_YEAR_CACHE = calendar.getTimeInMillis();

        // The actual year
        YEAR_AS_STRING_CACHE = String.valueOf(calendar.get(Calendar.YEAR));

        // update the cache time
        CACHE_REFRESH_TIME = System.currentTimeMillis();
    }
}
