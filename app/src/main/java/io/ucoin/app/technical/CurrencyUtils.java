package io.ucoin.app.technical;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

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

    public static String convertToUDAndFormat(final long coins, final long ud) {
        return formatUD(((double)coins) / ud);
    }

    public static String formatCoin(final long amount) {
        return formatWithSmartDecimal(amount);
    }

    public static String formatUD(final double amount) {
        return formatWithSmartDecimal(amount);
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
        if (amount < 0) {
            return "- " + formatWithSmartDecimal(-amount);
        }
        String result = currencyFormatter.format(amount);
        return result.replace(resultPartToIgnore, "");
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
