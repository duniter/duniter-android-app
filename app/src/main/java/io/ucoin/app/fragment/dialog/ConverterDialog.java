package io.ucoin.app.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import io.ucoin.app.R;
import io.ucoin.app.fragment.wallet.TransferFragment;
import io.ucoin.app.technical.CurrencyUtils;
import io.ucoin.app.technical.StringUtils;

/**
 * Created by naivalf27 on 26/10/15.
 */
public class ConverterDialog extends DialogFragment{

    private EditText txt_coin, txt_du, txt_time,mAmount;

    private long mUd;

    private int delay, pos;

    private TextWatcher for_coin, for_du, for_time;

    private TransferFragment fragment;

    public ConverterDialog(long mUd,int delay,EditText mAmount,int pos,TransferFragment f) {
        this.mUd = mUd;
        this.delay = delay;
        this.mAmount = mAmount;
        this.pos = pos;
        this.fragment = f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_converter, null);
        builder.setView(view);
        builder.setTitle(getString(R.string.converter));

        txt_coin = (EditText) view.findViewById(R.id.txt_coin);
        txt_du = (EditText) view.findViewById(R.id.txt_du);
        txt_time = (EditText) view.findViewById(R.id.txt_time);

        viewCreated();

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                enterValueInFragment();
                dismiss();
                fragment.updateComvertedAmountView();
            }
        });

        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });


        return builder.create();
    }

    private void viewCreated(){
        creatTextWatcher();

        addTextWatcher();

        String val = mAmount.getText().toString();

        if(StringUtils.isNotBlank(val)) {
            switch (pos) {
                case 0:
                    txt_coin.setText(val);
                    break;
                case 1:
                    txt_du.setText(val);
                    break;
                case 2:
                    txt_time.setText(val);
                    break;
            }
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
                if(!StringUtils.isNotBlank(val) || val.equals(".") || val.equals(" ")) {
                    val ="0";
                }
                if(val.substring(0,1).equals(".")){
                    val = "0"+val;
                }
                removeTextWatcher();
                txt_du.setText(String.valueOf(coinToDu(Long.parseLong(val))));
                txt_time.setText(String.valueOf(coinToTime(Long.parseLong(val))));
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
                if(!StringUtils.isNotBlank(val) || val.equals(".") || val.equals(" ")) {
                    val ="0";
                }
                if(val.substring(0,1).equals(".")){
                    val = "0"+val;
                }
                    long coin = duToCoin(Double.parseDouble(val));
                    removeTextWatcher();
                    txt_coin.setText(String.valueOf(coin));
                    txt_time.setText(String.valueOf(coinToTime(coin)));
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
                if(!StringUtils.isNotBlank(val) || val.equals(".") || val.equals(" ")) {
                    val ="0";
                }
                if(val.substring(0,1).equals(".")){
                    val = "0"+val;
                }
                long coin = timeToCoin(Double.parseDouble(val));
                removeTextWatcher();
                txt_coin.setText(String.valueOf(coin));
                txt_du.setText(String.valueOf(coinToDu(coin)));
                addTextWatcher();
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

    private long timeToCoin(Double time){
        return CurrencyUtils.convertTimeToCoin(time,mUd,delay);
    }

    private long duToCoin(Double du){
        return CurrencyUtils.convertToCoin(du,mUd);
    }

    private Double coinToDu(long coin){
        return CurrencyUtils.convertToUD(coin,mUd);
    }

    private Double coinToTime(long coin){
        return CurrencyUtils.convertCoinToTime(coin,mUd,delay);
    }

    private void enterValueInFragment(){
        switch (pos){
            case 0:
                mAmount.setText(txt_coin.getText().toString());
                break;
            case 1:
                mAmount.setText(txt_du.getText().toString());
                break;
            case 2:
                mAmount.setText(txt_time.getText().toString());
                break;
        }
    }
}