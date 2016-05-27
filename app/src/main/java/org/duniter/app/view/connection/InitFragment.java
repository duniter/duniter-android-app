package org.duniter.app.view.connection;

import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.duniter.app.R;

public class InitFragment extends Fragment implements View.OnClickListener {

    public static InitFragment newInstance() {
        InitFragment fragment = new InitFragment();
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

        return inflater.inflate(R.layout.fragment_connection_init,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btConnection = (Button) view.findViewById(R.id.bt_connection);
        Button btInscription = (Button) view.findViewById(R.id.bt_inscription);

        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            ((TextView)view.findViewById(R.id.version)).setText("v"+pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        btConnection.setOnClickListener(this);
        btInscription.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(getActivity() instanceof FirstConnectionActivity) {
            int next;
            switch (v.getId()) {
                case R.id.bt_connection:
                    next = ConnectionActivity.CONNECTION;
                    break;
                case R.id.bt_inscription:
                    next = ConnectionActivity.INSCRIPTION;
                    break;
                default:
                    next = ConnectionActivity.INSCRIPTION;
                    break;
            }
            ((FirstConnectionActivity) getActivity()).setNext(next);
        }
    }
}
