package io.ucoin.app.fragment.connection;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import io.ucoin.app.R;
import io.ucoin.app.activity.ConnectionActivity;

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

        btConnection.setOnClickListener(this);
        btInscription.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_connection:
                if(getActivity() instanceof ConnectionActivity){
                    ((ConnectionActivity) getActivity()).finishInit(ConnectionActivity.CONNECTION);
                }
                break;
            case R.id.bt_inscription:
                if(getActivity() instanceof ConnectionActivity){
                    ((ConnectionActivity) getActivity()).finishInit(ConnectionActivity.INSCRIPTION);
                }
                break;
        }
    }

    public interface FinishAction {
        void finishFirstConnection(Long currencyId);
    }
}
