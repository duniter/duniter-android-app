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

import io.ucoin.app.Application;
import io.ucoin.app.R;

public class PinFragment extends Fragment implements View.OnClickListener {

    private EditText pin1;
    private EditText pin2;
    private EditText pin3;
    private EditText pin4;
    private LinearLayout layoutPin;
    private Button btConnection;

    private TextWatcher twPin1;
    private TextWatcher twPin2;
    private TextWatcher twPin3;
    private TextWatcher twPin4;

    public static PinFragment newInstance() {
        PinFragment fragment = new PinFragment();
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

        return inflater.inflate(R.layout.fragment_connection_pin,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pin1 = (EditText) view.findViewById(R.id.pin1);
        pin2 = (EditText) view.findViewById(R.id.pin2);
        pin3 = (EditText) view.findViewById(R.id.pin3);
        pin4 = (EditText) view.findViewById(R.id.pin4);
        layoutPin = (LinearLayout) view.findViewById(R.id.layout_pin);
        btConnection = (Button) view.findViewById(R.id.bt_connection);

        btConnection.setOnClickListener(this);

        createTextListener();

        addTextListener();
    }

    void addTextListener(){
        pin1.addTextChangedListener(twPin1);
        pin2.addTextChangedListener(twPin2);
        pin3.addTextChangedListener(twPin3);
        pin4.addTextChangedListener(twPin4);
    }

    void removeTextListener(){
        pin1.removeTextChangedListener(twPin1);
        pin2.removeTextChangedListener(twPin2);
        pin3.removeTextChangedListener(twPin3);
        pin4.removeTextChangedListener(twPin4);
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
                connection();
            }
        };
    }

    public void connection(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String pin = preferences.getString(Application.PIN, "p");

        String text = pin1.getText().toString();
        text += pin2.getText().toString();
        text += pin3.getText().toString();
        text += pin4.getText().toString();

        if(!pin.equals(text) && !pin.equals("p")) {
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
        }else if(pin.equals("p")){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Application.PIN, text);
            editor.apply();
            if(getActivity() instanceof FinishAction){
                ((FinishAction) getActivity()).finishPinConnection();
            }
        }else{
            if(getActivity() instanceof FinishAction){
                ((FinishAction) getActivity()).finishPinConnection();
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
        void finishPinConnection();
    }
}
