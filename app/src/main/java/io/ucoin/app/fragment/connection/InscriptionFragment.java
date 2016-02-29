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
import com.android.volley.VolleyError;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.widget.InscriptionView;
import io.ucoin.app.widget.SelectorCurrencyView;

public class InscriptionFragment extends Fragment implements View.OnClickListener, Response.ErrorListener {

    private LinearLayout mFieldLayout;
    private LinearLayout mProgressLayout;

    private EditText mUid;
    private EditText mSalt;
    private EditText mPassword;
    private EditText mConfirmPassword;

    private Button btInscription;
    private Long currencyId;

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

        currencyId = null;

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

        selectorCurrencyView = new SelectorCurrencyView(true,getActivity(), view.findViewById(R.id.selector_currency), new SelectorCurrencyView.Action() {
            @Override
            public void currencyIdFind(Long id) {
                currencyId = id;
                inscriptionView.validateWallet(id, getFragmentManager());
            }

            @Override
            public void onError(VolleyError error) {
                onErrorResponse(error);
            }
        });
        inscriptionView = new InscriptionView(getActivity(), mUid, mSalt, mPassword, mConfirmPassword, selectorCurrencyView, new InscriptionView.Action() {
            @Override
            public void onFinish() {
                onFinishFragment();
            }

            @Override
            public void onError(VolleyError error, boolean forUid, boolean forPubKey) {
                if(error==null && (forUid || forPubKey)){
                    if(forUid){
                        badUid();
                    }
                    if(forPubKey){
                        badPublicKey();
                    }
                }else {
                    onErrorResponse(error);
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
                if(currencyId == null) {
                    selectorCurrencyView.checkCurrency();
                }else{
                    inscriptionView.validateWallet(currencyId, getFragmentManager());
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

    @Override
    public void onErrorResponse(VolleyError error) {
        if(error == null){
            Toast.makeText(Application.getContext(), getResources().getString(R.string.wallet_already_exists), Toast.LENGTH_SHORT).show();
        }else if(error instanceof NoConnectionError) {
            Toast.makeText(Application.getContext(),
                    getResources().getString(R.string.no_connection),
                    Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(Application.getContext(), error.toString(), Toast.LENGTH_LONG).show();
        }
        mFieldLayout.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.GONE);
        btInscription.setVisibility(View.VISIBLE);
    }

    public void onFinishFragment(){
        Application.requestSync();
        if(getActivity() instanceof FinishAction){
            ((FinishAction) getActivity()).finishInscription(currencyId);
        }
    }

    public interface FinishAction {
        void finishInscription(Long currencyId);
    }
}
