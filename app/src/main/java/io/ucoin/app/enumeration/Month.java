package io.ucoin.app.enumeration;


import android.content.Context;

import io.ucoin.app.R;

public enum Month {
    JANUARY(1),
    FEBRUARY(2),
    MARCH(3),
    APRIL(4),
    MAY(5),
    JUNE(6),
    JULY(7),
    AUGUST(8),
    SEPTEMBER(9),
    OCTOBER(10),
    NOVEMBER(11),
    DECEMBER(12),
    UNKNOWN(null);

    private Integer mMonth;

    Month(Integer month) {
        mMonth = month;
    }

    public static Month fromInt(Integer month) {
        if (month == null)
            return UNKNOWN;
        else if (month == 1)
            return JANUARY;
        else if (month == 2)
            return FEBRUARY;
        else if (month == 3)
            return MARCH;
        else if (month == 4)
            return APRIL;
        else if (month == 5)
            return MAY;
        else if (month == 6)
            return JUNE;
        else if (month == 7)
            return JULY;
        else if (month == 8)
            return AUGUST;
        else if (month == 9)
            return SEPTEMBER;
        else if (month == 10)
            return OCTOBER;
        else if (month == 11)
            return NOVEMBER;
        else if (month == 12)
            return DECEMBER;
        else
            return UNKNOWN;
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(Context context) {
        if (context == null) {
            throw new NullPointerException("Context is null");
        }
        if (mMonth == null)
            return context.getString(R.string.UNK);
        else if (mMonth == 1)
            return context.getString(R.string.january);
        else if (mMonth == 2)
            return context.getString(R.string.february);
        else if (mMonth == 3)
            return context.getString(R.string.march);
        else if (mMonth == 4)
            return context.getString(R.string.april);
        else if (mMonth == 5)
            return context.getString(R.string.may);
        else if (mMonth == 6)
            return context.getString(R.string.june);
        else if (mMonth == 7)
            return context.getString(R.string.july);
        else if (mMonth == 8)
            return context.getString(R.string.august);
        else if (mMonth == 9)
            return context.getString(R.string.september);
        else if (mMonth == 10)
            return context.getString(R.string.october);
        else if (mMonth == 11)
            return context.getString(R.string.november);
        else if (mMonth == 12)
            return context.getString(R.string.december);
        else
            return context.getString(R.string.UNK);
    }
}