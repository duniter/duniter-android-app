package org.duniter.app.view.connection;

import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

import org.duniter.app.Application;
import org.duniter.app.R;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.services.SqlService;
import org.duniter.app.technical.callback.CallbackBoolean;
import org.duniter.app.widget.ConnectionView;
import org.duniter.app.widget.SelectorCurrencyView;

public class ConnectionFragment extends Fragment implements View.OnClickListener{

    private LinearLayout mFieldLayout;
    private LinearLayout mProgressLayout;
    private EditText mSalt;
    private EditText mPassword;
    private Button btConnection;
    private Currency currency;

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

        currency = null;

        mFieldLayout = (LinearLayout) view.findViewById(R.id.field_layout);
        mProgressLayout = (LinearLayout) view.findViewById(R.id.progress_layout);
        mSalt = (EditText) view.findViewById(R.id.salt);
        mSalt.requestFocus();
        mPassword = (EditText) view.findViewById(R.id.password);
        mPassword.setTypeface(Typeface.DEFAULT);
        btConnection = (Button) view.findViewById(R.id.bt_connection);

        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            ((TextView)view.findViewById(R.id.version)).setText("v"+pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        selectorCurrencyView = new SelectorCurrencyView(true,getActivity(), view.findViewById(R.id.selector_currency), new SelectorCurrencyView.Action() {
            @Override
            public void currencyIdFind(Currency c) {
                currency = c;
                connectionView.validateWallet(c, getFragmentManager());
            }
        });
        connectionView = new ConnectionView(getActivity(), mSalt, mPassword, selectorCurrencyView, new ConnectionView.Action() {
            @Override
            public void onFinish() {
                onFinishFragment();
            }

            @Override
            public void onError() {
                mFieldLayout.setVisibility(View.VISIBLE);
                mProgressLayout.setVisibility(View.GONE);
                btConnection.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(),getString(R.string.wallet_no_exists),Toast.LENGTH_SHORT).show();
                SqlService.getCurrencySql(getActivity()).delete(currency.getId());
                currency = null;
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
                    Application.hideKeyboard(getActivity(), mPassword);
                    if(currency==null) {
                        selectorCurrencyView.checkCurrency(new CallbackBoolean() {
                            @Override
                            public void methode(boolean noError) {
                                if (!noError){
                                    mFieldLayout.setVisibility(View.VISIBLE);
                                    mProgressLayout.setVisibility(View.GONE);
                                    btConnection.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }else{
                        connectionView.validateWallet(currency, getFragmentManager());
                    }
                }
                break;
        }
    }

    /*##########ERROR##########*\
        mFieldLayout.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.GONE);
        btConnection.setVisibility(View.VISIBLE);
    \*#########################*/

    public void onFinishFragment(){
        //Application.requestSync();
        if(getActivity() instanceof ConnectionActivity){
            ((ConnectionActivity) getActivity()).setCurrency(currency);
        }
    }
}
