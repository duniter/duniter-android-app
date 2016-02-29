package io.ucoin.app.fragment.connection;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import io.ucoin.app.Application;
import io.ucoin.app.R;

public class FirstPinFragment extends Fragment implements View.OnClickListener {

    private EditText pin1;
    private EditText pin2;
    private EditText pin3;
    private EditText pin4;
    private EditText pin5;
    private EditText pin6;
    private EditText pin7;
    private EditText pin8;
    private LinearLayout layoutPin;
    private LinearLayout layoutPin2;
    private Button btConnection;

    private TextWatcher twPin1;
    private TextWatcher twPin2;
    private TextWatcher twPin3;
    private TextWatcher twPin4;
    private TextWatcher twPin5;
    private TextWatcher twPin6;
    private TextWatcher twPin7;
    private TextWatcher twPin8;

    public static FirstPinFragment newInstance() {
        FirstPinFragment fragment = new FirstPinFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_connection_create_pin,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pin1 = (EditText) view.findViewById(R.id.pin1);
        pin2 = (EditText) view.findViewById(R.id.pin2);
        pin3 = (EditText) view.findViewById(R.id.pin3);
        pin4 = (EditText) view.findViewById(R.id.pin4);
        pin5 = (EditText) view.findViewById(R.id.pin5);
        pin6 = (EditText) view.findViewById(R.id.pin6);
        pin7 = (EditText) view.findViewById(R.id.pin7);
        pin8 = (EditText) view.findViewById(R.id.pin8);

        layoutPin = (LinearLayout) view.findViewById(R.id.layout_pin);
        layoutPin2 = (LinearLayout) view.findViewById(R.id.layout_pin2);
        btConnection = (Button) view.findViewById(R.id.bt_connection);

        btConnection.setOnClickListener(this);

        createTextListener();

        addTextListener();
    }

    void addTextListener() {
        pin1.addTextChangedListener(twPin1);
        pin2.addTextChangedListener(twPin2);
        pin3.addTextChangedListener(twPin3);
        pin4.addTextChangedListener(twPin4);
        pin5.addTextChangedListener(twPin5);
        pin6.addTextChangedListener(twPin6);
        pin7.addTextChangedListener(twPin7);
        pin8.addTextChangedListener(twPin8);
    }


    void removeTextListener(){
        pin1.removeTextChangedListener(twPin1);
        pin2.removeTextChangedListener(twPin2);
        pin3.removeTextChangedListener(twPin3);
        pin4.removeTextChangedListener(twPin4);
        pin5.removeTextChangedListener(twPin5);
        pin6.removeTextChangedListener(twPin6);
        pin7.removeTextChangedListener(twPin7);
        pin8.removeTextChangedListener(twPin8);
    }

    void createTextListener(){
        twPin1 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                pin2.requestFocus();
            }
        };
        twPin2 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                pin3.requestFocus();
            }
        };
        twPin3 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                pin4.requestFocus();
            }
        };
        twPin4 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(checkfield()) {
                    pin5.requestFocus();
                }
            }
        };

        twPin5 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                pin6.requestFocus();
            }
        };
        twPin6 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                pin7.requestFocus();
            }
        };
        twPin7 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                pin8.requestFocus();
            }
        };
        twPin8 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                connection();
            }
        };
    }

    public boolean checkfield(){
        String p1 = pin1.getText().toString().equals("") ? "0" : pin1.getText().toString();
        String p2 = pin2.getText().toString().equals("") ? "0" : pin2.getText().toString();
        String p3 = pin3.getText().toString().equals("") ? "0" : pin3.getText().toString();
        String p4 = pin4.getText().toString().equals("") ? "0" : pin4.getText().toString();

        String text1 = p1.concat(p2).concat(p3).concat(p4);
        if(text1.equals("0000") || text1.equals("1234")){
            removeTextListener();
            pin4.setText("");
            pin3.setText("");
            pin2.setText("");
            pin1.setText("");
            pin1.requestFocus();
            addTextListener();
            Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
            Vibrator mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            layoutPin.startAnimation(shake);
            mVibrator.vibrate(300);
            Toast.makeText(getActivity(),getString(R.string.pin_diferent),Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void connection(){

        String p1 = pin1.getText().toString().equals("") ? "0" : pin1.getText().toString();
        String p2 = pin2.getText().toString().equals("") ? "0" : pin2.getText().toString();
        String p3 = pin3.getText().toString().equals("") ? "0" : pin3.getText().toString();
        String p4 = pin4.getText().toString().equals("") ? "0" : pin4.getText().toString();

        String p5 = pin5.getText().toString().equals("") ? "0" : pin5.getText().toString();
        String p6 = pin6.getText().toString().equals("") ? "0" : pin6.getText().toString();
        String p7 = pin7.getText().toString().equals("") ? "0" : pin7.getText().toString();
        String p8 = pin8.getText().toString().equals("") ? "0" : pin8.getText().toString();

        String text1 = p1.concat(p2).concat(p3).concat(p4);

        String text2 = p5.concat(p6).concat(p7).concat(p8);

        Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
        Vibrator mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        if(text1.equals("0000") || text1.equals("1234")){
            removeTextListener();
            pin4.setText("");
            pin3.setText("");
            pin2.setText("");
            pin1.setText("");
            pin5.setText("");
            pin6.setText("");
            pin7.setText("");
            pin8.setText("");
            pin1.requestFocus();
            addTextListener();
            layoutPin.startAnimation(shake);
            mVibrator.vibrate(300);
            Toast.makeText(getActivity(),getString(R.string.pin_diferent),Toast.LENGTH_SHORT).show();
        }else if(!text1.equals(text2)) {
            removeTextListener();
            pin5.setText("");
            pin6.setText("");
            pin7.setText("");
            pin8.setText("");
            pin5.requestFocus();
            addTextListener();
            layoutPin2.startAnimation(shake);
            mVibrator.vibrate(300);
            Toast.makeText(getActivity(),getString(R.string.pin_dont_match),Toast.LENGTH_SHORT).show();
        }else{
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
            editor.putString(Application.PIN, text1);
            editor.apply();
            if(getActivity() instanceof FinishAction){
                ((FinishAction) getActivity()).finishFirstPinConnection();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_connection:
                connection();
                break;
        }
    }

    public interface FinishAction {
        void finishFirstPinConnection();
    }
}
