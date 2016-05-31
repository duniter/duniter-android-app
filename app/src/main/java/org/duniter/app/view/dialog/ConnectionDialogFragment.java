package org.duniter.app.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.duniter.app.Application;
import org.duniter.app.R;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.technical.callback.CallbackBoolean;
import org.duniter.app.widget.ConnectionView;
import org.duniter.app.widget.SelectorCurrencyView;

public class ConnectionDialogFragment extends DialogFragment {

    private LinearLayout mFieldLayout;
    private LinearLayout mProgressLayout;

    private EditText mSalt;
    private EditText mPassword;
    private AlertDialog alert;

    private SelectorCurrencyView selectorCurrencyView;
    private ConnectionView connectionView;

    private Currency currency;

    public static ConnectionDialogFragment newInstance() {
        ConnectionDialogFragment fragment = new ConnectionDialogFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_fragment_connection, null);
        builder.setView(view);
        builder.setTitle(getString(R.string.connection_wallet));

        currency = null;

        mFieldLayout = (LinearLayout) view.findViewById(R.id.field_layout);
        mProgressLayout = (LinearLayout) view.findViewById(R.id.progress_layout);
        mSalt = (EditText) view.findViewById(R.id.salt);
        mPassword = (EditText) view.findViewById(R.id.password);
        mPassword.setTypeface(Typeface.DEFAULT);

        selectorCurrencyView = new SelectorCurrencyView(false,getActivity(), view.findViewById(R.id.selector_currency), new SelectorCurrencyView.Action() {
            @Override
            public void currencyIdFind(Currency c) {
                currency = c;
                connectionView.validateWallet(c, getFragmentManager());
            }
        });
        connectionView = new ConnectionView(getActivity(), mSalt, mPassword, selectorCurrencyView, new ConnectionView.Action() {
            @Override
            public void onFinish() {
                alert.dismiss();
            }

            @Override
            public void onError() {
                Toast.makeText(getActivity(),getActivity().getString(R.string.wallet_no_exists),Toast.LENGTH_SHORT).show();
            }
        });

        mPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    alert.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });

        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNeutralButton(R.string.help, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), getString(R.string.in_dev), Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });

        view.clearFocus();
        alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean valid = connectionView.checkField();
                        if (valid) {
                            mFieldLayout.setVisibility(View.GONE);
                            mProgressLayout.setVisibility(View.VISIBLE);
                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
                            alert.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);
                            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
                            mPassword.requestFocus();
                            Application.hideKeyboard(getActivity(), mPassword);
                            if(currency==null) {
                                selectorCurrencyView.checkCurrency(new CallbackBoolean() {
                                    @Override
                                    public void methode(boolean noError) {
                                        if (!noError){
                                            mFieldLayout.setVisibility(View.VISIBLE);
                                            mProgressLayout.setVisibility(View.GONE);
                                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                                            alert.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
                                            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }else{
                                connectionView.validateWallet(currency, getFragmentManager());
                            }
                        }
                    }
                });
            }
        });
        return alert;
    }
/*
    @Override
    public void onErrorResponse(VolleyError error) {
        if(error == null){
            Toast.makeText(Application.getContext(), getResources().getString(R.string.wallet_already_exists), Toast.LENGTH_SHORT).show();
        }else if(error instanceof NoConnectionError) {
            error.printStackTrace();
            Toast.makeText(Application.getContext(),
                    getResources().getString(R.string.no_connection),
                    Toast.LENGTH_LONG).show();
        }else if (error instanceof ServerError){
            error.printStackTrace();
            Toast.makeText(Application.getContext(),getResources().getString(R.string.wallet_no_exists), Toast.LENGTH_LONG).show();
        }else {
            error.printStackTrace();
            Toast.makeText(Application.getContext(), error.toString(), Toast.LENGTH_LONG).show();
        }
        mFieldLayout.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.GONE);
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
        alert.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
    }*/
}



