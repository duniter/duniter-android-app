package io.ucoin.app.enumeration;

import android.content.Context;

import io.ucoin.app.R;

public enum DayOfWeek {
    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    UNKNOWN(null);

    private Integer mDay;

    DayOfWeek(Integer day) {
        mDay = day;
    }

    public static DayOfWeek fromInt(Integer day, boolean mondayInFirst) {
        if(mondayInFirst) {
            if (day == 0) return MONDAY;
            else if (day == 1) return TUESDAY;
            else if (day == 2) return WEDNESDAY;
            else if (day == 3) return THURSDAY;
            else if (day == 4) return FRIDAY;
            else if (day == 5) return SATURDAY;
            else if (day == 6) return SUNDAY;
            else return UNKNOWN;
        }else {
            if (day == 0) return SUNDAY;
            else if (day == 1) return MONDAY;
            else if (day == 2) return TUESDAY;
            else if (day == 3) return WEDNESDAY;
            else if (day == 4) return THURSDAY;
            else if (day == 5) return FRIDAY;
            else if (day == 6) return SATURDAY;
            else return UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(Context context) {
        if (context == null) {
            throw new NullPointerException("Context is null");
        }

        if(mDay == null) {
            return context.getString(R.string.UNK);
        }
        else if (mDay == 0)
            return context.getString(R.string.SUN);
        else if (mDay == 1)
            return context.getString(R.string.MON);
        else if (mDay == 2)
            return context.getString(R.string.TUE);
        else if (mDay == 3)
            return context.getString(R.string.WED);
        else if (mDay == 4)
            return context.getString(R.string.THU);
        else if (mDay == 5)
            return context.getString(R.string.FRI);
        else if (mDay == 6)
            return context.getString(R.string.SAT);
        else
            return context.getString(R.string.UNK);
    }
}