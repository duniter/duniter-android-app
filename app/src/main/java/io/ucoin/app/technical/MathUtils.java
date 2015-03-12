package io.ucoin.app.technical;

/**
 * Created by eis on 12/03/15.
 */
public class MathUtils {

    public static final int maxDecimalNumber = 2;

    private static final int roundConversionCoef = (int)Math.pow(10, maxDecimalNumber);

    public static long convertToCoin(final double valueInUD, final int ud) {
        return Math.round(valueInUD * ud);
    }

    public static double convertToUD(final int coins, final int ud) {
        return Math.round(coins / ud * roundConversionCoef) / roundConversionCoef;
    }

    /**
     * Format a credit using a short notation : M for million, and G for billion
     * @param amount
     * @return
     */
    public static String formatShort(long amount) {
        // Less than a million
        if (amount < 1000000) {
            return Long.toString(amount);
        }

        // Between 1 million and 1 billion
        String amountStr = Long.toString(amount);
        if (amount < 1000000000) {
            return amountStr.substring(0, amountStr.length() - 6) + "M";
        }

        // More than 1 billion
        return amountStr.substring(0, amountStr.length() - 9) + "G";
    }

    /**
     * Format a credit using a short notation : M for million, and G for billion
     * @param amount
     * @return
     */
    public static String formatShort(double amount) {
        // Less than a million
        if (amount < 1000000) {
            return Double.toString(Math.round(amount * roundConversionCoef) / roundConversionCoef);
        }

        // Between 1 million and 1 billion
        String amountStr = Long.toString(Math.round(amount));
        if (amount < 1000000000) {
            return amountStr.substring(0, amountStr.length() - 6) + "M";
        }

        // More than 1 billion
        return amountStr.substring(0, amountStr.length() - 9) + "G";
    }
}
