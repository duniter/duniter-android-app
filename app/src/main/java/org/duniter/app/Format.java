package org.duniter.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.TextView;

import org.duniter.app.technical.AmountPair;
import org.duniter.app.technical.format.Contantes;
import org.duniter.app.technical.format.Formater;
import org.duniter.app.technical.format.UnitCurrency;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by naivalf27 on 08/12/15.
 */
public class Format {
    //USE DATE for time formater
    public static final boolean SIMPLE = true;
    public static final boolean LONG = false;

    public static void initUnit(Context context, TextView textView, long quantitatif, int base, long delay, long dividend, int baseDividend, boolean isFirstAmount, String currencyName){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int decimal = preferences.getInt(Application.DECIMAL,4);
        int unit = isFirstAmount ?
                Integer.parseInt(preferences.getString(Application.UNIT, String.valueOf(Application.UNIT_DU))):
                Integer.parseInt(preferences.getString(Application.UNIT_DEFAULT, String.valueOf(Application.UNIT_CLASSIC)));

        quantitatif = Format.convertBase(quantitatif,base,baseDividend);

        switch (unit){
            case Application.UNIT_CLASSIC:
                textView.setText(Formater.quantitatifFormatter(quantitatif,currencyName));
                break;
            case Application.UNIT_DU:
                double amount = UnitCurrency.quantitatif_relatif(quantitatif,base,dividend,baseDividend);
                textView.setText(Formater.relatifFormatter(context,decimal,amount));
                break;
            case Application.UNIT_TIME:
                long time = UnitCurrency.quantitatif_time(quantitatif,base,dividend,baseDividend, delay);
                textView.setText(Formater.timeFormatterV2(context,time));
                break;
        }
    }

    public static long convertBase(long value, int base, int newBase){
        long result = value;
        if (newBase!=base){
            result = Double.valueOf((double) value * Math.pow(10, base - newBase)).longValue();
        }
        return result;
    }

    public static String minifyPubkey(String pubkey) {
        return (pubkey == null || pubkey.length() < 6)? pubkey : pubkey.substring(0, 6);
    }

    public static String createUri(boolean simple, String uid, String publicKey, String currency) {
        String result;
        if(simple){
            result = publicKey;
        }else {
            result = Contantes.CONTACT_PATH;
            if (uid.isEmpty() || uid.equals(" ")) {
                result = result.concat(Contantes.SEPARATOR1);
            } else {
                result = result.concat(uid).concat(Contantes.SEPARATOR1);
            }

            if (publicKey.isEmpty() || publicKey.equals(" ")) {
                result = result.concat(Contantes.SEPARATOR2);
            } else {
                result = result.concat(publicKey).concat(Contantes.SEPARATOR2);
            }

            if (!currency.isEmpty() && !currency.equals(" ")) {
                result = result.concat(currency);
            }
        }
        return result;
    }

    public static Map<String, String> parseUri(String uri){
        Map<String, String> result = new HashMap<>();
        if(uri.substring(0,Contantes.CONTACT_PATH.length()).equals(Contantes.CONTACT_PATH)){
            int index1 = uri.indexOf(Contantes.SEPARATOR1,Contantes.CONTACT_PATH.length());
            int index2 = uri.indexOf(Contantes.SEPARATOR2,index1+Contantes.SEPARATOR1.length());

            String uid = uri.substring(Contantes.CONTACT_PATH.length(), index1);
            String publicKey = uri.substring(index1+Contantes.SEPARATOR1.length(),index2);
            String currency = uri.substring(index2+Contantes.SEPARATOR2.length());

            if(!uid.isEmpty()){
                result.put(Contantes.UID,uid);
            }
            if(!publicKey.isEmpty()){
                result.put(Contantes.PUBLICKEY,publicKey);
            }
            if(!currency.isEmpty()){
                result.put(Contantes.CURRENCY,currency);
            }
        }else{
            result.put(Contantes.PUBLICKEY,uri);
        }
        return result;
    }

    public static String isNull(String txt){
        return (txt==null || txt.isEmpty()) ? "" : txt;
    }
}
