package org.duniter.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.TextView;

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
    public static final int DEFAULT_DECIMAL = 2;
    public static final boolean SIMPLE = true;
    public static final boolean LONG = false;

    public static void initUnit(Context context, TextView textView, BigInteger quantitatif, long delay, BigInteger dividend, boolean isFirstAmount, String currencyName){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int decimal = preferences.getInt(Application.DECIMAL,DEFAULT_DECIMAL);
        int unit = isFirstAmount ?
                Integer.parseInt(preferences.getString(Application.UNIT, String.valueOf(Application.UNIT_CLASSIC))):
                Integer.parseInt(preferences.getString(Application.UNIT_DEFAULT, String.valueOf(Application.UNIT_DU)));

        switch (unit){
            case Application.UNIT_CLASSIC:
                textView.setText(Formater.quantitatifFormatter(quantitatif,currencyName));
                break;
            case Application.UNIT_DU:
                BigDecimal relatif = UnitCurrency.quantitatif_relatif(quantitatif,dividend);
                textView.setText(Formater.relatifFormatter(context,decimal,relatif));
                break;
            case Application.UNIT_TIME:
                long time = UnitCurrency.quantitatif_time(quantitatif,dividend,delay);
                textView.setText(Formater.timeFormatterV2(context,time));
                break;
        }
    }

    public static class Currency{

//        public static void changeUnit(
//                final Context context,
//                final String currencyName,
//                final BigInteger classiqueValue,
//                final BigInteger mUd,
//                final int delay,
//                final TextView currentAmount,
//                final TextView defaultAmount,
//                final String dir){
//
//            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//            preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
//                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
//                    changeUnit(context,currencyName,classiqueValue,mUd,delay,currentAmount,defaultAmount,dir);
//                }
//            });
//
//            int unit = Integer.parseInt(preferences.getString(Application.UNIT, Application.UNIT_CLASSIC + ""));
//            int defaultUnit = Integer.parseInt(preferences.getString(Application.UNIT_DEFAULT, Application.UNIT_DU + ""));
//
//            //        int unit = preferences.getInt(Application.UNIT, Application.UNIT_CLASSIC);
//            //        int defaultUnit = preferences.getInt(Application.UNIT_DEFAULT, Application.UNIT_CLASSIC);
//
//            if(currentAmount!=null) {
//                switch (unit) {
//                    case Application.UNIT_CLASSIC:
//                        currentAmount.setText(dir.concat(quantitativeFormatter(context, classiqueValue, currencyName)));
////                        currentAmount.setOnLongClickListener(new View.OnLongClickListener() {
////                            @Override
////                            public boolean onLongClick(View v) {
////                                Toast.makeText(context,(new DecimalFormat("#,###")).format(classiqueValue),Toast.LENGTH_SHORT).show();
////                                return true;
////                            }
////                        });
//                        break;
//                    case Application.UNIT_DU:
//                        currentAmount.setText(dir.concat(relativeFormatter(context,
//                                quantitativeToRelative(context,classiqueValue,mUd))));
//                        break;
//                    case Application.UNIT_TIME:
//                        BigDecimal timeValue = quantitativeToTime(context,classiqueValue,delay,mUd);
//                        if (dir.equals("")) {
//                            currentAmount.setText(Format.timeFormatter(context, timeValue));
//                        } else {
//                            currentAmount.setText(dir.concat("(").concat(timeFormatter(context, timeValue)).concat(")"));
//                        }
//                        break;
//                }
//            }
//            if(defaultAmount!=null) {
//                if (defaultUnit == unit) {
//                    defaultAmount.setVisibility(View.GONE);
//                } else {
//                    defaultAmount.setVisibility(View.VISIBLE);
//                    switch (defaultUnit) {
//                        case Application.UNIT_CLASSIC:
//                            defaultAmount.setText(quantitativeFormatter(context, classiqueValue, currencyName));
////                        defaultAmount.setOnLongClickListener(new View.OnLongClickListener() {
////                            @Override
////                            public boolean onLongClick(View v) {
////                                Toast.makeText(context, (new DecimalFormat("#,###")).format(classiqueValue), Toast.LENGTH_SHORT).show();
////                                return true;
////                            }
////                        });
//                            break;
//                        case Application.UNIT_DU:
//                            defaultAmount.setText(relativeFormatter(context, quantitativeToRelative(context, classiqueValue, mUd)));
//                            break;
//                        case Application.UNIT_TIME:
//                            defaultAmount.setText(timeFormatter(context, quantitativeToTime(context, classiqueValue, delay, mUd)));
//                            break;
//                    }
//                }
//            }
//        }

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
