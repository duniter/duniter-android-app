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
import org.duniter.app.view.InitActivity;

public class ChooseInitFragment extends Fragment implements View.OnClickListener {

    public static ChooseInitFragment newInstance() {
        ChooseInitFragment fragment = new ChooseInitFragment();
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
            String versionName = "v"+pInfo.versionName;
            ((TextView)view.findViewById(R.id.version)).setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        btConnection.setOnClickListener(this);
        btInscription.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int next = 0;
        switch (v.getId()) {
            case R.id.bt_connection:
                next = InitActivity.ETAPE_3_2;
                break;
            case R.id.bt_inscription:
                next = InitActivity.ETAPE_3_1;
                break;
        }
        if(getActivity() instanceof ConnectionActivity) {
            ((ConnectionActivity) getActivity()).nextEtape(next);
        }
    }
}
