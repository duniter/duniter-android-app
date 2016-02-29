package io.ucoin.app.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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

import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.widget.ConnectionView;
import io.ucoin.app.widget.SelectorCurrencyView;

public class ConnectionDialogFragment extends DialogFragment implements Response.ErrorListener {

    private LinearLayout mFieldLayout;
    private LinearLayout mProgressLayout;

    private EditText mSalt;
    private EditText mPassword;
    private AlertDialog alert;

    private SelectorCurrencyView selectorCurrencyView;
    private ConnectionView connectionView;

    private Long currencyId;

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

        currencyId = null;

        mFieldLayout = (LinearLayout) view.findViewById(R.id.field_layout);
        mProgressLayout = (LinearLayout) view.findViewById(R.id.progress_layout);
        mSalt = (EditText) view.findViewById(R.id.salt);
        mPassword = (EditText) view.findViewById(R.id.password);
        mPassword.setTypeface(Typeface.DEFAULT);

        selectorCurrencyView = new SelectorCurrencyView(false,getActivity(), view.findViewById(R.id.selector_currency), new SelectorCurrencyView.Action() {
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
                Application.requestSync();
                dismiss();
            }

            @Override
            public void onError(VolleyError error) {
                onErrorResponse(error);
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
                            if(currencyId==null) {
                                selectorCurrencyView.checkCurrency();
                            }else{
                                connectionView.validateWallet(currencyId, getFragmentManager());
                            }
                        }
                    }
                });
            }
        });
        return alert;
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
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
        alert.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
    }
}



