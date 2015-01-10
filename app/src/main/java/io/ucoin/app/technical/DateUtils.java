package io.ucoin.app.technical;
import android.app.Activity;
import android.provider.Settings;
import android.text.TextUtils;

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
        return DEFAULT_DATE_FORMAT.format(new Date(timestamp));
    }
}
