package org.duniter.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by naivalf27 on 08/12/15.
 */
public class Format {

    public static final int DEFAULT_DECIMAL = 2;

    public static final String MAX_INTEGER = "9000000000000000000";

    private static final BigInteger MILLION     = new BigInteger(("1 000 000").replaceAll(" ",""));
    private static final BigInteger MILLIARD    = new BigInteger(("1 000 000 000").replaceAll(" ",""));
    private static final BigInteger BILLION     = new BigInteger(("1 000 000 000 000").replaceAll(" ",""));
    private static final BigInteger BILLIARD    = new BigInteger(("1 000 000 000 000 000").replaceAll(" ",""));
    private static final BigInteger TRILLION    = new BigInteger(("1 000 000 000 000 000 000").replaceAll(" ",""));
    private static final BigInteger TRILLIARD   = new BigInteger(("1 000 000 000 000 000 000 000").replaceAll(" ",""));

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

    public static final String CONTACT_PATH = "duniter://";
    public static final String SEPARATOR1 = ":";
    public static final String SEPARATOR2 = "@";

    public static final String UID = "uid";
    public static final String PUBLICKEY = "public_key";
    public static final String CURRENCY = "currency";

    public static final boolean SIMPLE = true;
    public static final boolean LONG = false;

    public static class Time{

        public static final int YEAR = 0;
        public static final int DAY = 1;
        public static final int HOUR = 2;
        public static final int MINUTE = 3;
        public static final int SECOND = 4;
        public static final int MILLI_SECOND = 5;
        public static final int IN_YEAR = 31557600;
        public static final int IN_DAY = 86400;
        public static final int IN_HOUR = 3600;
        public static final int IN_MINUTE = 60;

        public static BigDecimal secondToYear(Context context, BigDecimal second){
            int decimal = PreferenceManager.getDefaultSharedPreferences(context).getInt(Application.DECIMAL, DEFAULT_DECIMAL);
            return second.divide(new BigDecimal(IN_YEAR),decimal, RoundingMode.HALF_EVEN);
        }

        public static BigDecimal secondToDay(Context context, BigDecimal second){
            int decimal = PreferenceManager.getDefaultSharedPreferences(context).getInt(Application.DECIMAL, DEFAULT_DECIMAL);
            return second.divide(new BigDecimal(IN_DAY),decimal, RoundingMode.HALF_EVEN);
        }

        public static BigDecimal secondToHour(Context context, BigDecimal second){
            int decimal = PreferenceManager.getDefaultSharedPreferences(context).getInt(Application.DECIMAL, DEFAULT_DECIMAL);
            return second.divide(new BigDecimal(IN_HOUR),decimal, RoundingMode.HALF_EVEN);
        }

        public static BigDecimal secondToMinute(Context context, BigDecimal second){
            int decimal = PreferenceManager.getDefaultSharedPreferences(context).getInt(Application.DECIMAL, DEFAULT_DECIMAL);
            return second.divide(new BigDecimal(IN_MINUTE),decimal, RoundingMode.HALF_EVEN);
        }

        public static BigDecimal toSecond(Context context, BigDecimal val,int unit){
            int decimal = PreferenceManager.getDefaultSharedPreferences(context).getInt(Application.DECIMAL, DEFAULT_DECIMAL);
            switch (unit){
                case YEAR:
                    val = val.multiply(new BigDecimal(IN_YEAR));
                    break;
                case DAY:
                    val = val.multiply(new BigDecimal(IN_DAY));
                    break;
                case HOUR:
                    val = val.multiply(new BigDecimal(IN_HOUR));
                    break;
                case MINUTE:
                    val = val.multiply(new BigDecimal(IN_MINUTE));
                    break;
                case MILLI_SECOND:
                    val = val.divide(new BigDecimal(1000),decimal, RoundingMode.HALF_EVEN);
                    break;
            }
            return val;
        }

        public static BigDecimal secondToMilliSecond(BigDecimal second){
            return second.multiply(new BigDecimal(1000));
        }
    }

    public static class Currency{

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

        public static String unitCurrency(String currencyName){
            String[] tab = currencyName.split("_");
            String result ="";
            for(String s:tab){
                result += s.charAt(0);
            }
            result = result.toUpperCase();
            return result;
        }

        public static void changeUnit(
                final Context context,
                final String currencyName,
                final BigInteger classiqueValue,
                final BigInteger mUd,
                final int delay,
                final TextView currentAmount,
                final TextView defaultAmount,
                final String dir){

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    changeUnit(context,currencyName,classiqueValue,mUd,delay,currentAmount,defaultAmount,dir);
                }
            });

            int unit = Integer.parseInt(preferences.getString(Application.UNIT, Application.UNIT_CLASSIC + ""));
            int defaultUnit = Integer.parseInt(preferences.getString(Application.UNIT_DEFAULT, Application.UNIT_DU + ""));

            //        int unit = preferences.getInt(Application.UNIT, Application.UNIT_CLASSIC);
            //        int defaultUnit = preferences.getInt(Application.UNIT_DEFAULT, Application.UNIT_CLASSIC);

            if(currentAmount!=null) {
                switch (unit) {
                    case Application.UNIT_CLASSIC:
                        currentAmount.setText(dir.concat(quantitativeFormatter(context, classiqueValue, currencyName)));
//                        currentAmount.setOnLongClickListener(new View.OnLongClickListener() {
//                            @Override
//                            public boolean onLongClick(View v) {
//                                Toast.makeText(context,(new DecimalFormat("#,###")).format(classiqueValue),Toast.LENGTH_SHORT).show();
//                                return true;
//                            }
//                        });
                        break;
                    case Application.UNIT_DU:
                        currentAmount.setText(dir.concat(relativeFormatter(context,
                                quantitativeToRelative(context,classiqueValue,mUd))));
                        break;
                    case Application.UNIT_TIME:
                        BigDecimal timeValue = quantitativeToTime(context,classiqueValue,delay,mUd);
                        if (dir.equals("")) {
                            currentAmount.setText(Format.timeFormatter(context, timeValue));
                        } else {
                            currentAmount.setText(dir.concat("(").concat(timeFormatter(context, timeValue)).concat(")"));
                        }
                        break;
                }
            }
            if(defaultAmount!=null) {
                if (defaultUnit == unit) {
                    defaultAmount.setVisibility(View.GONE);
                } else {
                    defaultAmount.setVisibility(View.VISIBLE);
                    switch (defaultUnit) {
                        case Application.UNIT_CLASSIC:
                            defaultAmount.setText(quantitativeFormatter(context, classiqueValue, currencyName));
//                        defaultAmount.setOnLongClickListener(new View.OnLongClickListener() {
//                            @Override
//                            public boolean onLongClick(View v) {
//                                Toast.makeText(context, (new DecimalFormat("#,###")).format(classiqueValue), Toast.LENGTH_SHORT).show();
//                                return true;
//                            }
//                        });
                            break;
                        case Application.UNIT_DU:
                            defaultAmount.setText(relativeFormatter(context, quantitativeToRelative(context, classiqueValue, mUd)));
                            break;
                        case Application.UNIT_TIME:
                            defaultAmount.setText(timeFormatter(context, quantitativeToTime(context, classiqueValue, delay, mUd)));
                            break;
                    }
                }
            }
        }

        public static BigInteger relativeToQuantitative(Context context, BigDecimal du, BigInteger mUd){
            return du.multiply(new BigDecimal(mUd)).toBigInteger();
        }

        public static BigDecimal quantitativeToRelative(Context context, BigInteger coin, BigInteger mUd){
            int decimal = PreferenceManager.getDefaultSharedPreferences(context).getInt(Application.DECIMAL, DEFAULT_DECIMAL);
            return new BigDecimal(coin).divide(new BigDecimal(mUd),decimal, RoundingMode.HALF_EVEN);
        }

        public static BigDecimal quantitativeToTime(Context context, BigInteger coin, int delay, BigInteger mUd){
            int decimal = PreferenceManager.getDefaultSharedPreferences(context).getInt(Application.DECIMAL, DEFAULT_DECIMAL);
            return new BigDecimal(coin).multiply(new BigDecimal(delay)).divide(new BigDecimal(mUd),decimal, RoundingMode.HALF_EVEN);
        }

        public static BigInteger timeToQuantitative(Context context, BigDecimal time, int delay, BigInteger mUd){
            int decimal = PreferenceManager.getDefaultSharedPreferences(context).getInt(Application.DECIMAL, DEFAULT_DECIMAL);
            return time.multiply(new BigDecimal(mUd)).divide(new BigDecimal(delay),decimal, RoundingMode.HALF_EVEN).toBigInteger();
        }

    }

    public static String minifyPubkey(String pubkey) {
        return (pubkey == null || pubkey.length() < 6)? pubkey : pubkey.substring(0, 6);
    }

    public static String timeFormatter(Context context, BigDecimal amount){
        int decimal = PreferenceManager.getDefaultSharedPreferences(context).getInt(Application.DECIMAL, DEFAULT_DECIMAL);
        String result = "";

        BigInteger year = amount
                .divide(new BigDecimal(Time.IN_YEAR),8, RoundingMode.HALF_EVEN).toBigInteger();
        BigInteger day = (amount
                .divide(new BigDecimal(Time.IN_DAY),8, RoundingMode.HALF_EVEN))
                .subtract(new BigDecimal(year).multiply(new BigDecimal(365.25))).toBigInteger();
        BigInteger hour = ((amount
                .divide(new BigDecimal(Time.IN_HOUR),8, RoundingMode.HALF_EVEN))
                .subtract(new BigDecimal(day).multiply(new BigDecimal(24))))
                .subtract(new BigDecimal(year).multiply(new BigDecimal(8766))).toBigInteger();
        BigInteger minute = (((amount.divide(new BigDecimal(Time.IN_MINUTE),8, RoundingMode.HALF_EVEN))
                .subtract(new BigDecimal(hour).multiply(new BigDecimal(60))))
                .subtract(new BigDecimal(day).multiply(new BigDecimal(1440))))
                .subtract(new BigDecimal(year).multiply(new BigDecimal(525960))).toBigInteger();
        BigInteger second = amount
                .subtract(new BigDecimal(minute).multiply(new BigDecimal(Time.IN_MINUTE)))
                .subtract(new BigDecimal(hour).multiply(new BigDecimal(Time.IN_HOUR)))
                .subtract(new BigDecimal(day).multiply(new BigDecimal(Time.IN_DAY)))
                .subtract(new BigDecimal(year).multiply(new BigDecimal(Time.IN_YEAR))).toBigInteger();
        BigInteger msecond = (amount.multiply(new BigDecimal(1000)))
                .subtract(new BigDecimal(second).multiply(new BigDecimal(1000)))
                .subtract(new BigDecimal(minute).multiply(new BigDecimal(Time.IN_MINUTE * 1000)))
                .subtract(new BigDecimal(hour).multiply(new BigDecimal(Time.IN_HOUR * 1000)))
                .subtract(new BigDecimal(day).multiply(new BigDecimal(Time.IN_DAY * 1000)))
                .subtract(new BigDecimal(year).multiply((new BigDecimal(Time.IN_YEAR)).multiply(new BigDecimal(1000)))).toBigInteger();


        if (year.compareTo(BigInteger.ZERO) > 0) {
            result += Currency.formatWithSmartDecimal(year.longValue()) + context.getResources().getString(R.string.year);
            if (day.compareTo(BigInteger.ZERO) > 0)
                result += "  " + Currency.formatWithSmartDecimal(day.longValue()) + context.getResources().getString(R.string.day);
        }else {
            if (day.compareTo(BigInteger.ZERO) > 0)
                result += Currency.formatWithSmartDecimal(day.longValue()) + context.getResources().getString(R.string.day);
            if (hour.compareTo(BigInteger.ZERO) > 0 ||
                    day.compareTo(BigInteger.ZERO) > 0)
                result += "  " + Currency.formatWithSmartDecimal(hour.longValue()) + "h";
            if (minute.compareTo(BigInteger.ZERO) > 0 ||
                    hour.compareTo(BigInteger.ZERO) > 0 ||
                    day.compareTo(BigInteger.ZERO) > 0)
                result += "  " + Currency.formatWithSmartDecimal(minute.longValue()) + "min";
            if (second.compareTo(BigInteger.ZERO) > 0 &&
                    minute.compareTo(BigInteger.ZERO) >= 0 &&
                    day.compareTo(BigInteger.ZERO) == 0 &&
                    hour.compareTo(BigInteger.ZERO) == 0)
                result += "  " + Currency.formatWithSmartDecimal(second.longValue()) + "s";
            if (msecond.compareTo(BigInteger.ZERO) == 0 &&
                    second.compareTo(BigInteger.ZERO) == 0 &&
                    day.compareTo(BigInteger.ZERO) == 0 &&
                    hour.compareTo(BigInteger.ZERO) == 0 &&
                    minute.compareTo(BigInteger.ZERO) == 0 &&
                    amount.compareTo(BigDecimal.ZERO) >= 0 )
                result = "<1 ms";
            if (msecond.compareTo(BigInteger.ZERO) > 0 &&
                    second.compareTo(BigInteger.ZERO) >= 0 &&
                    day.compareTo(BigInteger.ZERO) == 0 &&
                    hour.compareTo(BigInteger.ZERO) == 0 &&
                    minute.compareTo(BigInteger.ZERO) == 0)
                result += "  " + Currency.formatWithSmartDecimal(msecond.longValue()) + "ms";
            if (msecond.compareTo(BigInteger.ZERO) ==0 &&
                    second.compareTo(BigInteger.ZERO) == 0 &&
                    day.compareTo(BigInteger.ZERO) == 0 &&
                    hour.compareTo(BigInteger.ZERO) == 0 &&
                    minute.compareTo(BigInteger.ZERO) == 0 &&
                    amount.compareTo(BigDecimal.ZERO) == 0)
                result = "0 ms";
        }


        return result;
    }

    public static String quantitativeFormatter(Context context, BigInteger amount,String currencyName){
        BigInteger result;
        String plus;
        if(amount.compareTo(TRILLIARD)>=1){
            result = amount.divide(TRILLION);
            plus = PREFIX_VALUE.get(TRILLION);
        }else {
            if (amount.compareTo(TRILLION) >= 1) {
                result = amount.divide(BILLIARD);
                plus = PREFIX_VALUE.get(BILLIARD);
            } else {
                if (amount.compareTo(BILLIARD) >= 1) {
                    result = amount.divide(BILLION);
                    plus = PREFIX_VALUE.get(BILLION);
                } else {
                    if (amount.compareTo(BILLION) >= 1) {
                        result = amount.divide(MILLIARD);
                        plus = PREFIX_VALUE.get(MILLIARD);
                    } else {
                        if (amount.compareTo(MILLIARD) >= 1) {
                            result = amount.divide(MILLION);
                            plus = PREFIX_VALUE.get(MILLION);
                        }else {
                            result = amount;
                            plus = "";
                        }
                    }
                }
            }
        }
        String formatClassic = "#,###";
        DecimalFormat formatter = new DecimalFormat(formatClassic);
        return formatter.format(result).concat(plus).concat(" ").concat(Currency.unitCurrency(currencyName));
    }

    public static String relativeFormatter(Context context, BigDecimal amount){
        int decimal = PreferenceManager.getDefaultSharedPreferences(context).getInt(Application.DECIMAL, DEFAULT_DECIMAL);
        String formatDu = ("%.").concat(String.valueOf(decimal)).concat("f");
        String zero = "0,";
        for(int i=0;i<decimal;i++){
            zero = zero.concat("0");
        }

        return String.format(formatDu, amount).concat(" ").concat(context.getResources().getString(R.string.ud));
    }


    public static String createUri(boolean simple, String uid, String publicKey, String currency) {
        String result;
        if(simple){
            result = publicKey;
        }else {
            result = CONTACT_PATH;
            if (uid.isEmpty() || uid.equals(" ")) {
                result = result.concat(SEPARATOR1);
            } else {
                result = result.concat(uid).concat(SEPARATOR1);
            }

            if (publicKey.isEmpty() || publicKey.equals(" ")) {
                result = result.concat(SEPARATOR2);
            } else {
                result = result.concat(publicKey).concat(SEPARATOR2);
            }

            if (!currency.isEmpty() && !currency.equals(" ")) {
                result = result.concat(currency);
            }
        }
        return result;
    }

    public static Map<String, String> parseUri(String uri){
        Map<String, String> result = new HashMap<>();
        if(uri.substring(0,CONTACT_PATH.length()).equals(CONTACT_PATH)){
            int index1 = uri.indexOf(SEPARATOR1,CONTACT_PATH.length());
            int index2 = uri.indexOf(SEPARATOR2,index1+SEPARATOR1.length());

            String uid = uri.substring(CONTACT_PATH.length(), index1);
            String publicKey = uri.substring(index1+SEPARATOR1.length(),index2);
            String currency = uri.substring(index2+SEPARATOR2.length());

            if(!uid.isEmpty()){
                result.put(UID,uid);
            }
            if(!publicKey.isEmpty()){
                result.put(PUBLICKEY,publicKey);
            }
            if(!currency.isEmpty()){
                result.put(CURRENCY,currency);
            }
        }else{
            result.put(PUBLICKEY,uri);
        }
        return result;
    }

    public static String expo(String str){
        String result ="";
        int dot = 0;
        int e = 0;
        if(str.contains("e+")){
            dot = str.indexOf(".");
            e = str.indexOf("e+");
            result = str.substring(0,dot);
            String bt = str.substring(dot+1,e);
            int val = Integer.parseInt(str.substring(e+2,str.length()));
            for(int i=0;i<val;i++){
                if(i>=bt.length()){
                    result+="0";
                }else {
                    result += bt.charAt(i);
                }
            }
        }else{
            if(str.contains(".")){
                result = str.substring(0,str.indexOf("."));
            }else {
                result = str;
            }
        }
        return result;
    }

    public static String isNull(String txt){
        return (txt==null || txt.isEmpty()) ? "" : txt;
    }
}
