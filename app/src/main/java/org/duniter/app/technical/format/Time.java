package org.duniter.app.technical.format;

import android.content.Context;
import android.preference.PreferenceManager;

import org.duniter.app.Application;
import org.duniter.app.Format;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Created by naivalf27 on 08/06/16.
 */
public class Time {

    public static final int YEAR = 0;
    public static final int DAY = 1;
    public static final int HOUR = 2;
    public static final int MINUTE = 3;
    public static final int SECOND = 4;
    public static final int MILLI_SECOND = 5;

    public static final long IN_YEAR = 31557600000L;
    public static final long IN_DAY = 86400000L;
    public static final long IN_HOUR = 3600000L;
    public static final long IN_MINUTE = 60000L;
    public static final long IN_SECOND = 1000L;

    public static long milliSecondToYear(long timeInMilliSecond){
        return timeInMilliSecond/IN_YEAR;
    }

    public static long milliSecondToDay(long timeInMilliSecond){
        return timeInMilliSecond/IN_DAY;
    }

    public static long milliSecondToHour(long timeInMilliSecond){
        return timeInMilliSecond/IN_HOUR;
    }

    public static long milliSecondToMinute(long timeInMilliSecond){
        return timeInMilliSecond/IN_MINUTE;
    }

    public static long milliSecondToSeconde(long timeInMilliSecond){
        return timeInMilliSecond/IN_SECOND;
    }

    public static long toMilliSecond(long val, int unit){
        switch (unit){
            case YEAR:
                val = val * IN_YEAR;
                break;
            case DAY:
                val = val * IN_DAY;
                break;
            case HOUR:
                val = val * IN_HOUR;
                break;
            case MINUTE:
                val = val * IN_MINUTE;
                break;
            case SECOND:
                val = val * IN_SECOND;
                break;
        }
        return val;
    }
}
