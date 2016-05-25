package org.duniter.app.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.duniter.app.Application;
import org.duniter.app.R;

/**
 * Created by naivalf27 on 23/02/16.
 */
public class PinPreference extends DialogPreference {

    private String code1;
    private String code2;
    private String code3;

    private boolean confirm1 = false;
    private boolean confirm2 = false;

    private ViewHolder holder;

    private Context context;
    private SharedPreferences preferences;

    public PinPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        setPersistent(false);
        setDialogLayoutResource(R.layout.dialog_preference_pin);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        this.code1 = "";
        this.code2 = "";
        this.code3 = "";

        this.holder = new ViewHolder(view);

        initPin();
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        AlertDialog alertDlg = (AlertDialog)getDialog();
        Button btn = alertDlg.getButton(AlertDialog.BUTTON_POSITIVE);
        btn.setVisibility(View.GONE);
    }

    private void initPin(){
        List<String> list = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        Collections.shuffle(list);

        this.holder.b0.setText(list.get(0));
        this.holder.b1.setText(list.get(1));
        this.holder.b2.setText(list.get(2));
        this.holder.b3.setText(list.get(3));
        this.holder.b4.setText(list.get(4));
        this.holder.b5.setText(list.get(5));
        this.holder.b6.setText(list.get(6));
        this.holder.b7.setText(list.get(7));
        this.holder.b8.setText(list.get(8));
        this.holder.b9.setText(list.get(9));

        this.holder.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove();
            }
        });

        this.holder.b0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                press(holder.b0);
            }
        });

        this.holder.b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                press(holder.b1);
            }
        });

        this.holder.b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                press(holder.b2);
            }
        });

        this.holder.b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                press(holder.b3);
            }
        });

        this.holder.b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                press(holder.b4);
            }
        });

        this.holder.b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                press(holder.b5);
            }
        });

        this.holder.b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                press(holder.b6);
            }
        });

        this.holder.b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                press(holder.b7);
            }
        });

        this.holder.b8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                press(holder.b8);
            }
        });

        this.holder.b9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                press(holder.b9);
            }
        });
    }

    private void press(Button b){
        if (confirm1 && !confirm2) {
            code2 += b.getText();
            holder.listPin.get(code2.length()-1).setImageResource(R.drawable.ic_dot_activate);
        } else if(confirm1 && confirm2) {
            code3 += b.getText();
            holder.listPin.get(code3.length()-1).setImageResource(R.drawable.ic_dot_activate);
        }else{
            code1 += b.getText();
            holder.listPin.get(code1.length()-1).setImageResource(R.drawable.ic_dot_activate);
        }
        change();
    }

    private void change(){
        if(!confirm2) {
            if (!confirm1 && code1.length() == 4) {
                String pin = preferences.getString(Application.PIN, "p");
                if (code1.equals(pin)) {
                    confirm1 = true;
                    this.holder.actual_context.setText(this.context.getString(R.string.tap_new_pin_code));
                    remove();
                }else{
                    getDialog().dismiss();
                    Toast.makeText(context, context.getString(R.string.pin_dont_valid),Toast.LENGTH_SHORT).show();
                    onDialogClosed(false);
                }
            }

            if (confirm1 && code2.length() == 4){
                confirm2 = true;
                remove();
                this.holder.actual_context.setText(this.context.getString(R.string.tap_confirm_pin_code));
            }
        }else{
            if(code3.length()==4 && code3.equals(code2)){
                getDialog().dismiss();
                onDialogClosed(true);
            }else if (code3.length()==4){
                getDialog().dismiss();
                Toast.makeText(context, context.getString(R.string.pin_dont_match), Toast.LENGTH_SHORT).show();
                onDialogClosed(false);
            }
        }
    }

    public void remove(){
        code1 = (!confirm1 && !confirm2) ? "" : code1;
        code2 = (confirm1 && !confirm2) ? "" : code2;
        code3 = (confirm1 && confirm2) ? "" : code3;

        for(ImageView img : this.holder.listPin){
            img.setImageResource(R.drawable.ic_dot_disable);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Application.PIN, code3);
            editor.apply();
            Toast.makeText(context, context.getString(R.string.pin_changed_ok),Toast.LENGTH_SHORT).show();
        }
        code1 = "";
        code2 = "";
        code3 ="";
        confirm1 = false;
        confirm2 = false;
        super.onDialogClosed(positiveResult);
    }

    public static class ViewHolder {
        public View rootView;

        public ArrayList<ImageView> listPin;

        public TextView actual_context;
        public ImageView del;
        public Button b0;
        public Button b1;
        public Button b2;
        public Button b3;
        public Button b4;
        public Button b5;
        public Button b6;
        public Button b7;
        public Button b8;
        public Button b9;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            listPin =new ArrayList<>();
            this.actual_context = (TextView) rootView.findViewById(R.id.actual_context);
            listPin.add((ImageView) rootView.findViewById(R.id.pin1));
            listPin.add((ImageView) rootView.findViewById(R.id.pin2));
            listPin.add((ImageView) rootView.findViewById(R.id.pin3));
            listPin.add((ImageView) rootView.findViewById(R.id.pin4));
            this.del = (ImageView) rootView.findViewById(R.id.del);
            this.b0 = (Button) rootView.findViewById(R.id.b0);
            this.b1 = (Button) rootView.findViewById(R.id.b1);
            this.b2 = (Button) rootView.findViewById(R.id.b2);
            this.b3 = (Button) rootView.findViewById(R.id.b3);
            this.b4 = (Button) rootView.findViewById(R.id.b4);
            this.b5 = (Button) rootView.findViewById(R.id.b5);
            this.b6 = (Button) rootView.findViewById(R.id.b6);
            this.b7 = (Button) rootView.findViewById(R.id.b7);
            this.b8 = (Button) rootView.findViewById(R.id.b8);
            this.b9 = (Button) rootView.findViewById(R.id.b9);
        }

    }
}