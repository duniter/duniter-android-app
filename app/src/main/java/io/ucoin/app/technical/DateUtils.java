package io.ucoin.app.technical;

import java.util.Date;

/**
 * Helper class for date
 * @author Benoit Lavenier <benoit.lavenier@e-is.pro>
 * @since 1.0
 */
public class DateUtils {

	protected DateUtils() {
		// Helper class
	}
	
	public static long getTimestamp(Date date) {
		return date.getTime();
	}
}
