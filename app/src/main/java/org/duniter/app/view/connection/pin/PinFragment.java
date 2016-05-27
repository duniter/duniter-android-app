package org.duniter.app.view.connection.pin;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.duniter.app.view.connection.ConnectionActivity;

public class PinFragment extends Fragment{

    private String code1;

    private ViewHolder holder;

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

        this.code1 = "";

        this.holder = new ViewHolder(view);

        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            ((TextView)view.findViewById(R.id.version)).setText("v"+pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        initPin();
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
        code1 += b.getText();
        holder.listPin.get(code1.length()-1).setImageResource(R.drawable.ic_dot_activate);
        change();
    }

    private void change(){
        if(code1.length()==4){
            connection();
        }
    }

    public void remove(){
        code1 = "";

        for(ImageView img : this.holder.listPin){
            img.setImageResource(R.drawable.ic_dot_disable);
        }
    }

    public void connection(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String pin = preferences.getString(Application.PIN, "p");

        if (!code1.equals(pin)) {
            Toast.makeText(getActivity(), getString(R.string.pin_dont_valid), Toast.LENGTH_SHORT).show();
            remove();
        } else {
            if(getActivity() instanceof ConnectionActivity){
                ((ConnectionActivity) getActivity()).nextFragment();
            }
        }
    }

    public interface FinishAction {
        void finishPinConnection();
    }

    public static class ViewHolder {
        public View      rootView;

        public ArrayList<ImageView> listPin;

        public ImageView del;
        public Button    b0;
        public Button    b1;
        public Button    b2;
        public Button    b3;
        public Button    b4;
        public Button    b5;
        public Button    b6;
        public Button    b7;
        public Button    b8;
        public Button    b9;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            listPin =new ArrayList<>();
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
