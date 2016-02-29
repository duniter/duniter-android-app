package io.ucoin.app.fragment.connection;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.widget.ConnectionView;
import io.ucoin.app.widget.SelectorCurrencyView;

public class ConnectionFragment extends Fragment implements View.OnClickListener, Response.ErrorListener {

    private LinearLayout mFieldLayout;
    private LinearLayout mProgressLayout;
    private EditText mSalt;
    private EditText mPassword;
    private Button btConnection;
    private Long currencyId;

    private SelectorCurrencyView selectorCurrencyView;
    private ConnectionView connectionView;

    public static ConnectionFragment newInstance() {
        ConnectionFragment fragment = new ConnectionFragment();
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

        return inflater.inflate(R.layout.fragment_connection_connection,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currencyId = null;

        mFieldLayout = (LinearLayout) view.findViewById(R.id.field_layout);
        mProgressLayout = (LinearLayout) view.findViewById(R.id.progress_layout);
        mSalt = (EditText) view.findViewById(R.id.salt);
        mSalt.requestFocus();
        mPassword = (EditText) view.findViewById(R.id.password);
        mPassword.setTypeface(Typeface.DEFAULT);
        btConnection = (Button) view.findViewById(R.id.bt_connection);

        selectorCurrencyView = new SelectorCurrencyView(true,getActivity(), view.findViewById(R.id.selector_currency), new SelectorCurrencyView.Action() {
            @Override
            public void currencyIdFind(Long id) {
                currencyId = id;
                connectionView.validateWallet(id, getFragmentManager());
            }

            @Override
            public void onError(VolleyError error) {
                onErrorResponse(error);
            }
        });
        connectionView = new ConnectionView(getActivity(), mSalt, mPassword, selectorCurrencyView, new ConnectionView.Action() {
            @Override
            public void onFinish() {
                onFinishFragment();
            }

            @Override
            public void onError(VolleyError error) {
                onErrorResponse(error);
            }
        });

        btConnection.setOnClickListener(this);

        mPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btConnection.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_connection:
                boolean valid = connectionView.checkField();
                if (valid) {
                    mFieldLayout.setVisibility(View.GONE);
                    mProgressLayout.setVisibility(View.VISIBLE);
                    btConnection.setVisibility(View.GONE);
                    mPassword.requestFocus();
                    Application.hideKeyboard(getActivity(),mPassword);
                    if(currencyId==null) {
                        selectorCurrencyView.checkCurrency();
                    }else{
                        connectionView.validateWallet(currencyId, getFragmentManager());
                    }
                }
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if(error == null){
            Toast.makeText(Application.getContext(), getResources().getString(R.string.wallet_already_exists), Toast.LENGTH_SHORT).show();
        }else if(error instanceof NoConnectionError) {
            Toast.makeText(Application.getContext(),
                    getResources().getString(R.string.no_connection),
                    Toast.LENGTH_LONG).show();
        }else if (error instanceof ServerError){
            Toast.makeText(Application.getContext(),getResources().getString(R.string.wallet_no_exists), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(Application.getContext(), error.toString(), Toast.LENGTH_LONG).show();
        }
        mFieldLayout.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.GONE);
        btConnection.setVisibility(View.VISIBLE);
    }

    public void onFinishFragment(){
        Application.requestSync();
        if(getActivity() instanceof FinishAction){
            ((FinishAction) getActivity()).finishConnection(currencyId);
        }
    }

    public interface FinishAction {
        void finishConnection(Long currencyId);
    }
}
