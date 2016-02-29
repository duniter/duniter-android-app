package io.ucoin.app.fragment.dialog;

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
import java.util.Arrays;
import java.util.List;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.Format;

/**
 * Created by naivalf27 on 26/10/15.
 */
public class ConverterDialog extends DialogFragment{



    private int timeSelected = Format.Time.MINUTE;
    private int lastTimeSelected = Format.Time.MINUTE;

    private EditText txt_coin, txt_du, mAmount;

    private EditText txt_time;

    private BigInteger mUd;

    private int delay, unit;

    private TextWatcher for_coin, for_du;
    private TextWatcher for_time;
    private Spinner list_Unit_time;
    private Spinner mSpinner;
    private TextView time_converted;
    private String currencyName;

    public ConverterDialog(BigInteger mUd, int delay, EditText mAmount, Spinner spinner,String name) {
        this.mUd = mUd;
        this.delay = delay;
        this.mAmount = mAmount;
        this.mSpinner = spinner;
        this.currencyName = name;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        unit = preferences.getInt(Application.UNIT,Application.UNIT_CLASSIC);

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
        list_Unit_time.setSelection(Format.Time.MINUTE);
        list_Unit_time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                lastTimeSelected = timeSelected;
                timeSelected = position;
                String val = txt_time.getText().toString();
                if(val.equals("") || val.equals(".") || val.equals(" ")) {
                    val ="0";
                }
                if(val.substring(0,1).equals(".")){
                    val = "0"+val;
                }
                majTime(val,false);
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

    private void majTime(String textview,boolean is_second){
        BigDecimal val = new BigDecimal(textview);
        if(!is_second) {
            val = Format.Time.toSecond(getActivity(), val, timeSelected);
            BigInteger coin = Format.Currency.timeToQuantitative(getActivity(), val, delay, mUd);
            removeTextWatcher();
            time_converted.setText(Format.timeFormatter(getActivity(), val));
            txt_coin.setText(String.valueOf(coin));
            txt_du.setText(Format.Currency.quantitativeToRelative(getActivity(), coin, mUd).toString());
            addTextWatcher();
        }else{
            time_converted.setText(Format.timeFormatter(getActivity(), val));
            val = convertTime(val);
            txt_time.setText(String.valueOf(val));
        }
    }

    private BigDecimal convertTime(BigDecimal val){
        switch (timeSelected){
            case Format.Time.YEAR:
                val = Format.Time.secondToYear(getActivity(), val);
                break;
            case Format.Time.DAY:
                val = Format.Time.secondToDay(getActivity(), val);
                break;
            case Format.Time.HOUR:
                val = Format.Time.secondToHour(getActivity(), val);
                break;
            case Format.Time.MINUTE:
                val = Format.Time.secondToMinute(getActivity(), val);
                break;
            case Format.Time.MILLI_SECOND:
                val = Format.Time.secondToMilliSecond(val);
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
                removeTextWatcher();
                txt_du.setText(Format.Currency.quantitativeToRelative(getActivity(), new BigInteger(val), mUd).toString());
                majTime(Format.Currency.quantitativeToTime(getActivity(), new BigInteger(val), delay, mUd).toString(), true);
                addTextWatcher();
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
                BigInteger coin = Format.Currency.relativeToQuantitative(getActivity(), new BigDecimal(val), mUd);
                removeTextWatcher();
                txt_coin.setText(String.valueOf(coin));
                majTime(Format.Currency.quantitativeToTime(getActivity(), coin, delay, mUd).toString(), true);
                addTextWatcher();
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
                majTime(val,false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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