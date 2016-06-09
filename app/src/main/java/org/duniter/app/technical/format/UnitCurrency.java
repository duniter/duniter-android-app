package org.duniter.app.technical.format;

import android.preference.PreferenceManager;

import org.duniter.app.Application;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Created by naivalf27 on 08/06/16.
 */
public class UnitCurrency{

    public static BigDecimal quantitatif_relatif(BigInteger quantitatif,BigInteger dividend){
        return new BigDecimal(quantitatif).divide(new BigDecimal(dividend),8, RoundingMode.HALF_EVEN);
    }

    public static long quantitatif_time(BigInteger quantitatif,BigInteger dividend, long delay){
        return new BigDecimal(quantitatif).multiply(new BigDecimal(delay * 1000L)).divide(new BigDecimal(dividend),8, RoundingMode.HALF_EVEN).longValue();
    }

    public static BigInteger relatif_quantitatif(BigDecimal relatif, BigInteger dividend){
        return relatif.multiply(new BigDecimal(dividend)).toBigInteger();
    }

    public static BigInteger time_quantitatif(long timeinMilliSecond,BigInteger dividend, long delay){
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
