package org.duniter.app.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import org.duniter.app.Application;
import org.duniter.app.R;
import org.duniter.app.technical.format.Formater;
import org.duniter.app.technical.format.Time;
import org.duniter.app.technical.format.UnitCurrency;

/**
 * Created by naivalf27 on 26/10/15.
 */
public class ConverterDialog extends DialogFragment{



    private int timeSelected = Time.MINUTE;
    private int lastTimeSelected = Time.MINUTE;

    private EditText txt_coin;
    private EditText txt_du;
    private static EditText mAmount;

    private EditText txt_time;

    private static long dividend;

    private static int delay;
    private int unit;
    private int decimal;
    private static int base;

    private TextWatcher for_coin, for_du;
    private TextWatcher for_time;
    private Spinner list_Unit_time;
    private static Spinner mSpinner;
    private TextView time_converted;
    private static String currencyName;

    public static ConverterDialog newInstance(long _dividend, int _delay, int _base, EditText _mAmount, Spinner _spinner,String _name) {
        dividend = _dividend;
        delay = _delay;
        base = _base;
        mAmount = _mAmount;
        mSpinner = _spinner;
        currencyName = _name;
        Bundle args = new Bundle();

        ConverterDialog fragment = new ConverterDialog();
        fragment.setArguments(args);
        return fragment;
    }

//    public ConverterDialog(BigInteger dividend, int delay, EditText mAmount, Spinner spinner,String name) {
//        this.dividend = dividend;
//        this.delay = delay;
//        this.mAmount = mAmount;
//        this.mSpinner = spinner;
//        this.currencyName = name;
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        decimal = preferences.getInt(Application.DECIMAL,4);
        unit = Integer.parseInt(preferences.getString(Application.UNIT, Application.UNIT_DU + ""));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_converter, null);
        builder.setView(view);
        builder.setTitle(getString(R.string.converter));

        txt_coin = (EditText) view.findViewById(R.id.txt_coin);
        txt_du = (EditText) view.findViewById(R.id.txt_du);
        txt_time = (EditText) view.findViewById(R.id.txt_time);

        TextView nameCurrency = (TextView) view.findViewById(R.id.currency_name);
        nameCurrency.setText(this.currencyName);

        list_Unit_time = (Spinner) view.findViewById(R.id.list_Unit_time);
        List list = Arrays.asList(getResources().getStringArray(R.array.list_unit_time));
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        list_Unit_time.setAdapter(dataAdapter);
        list_Unit_time.setSelection(Time.MINUTE);
        list_Unit_time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                timeSelected = position;
                if (lastTimeSelected != timeSelected) {
                    lastTimeSelected = timeSelected;
                    String val = txt_time.getText().toString();
                    if (val.equals("") || val.equals(".") || val.equals(" ")) {
                        val = "0";
                    }
                    if (val.substring(0, 1).equals(".")) {
                        val = "0" + val;
                    }
                    long value = Time.toMilliSecond(Long.valueOf(val), timeSelected);

                    majValue(String.valueOf(value), Application.UNIT_TIME);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        time_converted = (TextView) view.findViewById(R.id.time_converted);

        viewCreated();

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                enterValueInFragment();
                dismiss();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });

        view.clearFocus();
        return builder.create();
    }

    private void majValue(String value,int unit){
        long quantitatif;
        String valueQuantitatif;
        double relatif;
        String valueRelatif;
        long time;
        removeTextWatcher();
        switch (unit){
            case Application.UNIT_CLASSIC:
                quantitatif = Long.valueOf(value);

                relatif = UnitCurrency.quantitatif_relatif(quantitatif,base, dividend,base);
                txt_du.setText(String.valueOf(relatif));

                time = UnitCurrency.quantitatif_time(quantitatif, base,dividend,base,delay);
                time_converted.setText(Formater.timeFormatterV2(getActivity(),time));
                txt_time.setText(String.valueOf(convertTime(time)));
                break;
            case Application.UNIT_DU:
                relatif = Double.valueOf(value);

                quantitatif = UnitCurrency.relatif_quantitatif(relatif,base,dividend,base).quantitatif;
//                valueQuantitatif = quantitatif.toString();
                txt_coin.setText(String.valueOf(quantitatif));

                time = UnitCurrency.quantitatif_time(quantitatif,base, dividend,base,delay);
                time_converted.setText(Formater.timeFormatterV2(getActivity(),time));
                txt_time.setText(String.valueOf(convertTime(time)));
                break;
            case Application.UNIT_TIME:
                time = Long.valueOf(value);
                time_converted.setText(Formater.timeFormatterV2(getActivity(),time));

                quantitatif = UnitCurrency.time_quantitatif(time,dividend,base,delay).quantitatif;
//                valueQuantitatif = quantitatif.toString();
                txt_coin.setText(String.valueOf(quantitatif));

                relatif = UnitCurrency.quantitatif_relatif(quantitatif,base, dividend,base);
                txt_du.setText(String.valueOf(relatif));
                break;
        }
        addTextWatcher();
    }

//    private void majTime(String textview,boolean is_second){
//        BigDecimal val = new BigDecimal(textview);
//        BigInteger i = Time.toMilliSecond(val, timeSelected);
//        BigInteger coin = UnitCurrency.time_quantitatif(i, dividend, delay);
//        removeTextWatcher();
//        time_converted.setText(Formater.timeFormatterV2(getActivity(), i));
//        txt_coin.setText(String.valueOf(coin));
//        txt_du.setText(String.valueOf(UnitCurrency.quantitatif_relatif(coin, dividend)));
//        addTextWatcher();
//    }

    private long convertTime(long val){
        switch (timeSelected){
            case Time.YEAR:
                val = Time.milliSecondToYear(val);
                break;
            case Time.DAY:
                val = Time.milliSecondToDay(val);
                break;
            case Time.HOUR:
                val = Time.milliSecondToHour(val);
                break;
            case Time.MINUTE:
                val = Time.milliSecondToMinute(val);
                break;
            case Time.SECOND:
                val = Time.milliSecondToSeconde(val);
                break;
        }
        return val;
    }

    private void viewCreated(){
        creatTextWatcher();
        addTextWatcher();

        String val = mAmount.getText().toString();

        if(val.equals("") || val.equals(".") || val.equals(" ")) {
            val ="0";
        }
        if(val.substring(0,1).equals(".")){
            val = "0"+val;
        }
        switch (unit) {
            case Application.UNIT_CLASSIC:
                txt_coin.setText(val);
                break;
            case Application.UNIT_DU:
                txt_du.setText(val);
                break;
            case Application.UNIT_TIME:
                list_Unit_time.setSelection(mSpinner.getSelectedItemPosition());
                txt_time.setText(val);
                break;
        }
    }

    private void removeTextWatcher(){
        txt_coin.removeTextChangedListener(for_coin);
        txt_du.removeTextChangedListener(for_du);
        txt_time.removeTextChangedListener(for_time);
    }

    private void addTextWatcher(){
        txt_coin.addTextChangedListener(for_coin);
        txt_du.addTextChangedListener(for_du);
        txt_time.addTextChangedListener(for_time);
    }

    public void creatTextWatcher(){
        for_coin = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String val = txt_coin.getText().toString();
                if(val.equals("") || val.equals(".") || val.equals(" ")) {
                    val ="0";
                }
                if(val.substring(0,1).equals(".")){
                    val = "0"+val;
                }
                majValue(val,Application.UNIT_CLASSIC);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        for_du = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String val = txt_du.getText().toString();
                if(val.equals("") || val.equals(".") || val.equals(" ")) {
                    val ="0";
                }
                if(val.substring(0,1).equals(".")){
                    val = "0"+val;
                }
                majValue(val,Application.UNIT_DU);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        for_time = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String val = txt_time.getText().toString();
                if(val.equals("") || val.equals(".") || val.equals(" ")) {
                    val ="0";
                }
                if(val.substring(0,1).equals(".")){
                    val = "0"+val;
                }
                long value =Time.toMilliSecond(Long.valueOf(val),timeSelected);

                majValue(String.valueOf(value),Application.UNIT_TIME);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }


    private void enterValueInFragment(){
        switch (unit){
            case Application.UNIT_CLASSIC:
                mAmount.setText(txt_coin.getText().toString());
                break;
            case Application.UNIT_DU:
                mAmount.setText(txt_du.getText().toString());
                break;
            case Application.UNIT_TIME:
                mAmount.setText(txt_time.getText());
                mSpinner.setSelection(list_Unit_time.getSelectedItemPosition());
                break;
        }
    }
}