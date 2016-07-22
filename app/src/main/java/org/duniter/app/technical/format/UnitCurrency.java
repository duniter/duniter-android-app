package org.duniter.app.technical.format;

import android.preference.PreferenceManager;

import org.duniter.app.Application;
import org.duniter.app.technical.AmountPair;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Created by naivalf27 on 08/06/16.
 */
public class UnitCurrency{

    public static double quantitatif_relatif(final long quantitatif,final int base,final long dividend,final int baseDividend){
        int b = base-baseDividend;
        double relatif = (double)quantitatif/(double)dividend;
        return relatif * Math.pow(10,b);
    }

    public static long quantitatif_time(final long quantitatif,final int base,final long dividend,final int baseDividend,final long delay){
        double relatif = quantitatif_relatif(quantitatif,base,dividend,baseDividend);

        Double time = relatif *(double)delay*1000d;

        return time.longValue();
    }

    public static AmountPair relatif_quantitatif(final double relatif, final int base, final long dividend, final int baseDividend){
        int b = base + baseDividend;
        Double quantitatif = relatif*(double)dividend;
        return new AmountPair(quantitatif.longValue(),baseDividend);
    }

    public static AmountPair time_quantitatif(final long timeinMilliSecond,final long dividend, final int baseDividend, long delay){

        int b = baseDividend;

        Double quantitatif = ((double)timeinMilliSecond / ((double)delay * 1000d * Math.pow(10,b))) * (double)dividend;

        return new AmountPair(quantitatif.longValue(),b);
    }

    public static BigDecimal big_quantitatif_relatif(BigInteger quantitatif, BigInteger dividend){
        return new BigDecimal(quantitatif).divide(new BigDecimal(dividend),8, RoundingMode.HALF_EVEN);
    }
    public static long big_quantitatif_time(BigInteger quantitatif, BigInteger dividend,final long delay){
        return new BigDecimal(quantitatif).multiply(new BigDecimal(delay * 1000L)).divide(new BigDecimal(dividend),8, RoundingMode.HALF_EVEN).longValue();
    }
    public static BigInteger big_relatif_quantitatif(BigDecimal relatif, BigInteger dividend){
        return relatif.multiply(new BigDecimal(dividend)).toBigInteger();
    }
    public static BigInteger big_time_quantitatif(long timeinMilliSecond,BigInteger dividend, long delay){
        return new BigDecimal(timeinMilliSecond).multiply(new BigDecimal(dividend)).divide(new BigDecimal(delay * 1000L),8, RoundingMode.HALF_EVEN).toBigInteger();
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
}
