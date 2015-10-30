package io.ucoin.app.technical;

import android.content.Context;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import io.ucoin.app.R;
import io.ucoin.app.model.local.Movement;

/**
 * Created by eis on 12/03/15.
 */
public class CurrencyUtils {

    public static final int maxDecimalNumber = 4;

    private static final int roundConversionCoef = (int)Math.pow(10, maxDecimalNumber);

    private static Locale currencyLocale = Locale.getDefault();
    private static DecimalFormat currencyFormatter = (DecimalFormat)NumberFormat.getCurrencyInstance(Locale.getDefault());
    private static String resultPartToIgnore = currencyFormatter.getDecimalFormatSymbols().getDecimalSeparator() + "00";

    public static void setDefaultLocale(final Locale locale) {
        currencyLocale = locale;
        // create the formatter from locale, but override the symbols
        currencyFormatter = (DecimalFormat)NumberFormat.getCurrencyInstance(locale);
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(locale);
        dfs.setCurrencySymbol("");
        currencyFormatter.setDecimalFormatSymbols(dfs);

        resultPartToIgnore = dfs.getDecimalSeparator() + "00";
    }

    public static long convertToCoin(final double valueInUD, final long ud) {
        return Math.round(valueInUD * ud);
    }

    public static double convertToUD(final long coins, final long ud) {
        return ((double)coins) / ud;
    }

    public static double convertCoinToTime(long coins, long lastUd, long delayInMs) {
        double val = (((double) coins * delayInMs) / lastUd);
        return val;
    }

    public static double convertCoinToTime(double coins, long lastUd, long delayInMs) {
        double val = ((coins * delayInMs) / lastUd);
        return val;
    }

    public static double convertTimeToCoin(long time, long lastUd, long delayInMs) {
        double val = (((double) time * lastUd) / delayInMs);
        return val;
    }
    public static long convertTimeToCoin(double time, long lastUd, long delayInMs) {
        long val = Math.round((time * lastUd) / delayInMs);
        return val;
    }

    public static double convertUdToTime(final double du,final long lastUd,final long delay) {
        return ((du*delay)/lastUd);
    }

    /**
     * TODO FMA a implementer dans la base du wallet
     * @param movementList
     * @param delay
     * @return
     */
    public static double calculCreditTimeWithoutForget(final List<Movement> movementList,final int delay){
        double result =0;

        for(Movement m : movementList){
           result += convertCoinToTime(m.getAmount(), m.getDividend(), delay);
        }

        return result;
    }

    public static String formatCoin(final long amount) {
        return formatWithSmartDecimal(amount);
    }

    public static String formatUD(final double amount) {
        return formatWithSmartDecimal(amount);
    }

    public static String formatTime(final Context context,final double amount) {
        return formatWithTime(context,amount);
    }

    /**
     * Format a credit using a currency format.
     * @param amount
     * @return
     */
    public static String format(long amount) {
        return currencyFormatter.format(amount);
    }

    /**
     * Format a credit using a currency format.
     * Unnecessary '.00' will be removed
     * @param amount
     * @return
     */
    public static String formatWithSmartDecimal(long amount) {
        if (amount < 0) {
            return "- " + formatWithSmartDecimal(-amount);
        }

        String result = currencyFormatter.format(amount);
        return result.replace(resultPartToIgnore, "");
    }

    /**
     * Format a credit using a currency format.
     * Unnecessary '.00' will be removed
     * @param amount
     * @return
     */
    public static String formatWithSmartDecimal(double amount) {
        String result = currencyFormatter.format(amount);
        return result.replace(resultPartToIgnore, "");
    }

    public static String formatWithTime(Context context,double amount) {
        String result = "";
        long tmp = 31557600;

        long year = (long) (amount / 31557600);
        long day = (long) ((amount / 86400) - (year * 365.25));
        long hour = (long) ((amount / 3600) - (day * 24) - (year * 8766));
        long minute = (long) ((amount / 60) - (hour * 60) - (day * 1440) - (year * 525960));
        long second = (long) (amount - (minute * 60) - (hour * 3600) - (day * 86400) - (year * 31557600));
        double msecond1 = (double) ((amount * 1000) - (second * 1000) - (minute * 60000) - (hour * 3600000));
        double msecond2 = (day * 86400000) - (year * (tmp * 1000));
        double msecond = msecond1 - msecond2;


        if (year > 0) {
            result += formatWithSmartDecimal(year) + context.getResources().getString(R.string.year) + "  ";
            if (day > 0) result += formatWithSmartDecimal(day) + context.getResources().getString(R.string.day);
        }else {
            if (day > 0) result += formatWithSmartDecimal(day) + context.getResources().getString(R.string.day)+"  ";
            if (hour > 0 || day > 0) result += formatWithSmartDecimal(hour) + "h  ";
            if (minute > 0 || hour > 0 || day > 0) result += formatWithSmartDecimal(minute) + "min  ";
            if (second > 0 && minute == 0 && day == 0 && hour == 0) result += formatWithSmartDecimal(second) + "s";
            if (msecond >= 1 && second == 0 && day == 0 && hour == 0 && minute == 0)
                result += formatWithSmartDecimal(msecond) + "ms";
            if (msecond < 1 && second == 0 && day == 0 && hour == 0 && minute == 0)
                result += ">1 ms";
        }


        return result;
    }

    /**
     * Format a credit using a short notation : M for million, and G for billion
     * @param amount
     * @return
     */
    public static String formatShort(long amount) {

        // Less than a million
        if (amount < 1000000) {
            return formatWithSmartDecimal(amount);
        }

        // Between 1 million and 1 billion
        String amountStr = Long.toString(amount);
        if (amount < 1000000000) {
            amountStr = amountStr.substring(0, amountStr.length() - 3);
            return formatWithSmartDecimal(Long.parseLong(amountStr) / 1000) + "M";
        }

        // More than 1 billion
        amountStr = amountStr.substring(0, amountStr.length() - 6);
        return formatWithSmartDecimal(Long.parseLong(amountStr) / 1000) + "G";
    }

    /**
     * Format a credit using a short notation : M for million, and G for billion
     * @param amount
     * @return
     */
    public static String formatShort(double amount) {

        // Less than a million
        if (amount < 1000000) {
            return formatWithSmartDecimal(amount);
        }

        // Between 1 million and 1 billion
        String amountStr = Long.toString(Math.round(amount));
        if (amount < 1000000000) {
            amountStr = amountStr.substring(0, amountStr.length() - 3);
            return formatShort(Long.parseLong(amountStr) / 1000) + "M";
        }

        // More than 1 billion
        amountStr = amountStr.substring(0, amountStr.length() - 6);
        return formatShort(Long.parseLong(amountStr) / 1000) + "G";
    }

    public static Double parse(String amount) {
        if (StringUtils.isBlank(amount)) {
            return null;
        }
        return Double.parseDouble(amount.trim().replace(',', '.'));
    }

    public static Long parseLong(String amount) {
        if (StringUtils.isBlank(amount)) {
            return null;
        }
        amount = amount.replaceAll("[\\s,]+", "");
        return Long.parseLong(amount);
    }

    /* -- Internal methods -- */


}
