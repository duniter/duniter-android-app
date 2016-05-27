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

import org.duniter.app.Application;
import org.duniter.app.R;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.technical.callback.CallbackBoolean;
import org.duniter.app.widget.InscriptionView;
import org.duniter.app.widget.SelectorCurrencyView;

public class InscriptionFragment extends Fragment implements View.OnClickListener {

    private LinearLayout mFieldLayout;
    private LinearLayout mProgressLayout;

    private EditText mUid;
    private EditText mSalt;
    private EditText mPassword;
    private EditText mConfirmPassword;

    private Button btInscription;
    private Currency currency;

    private SelectorCurrencyView selectorCurrencyView;
    private InscriptionView inscriptionView;

    public static InscriptionFragment newInstance() {
        InscriptionFragment fragment = new InscriptionFragment();
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

        return inflater.inflate(R.layout.fragment_connection_inscription,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currency = null;

        mFieldLayout = (LinearLayout) view.findViewById(R.id.field_layout);
        mProgressLayout = (LinearLayout) view.findViewById(R.id.progress_layout);
        mUid = (EditText) view.findViewById(R.id.uid);
        mSalt = (EditText) view.findViewById(R.id.salt);
        mPassword = (EditText) view.findViewById(R.id.password);
        mPassword.setTypeface(Typeface.DEFAULT);
        mConfirmPassword = (EditText) view.findViewById(R.id.confirm_password);
        mConfirmPassword.setTypeface(Typeface.DEFAULT);
        btInscription = (Button) view.findViewById(R.id.bt_inscription);
        btInscription.setOnClickListener(this);

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
                inscriptionView.validateWallet(c, getFragmentManager());
            }
        });
        inscriptionView = new InscriptionView(getActivity(), mUid, mSalt, mPassword, mConfirmPassword, selectorCurrencyView, new InscriptionView.Action() {
            @Override
            public void onFinish() {
                onFinishFragment();
            }

            @Override
            public void onError(boolean isUid) {
                if(isUid){
                    badUid();
                }else{
                    badPublicKey();
                }
            }
        });

        mConfirmPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btInscription.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_inscription:
                mFieldLayout.setVisibility(View.GONE);
                mProgressLayout.setVisibility(View.VISIBLE);
                btInscription.setVisibility(View.GONE);
                mConfirmPassword.requestFocus();
                Application.hideKeyboard(getActivity(), mConfirmPassword);
                if(currency == null) {
                    selectorCurrencyView.checkCurrency(new CallbackBoolean() {
                        @Override
                        public void methode(boolean noError) {
                            if (!noError) {
                                mFieldLayout.setVisibility(View.VISIBLE);
                                mProgressLayout.setVisibility(View.GONE);
                                btInscription.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }else{
                    inscriptionView.validateWallet(currency, getFragmentManager());
                }
                break;
        }
    }

    public void badPublicKey(){
        mFieldLayout.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.GONE);
        btInscription.setVisibility(View.VISIBLE);
        mSalt.setError(getString(R.string.wallet_already_registered));
        mSalt.selectAll();
        mSalt.requestFocus();
    }

    public void badUid(){
        mFieldLayout.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.GONE);
        btInscription.setVisibility(View.VISIBLE);
        mUid.setError(getString(R.string.uid_alredy_exist));
        mUid.selectAll();
        mUid.requestFocus();
    }

    /*##########ERROR##########*\
        mFieldLayout.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.GONE);
        btInscription.setVisibility(View.VISIBLE);
    \*#########################*/

    public void onFinishFragment(){
        Application.requestSync();
        if(getActivity() instanceof ConnectionActivity){
            ((ConnectionActivity) getActivity()).setCurrency(currency);
        }
    }
}
