package org.duniter.app.technical.format;

import android.content.Context;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.text.format.DateUtils;

import org.duniter.app.Application;
import org.duniter.app.R;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by naivalf27 on 08/06/16.
 */
public class Formater {
    public static final String MAX_INTEGER = "9000000000000000000";

    public static final BigInteger MILLION     = new BigInteger(("1 000 000").replaceAll(" ",""));
    public static final BigInteger MILLIARD    = new BigInteger(("1 000 000 000").replaceAll(" ",""));
    public static final BigInteger BILLION     = new BigInteger(("1 000 000 000 000").replaceAll(" ",""));
    public static final BigInteger BILLIARD    = new BigInteger(("1 000 000 000 000 000").replaceAll(" ",""));
    public static final BigInteger TRILLION    = new BigInteger(("1 000 000 000 000 000 000").replaceAll(" ",""));
    public static final BigInteger TRILLIARD   = new BigInteger(("1 000 000 000 000 000 000 000").replaceAll(" ",""));

    private static final Map<BigInteger,String> PREFIX_VALUE;
    static{
        PREFIX_VALUE = new HashMap<>();
        PREFIX_VALUE.put(MILLION," M");    //Million
        PREFIX_VALUE.put(MILLIARD," G");    //Milliard
        PREFIX_VALUE.put(BILLION," T");   //Billion
        PREFIX_VALUE.put(BILLIARD," P");   //Billiard
        PREFIX_VALUE.put(TRILLION," E");   //Trillion
        PREFIX_VALUE.put(TRILLIARD," Z");   //Trilliard
    }

    public static String quantitatifFormatter(long quantitatif, String currencyName){
        String formatClassic = "#,###";
        DecimalFormat formatter = new DecimalFormat(formatClassic);
        return formatter.format(quantitatif).concat(" ").concat(UnitCurrency.unitCurrency(currencyName));
    }

    public static String big_quantitatifFormatter(BigInteger quantitatif, String currencyName){
        BigInteger result;
        String plus = "";
        if(quantitatif.compareTo(TRILLIARD)>=1){
            result = quantitatif.divide(TRILLION);
            plus = PREFIX_VALUE.get(TRILLION);
        }else {
            if (quantitatif.compareTo(TRILLION) >= 1) {
                result = quantitatif.divide(BILLIARD);
                plus = PREFIX_VALUE.get(BILLIARD);
            } else {
                if (quantitatif.compareTo(BILLIARD) >= 1) {
                    result = quantitatif.divide(BILLION);
                    plus = PREFIX_VALUE.get(BILLION);
                } else {
                    if (quantitatif.compareTo(BILLION) >= 1) {
                        result = quantitatif.divide(MILLIARD);
                        plus = PREFIX_VALUE.get(MILLIARD);
                    } else {
                        if (quantitatif.compareTo(MILLIARD) >= 1) {
                            result = quantitatif.divide(MILLION);
                            plus = PREFIX_VALUE.get(MILLION);
                        }else {
                            result = quantitatif;
                            plus = "";
                        }
                    }
                }
            }
        }
        String formatClassic = "#,###";
        DecimalFormat formatter = new DecimalFormat(formatClassic);
        return formatter.format(result).concat(plus).concat(" ").concat(UnitCurrency.unitCurrency(currencyName));
    }

    public static String big_relatifFormatter(Context context, int decimal, BigDecimal relatif){
        String formatDu = ("%.").concat(String.valueOf(decimal)).concat("f");
        String zero = "0,";
        for(int i=0;i<decimal;i++){
            zero = zero.concat("0");
        }

        return String.format(formatDu, relatif).concat(" ").concat(context.getResources().getString(R.string.ud));
    }

    public static String timeFormatterV2(Context context, long allMilli){
        String result = "";
        String plus = "";
        if (allMilli<0){
            plus = "-";
            allMilli = allMilli* (-1);
        }

        long year = allMilli/31557600000L;
        long day = (allMilli%31557600000L)/86400000;
        long hour = ((allMilli%31557600000L)%86400000)/3600000;
        long minute = (((allMilli%31557600000L)%86400000)%3600000)/60000;
        long second = ((((allMilli%31557600000L)%86400000)%3600000)%60000)/1000;
        long milliSecond = ((((allMilli%31557600000L)%86400000)%3600000)%60000)%1000;

        if (year > 0) {
            result += formatWithSmartDecimal(year) + context.getResources().getString(R.string.year);
            if (day > 0)
                result += "  " + formatWithSmartDecimal(day) + context.getResources().getString(R.string.day);
        }else {
            if (day > 0)
                result += formatWithSmartDecimal(day) + context.getResources().getString(R.string.day);
            if (hour > 0 ||
                    day > 0)
                result += "  " + formatWithSmartDecimal(hour) + "h";
            if (minute > 0 ||
                    hour > 0 ||
                    day > 0)
                result += "  " + formatWithSmartDecimal(minute) + "min";

            if (day == 0){
                if (hour == 0){
                    if (minute >= 0){
                        if (second > 0){
                            result += "  " + formatWithSmartDecimal(second) + "s";
                        }
                    }
                    if (minute == 0){
                        if (second >= 0){
                            if (milliSecond > 0){
                                result += "  " + formatWithSmartDecimal(milliSecond) + "ms";
                            }
                        }

                        if (second == 0){
                            if (milliSecond == 0){
                                if (allMilli > 0){
                                    result = "<1 ms";
                                }else{
                                    result = "0 ms";
                                }
                            }
                        }
                    }
                }
            }
        }

        return plus + result;
    }

    public static String relatifFormatter(Context context, int decimal, double relatif){
        String formatDu = ("%.").concat(String.valueOf(decimal)).concat("f");
        String zero = "0,";
        for(int i=0;i<decimal;i++){
            zero = zero.concat("0");
        }

        return String.format(formatDu, relatif).concat(" ").concat(context.getResources().getString(R.string.ud));
    }

    public static String formatWithSmartDecimal(long amount) {
        if (amount < 0) {
            return "- " + formatWithSmartDecimal(-amount);
        }
        DecimalFormat currencyFormatter = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
        String resultPartToIgnore = currencyFormatter.getDecimalFormatSymbols().getDecimalSeparator() + "00";

        //String result = currencyFormatter.format(amount);
        String result = String.valueOf(amount);
        return result.replace(resultPartToIgnore, "");
    }
}
